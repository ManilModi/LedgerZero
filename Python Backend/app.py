import os
import logging
import psycopg2
import pandas as pd
import redis
from fastapi import FastAPI, BackgroundTasks, HTTPException
from neo4j import GraphDatabase
from dotenv import load_dotenv

load_dotenv()

# --- CONFIGURATION ---
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger("GraphSyncAPI")

# DB Credentials
RDS_HOST = os.getenv("RDS_HOST", "localhost")
RDS_PORT = int(os.getenv("RDS_PORT", 5432))
RDS_USER = os.getenv("RDS_USER", "postgres")
RDS_PASSWORD = os.getenv("RDS_PASSWORD", "password")

NEO4J_URI = os.getenv("NEO4J_URI", "bolt://localhost:7687")
NEO4J_AUTH = ("neo4j", os.getenv("NEO4J_PASSWORD", "password"))

REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", 6379))

app = FastAPI(title="LedgerZero Graph Sync Engine")

# --- THE SYNC ENGINE CLASS (Embedded) ---
class GraphSyncService:
    def __init__(self):
        # 1. Neo4j & Redis
        self.driver = GraphDatabase.driver(NEO4J_URI, auth=NEO4J_AUTH)
        self.redis_client = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, db=0, decode_responses=True)
        self.state = self.load_state()

        # 2. Persistent Postgres Connections
        self.gateway_conn = None
        self.switch_conn = None
        self.connect_postgres()

    def connect_postgres(self):
        """Establishes persistent connections"""
        try:
            logger.info("🔌 Connecting to PostgreSQL...")
            if not self.gateway_conn or self.gateway_conn.closed:
                self.gateway_conn = psycopg2.connect(
                    host=RDS_HOST, database="gateway_db", user=RDS_USER, password=RDS_PASSWORD, port=RDS_PORT
                )
            if not self.switch_conn or self.switch_conn.closed:
                self.switch_conn = psycopg2.connect(
                    host=RDS_HOST, database="switch_db", user=RDS_USER, password=RDS_PASSWORD, port=RDS_PORT
                )
            logger.info("✅ PostgreSQL Connected (Persistent).")
        except Exception as e:
            logger.error(f"❌ DB Init Failed: {e}")

    def ensure_conn(self):
        """Reconnects if connection is explicitly closed"""
        if self.gateway_conn is None or self.gateway_conn.closed:
            self.connect_postgres()
        if self.switch_conn is None or self.switch_conn.closed:
            self.connect_postgres()

    def load_state(self):
        try:
            return {
                "last_user_time": self.redis_client.get("sync:last_user_time") or "2023-01-01 00:00:00",
                "last_device_time": self.redis_client.get("sync:last_device_time") or "2023-01-01 00:00:00",
                "last_txn_time": self.redis_client.get("sync:last_txn_time") or "2023-01-01 00:00:00"
            }
        except Exception:
            return {"last_user_time": "2023-01-01", "last_device_time": "2023-01-01", "last_txn_time": "2023-01-01"}

    def save_state(self, key, value):
        try:
            self.state[key] = str(value)
            self.redis_client.set(f"sync:{key}", str(value))
        except Exception:
            pass

    # --- WORKER METHODS (Called by API) ---

    def sync_users(self):
        try:
            self.ensure_conn()
            query = f"SELECT user_id, phone_number, kyc_status, risk_score, created_at FROM users WHERE created_at > '{self.state['last_user_time']}' ORDER BY created_at ASC LIMIT 1000"
            
            df = pd.read_sql(query, self.gateway_conn)
            if df.empty: return

            logger.info(f"🔄 Syncing {len(df)} New Users...")
            with self.driver.session() as session:
                for _, row in df.iterrows():
                    session.run("""
                        MERGE (u:User {userId: toString($uid)})
                        SET u.phone = $phone, u.kyc = $kyc, u.riskScore = toFloat($risk), u.vpa = $phone + '@upibank'
                    """, uid=row['user_id'], phone=row['phone_number'], kyc=row['kyc_status'], risk=row['risk_score'])
            
            self.save_state('last_user_time', df.iloc[-1]['created_at'])

        except Exception as e:
            logger.error(f"❌ User Sync Error: {e}")
            self.gateway_conn = None 

    def sync_transactions(self):
        try:
            self.ensure_conn()
            # Note: Fetch only SUCCESS transactions
            query = f"SELECT global_txn_id, payer_vpa, payee_vpa, amount, created_at FROM transactions WHERE created_at > '{self.state['last_txn_time']}' AND status = 'SUCCESS' ORDER BY created_at ASC LIMIT 1000"
            
            df = pd.read_sql(query, self.switch_conn)
            if df.empty: return

            logger.info(f"🔄 Syncing {len(df)} New Txns...")
            with self.driver.session() as session:
                for _, row in df.iterrows():
                    session.run("""
                        MERGE (s:User {vpa: $payer})
                        MERGE (r:User {vpa: $payee})
                        MERGE (s)-[:SENT_MONEY {txnId: $tid, amount: toFloat($amt), ts: $ts}]->(r)
                    """, payer=row['payer_vpa'], payee=row['payee_vpa'], tid=row['global_txn_id'], amt=row['amount'], ts=str(row['created_at']))
            
            self.save_state('last_txn_time', df.iloc[-1]['created_at'])

        except Exception as e:
            logger.error(f"❌ Txn Sync Error: {e}")
            self.switch_conn = None 

# --- INITIALIZATION ---
sync_service = None

@app.on_event("startup")
def startup():
    global sync_service
    sync_service = GraphSyncService()
    logger.info("✅ Graph Sync Engine Ready")

@app.on_event("shutdown")
def shutdown():
    if sync_service:
        if sync_service.driver: sync_service.driver.close()
        if sync_service.gateway_conn: sync_service.gateway_conn.close()
        if sync_service.switch_conn: sync_service.switch_conn.close()

# --- API ENDPOINTS ---

@app.post("/sync/users")
async def trigger_user_sync(background_tasks: BackgroundTasks):
    """Triggered by Gateway when new user registers"""
    if sync_service:
        background_tasks.add_task(sync_service.sync_users)
    return {"status": "Accepted", "message": "User sync queued"}

@app.post("/sync/transactions")
async def trigger_txn_sync(background_tasks: BackgroundTasks):
    """Triggered by Switch when payment succeeds"""
    if sync_service:
        background_tasks.add_task(sync_service.sync_transactions)
    return {"status": "Accepted", "message": "Txn sync queued"}

@app.post("/sync/all")
async def trigger_full_sync(background_tasks: BackgroundTasks):
    """Manual full sync"""
    if sync_service:
        background_tasks.add_task(sync_service.sync_users)
        background_tasks.add_task(sync_service.sync_transactions)
    return {"status": "Accepted"}

@app.get("/health")
def health():
    return {"status": "up"}