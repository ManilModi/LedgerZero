<div align="center">

# 🏦 LedgerZero

### AI-Powered Real-Time Fraud Detection for Digital Payments

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.11-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://python.org/)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![Neo4j](https://img.shields.io/badge/Neo4j-Graph%20DB-008CC1?style=for-the-badge&logo=neo4j&logoColor=white)](https://neo4j.com/)
[![PyTorch](https://img.shields.io/badge/PyTorch-GNN-EE4C2C?style=for-the-badge&logo=pytorch&logoColor=white)](https://pytorch.org/)

*A production-grade UPI-style payment ecosystem with Graph Neural Networks, GraphRAG forensic analysis, and real-time money laundering detection.*

[Features](#-features) • [Architecture](#-architecture) • [Tech Stack](#-tech-stack)

---

<img src="https://raw.githubusercontent.com/andreasbm/readme/master/assets/lines/rainbow.png" alt="rainbow" width="100%">

</div>

## 🎯 Overview

**LedgerZero** is a full-stack digital payment platform that combines the familiarity of UPI-style transactions with cutting-edge AI/ML fraud detection. Unlike traditional rule-based systems, LedgerZero uses **Graph Neural Networks (GNN)** to analyze transaction patterns across the entire network in real-time, detecting sophisticated fraud schemes like mule networks, layering, and structuring.

### Why LedgerZero?

| Traditional Systems | LedgerZero |
|:---:|:---:|
| Rule-based fraud detection | **AI-powered GNN** learns evolving patterns |
| Individual transaction analysis | **Graph-based** network-wide analysis |
| Manual SAR generation | **GraphRAG** auto-generates forensic reports |
| Siloed architecture | **Microservices** with event-driven design |

---

## ✨ Features

### 💳 Payment System
- **UPI-style Transactions** - Instant P2P and merchant payments via VPA (Virtual Payment Address)
- **QR Code Payments** - Scan-to-pay and dynamic QR generation
- **Multi-Bank Support** - Pluggable bank adapters (Axis, SBI architecture)
- **Real-time Notifications** - WebSocket-powered instant payment alerts

### 🔐 Security & Authentication
- **Multi-Factor Auth** - Phone OTP + 6-digit MPIN verification
- **Device Fingerprinting** - Track and trust user devices across sessions
- **Session Management** - Secure HTTP-only cookie-based JWT tokens
- **Rate Limiting** - Per-device transaction throttling

### 🤖 AI-Powered Fraud Detection
- **Graph Neural Network (GNN)** - PyTorch Geometric model trained on transaction + device topology
- **Real-time Risk Scoring** - Sub-100ms fraud predictions during payment flow
- **Pattern Detection** - Identifies Fan-In, Fan-Out, Layering, and Structuring schemes
- **Shared Device Analysis** - Detects mule networks using common device fingerprints

### 🕵️ GraphRAG Forensic Engine
- **Automated SAR Generation** - AI writes Suspicious Activity Reports using transaction graph context
- **Ego Graph Retrieval** - Fetches 2-hop transaction network for any user
- **LLM-Powered Analysis** - Google Gemini analyzes patterns and provides verdicts
- **Explainable AI** - Clear reasoning for why transactions are flagged

### 📊 Analytics & Visualization
- **Transaction Network Graph** - D3.js-powered interactive visualization
- **Risk Score Dashboards** - Real-time monitoring of system-wide fraud metrics
- **Transaction History** - Detailed audit trail with filters and search

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND (React 19)                            │
│                    Vite • TypeScript • TailwindCSS • Framer Motion          │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │ HTTPS
                                   ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           GATEWAY SERVICE (Spring Boot)                      │
│              Auth • User Management • Payment Initiation • WebSocket         │
│                          Device Trust • Rate Limiting                        │
└──────────────┬──────────────────────────────────────────────┬───────────────┘
               │ REST                                          │ Kafka
               ▼                                               ▼
┌──────────────────────────────┐          ┌───────────────────────────────────┐
│     SWITCH SERVICE           │          │      PYTHON FRAUD ENGINE          │
│  Transaction Routing         │◄────────►│   FastAPI • GNN Model • GraphRAG  │
│  Fraud Score Aggregation     │  REST    │   Neo4j Sync • Forensic Analysis  │
└──────────────┬───────────────┘          └───────────────────────────────────┘
               │ REST                                          │
               ▼                                               ▼
┌──────────────────────────────┐          ┌───────────────────────────────────┐
│      BANK SERVICE            │          │         NEO4J GRAPH DB            │
│  Account Management          │          │   Transaction Network • Users     │
│  Balance • Transactions      │          │   Device Topology • Risk Scores   │
└──────────────┬───────────────┘          └───────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        AWS RDS (PostgreSQL 17)                              │
│               Primary + Read Replica • Encrypted • Performance Insights      │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Module Breakdown

| Module | Description |
|--------|-------------|
| `ledger-gateway` | API Gateway, Auth, User Management, WebSocket |
| `ledger-switch` | Transaction routing, Fraud orchestration |
| `ledger-bank` | Core banking, Accounts, Transactions |
| `ledger-common` | Shared DTOs, Enums, Utilities |
| `Python Backend` | GNN Model, GraphRAG, Neo4j Sync |
| `website` | React Frontend |

---

## 🛠 Tech Stack

### Backend Services
| Technology | Purpose |
|------------|---------|
| **Java 21** | Primary language for microservices |
| **Spring Boot 3.4** | Application framework |
| **Spring Security** | Authentication & authorization |
| **Spring Data JPA** | Database access layer |
| **Apache Kafka** | Event streaming & notifications |
| **WebSocket (STOMP)** | Real-time payment notifications |

### AI/ML Engine
| Technology | Purpose |
|------------|---------|
| **Python 3.11** | ML service runtime |
| **FastAPI** | High-performance API framework |
| **PyTorch Geometric** | Graph Neural Network framework |
| **LangChain** | LLM orchestration for GraphRAG |
| **Google Gemini** | Large Language Model for forensics |
| **ONNX Runtime** | Model inference optimization |

### Data Layer
| Technology | Purpose |
|------------|---------|
| **PostgreSQL 17** | Primary relational database |
| **Neo4j** | Graph database for transaction networks |
| **Redis** | Caching & sync state management |
| **AWS RDS** | Managed database with read replicas |

### Frontend
| Technology | Purpose |
|------------|---------|
| **React 19** | UI framework |
| **TypeScript** | Type-safe development |
| **Vite 7** | Build tool & dev server |
| **TailwindCSS 4** | Utility-first styling |
| **Framer Motion** | Animations & transitions |
| **Zustand** | State management |
| **React Query** | Server state & caching |
| **D3.js** | Transaction graph visualization |

### Infrastructure
| Technology | Purpose |
|------------|---------|
| **Terraform** | Infrastructure as Code |
| **Docker** | Containerization |
| **AWS** | Cloud platform (RDS, EC2) |
| **Vercel** | Frontend deployment |

---

## 📁 Project Structure

```
LedgerZero/
├── 📂 ledger-gateway/          # API Gateway & Auth Service
│   └── src/main/java/org/example/
│       ├── controller/         # REST endpoints
│       ├── service/imp/        # Business logic
│       ├── model/              # JPA entities
│       └── repository/         # Data access
│
├── 📂 ledger-switch/           # Transaction Router
│   └── src/main/java/org/example/
│       ├── client/             # Feign clients
│       └── service/            # Routing logic
│
├── 📂 ledger-bank/             # Core Banking Service
│   └── src/main/java/org/example/
│       ├── service/            # Account & Transaction logic
│       └── config/             # Bank profiles (Axis, SBI)
│
├── 📂 ledger-common/           # Shared Library
│   └── src/main/java/org/example/
│       ├── dto/                # Data Transfer Objects
│       └── enums/              # Status codes, types
│
├── 📂 Python Backend/          # AI/ML Fraud Engine
│   ├── app.py                  # FastAPI main
│   ├── graph_rag.py            # GraphRAG implementation
│   ├── train_gnn_neo4j_v2.py   # GNN training script
│   └── models/                 # Trained models
│
├── 📂 website/                 # React Frontend
│   └── src/
│       ├── pages/              # Route components
│       ├── components/         # Reusable UI
│       ├── hooks/              # Custom hooks
│       ├── services/           # API clients
│       └── store/              # Zustand stores
│
└── 📂 terraform-rds/           # Infrastructure as Code
    ├── master.tf               # Primary RDS
    └── replica.tf              # Read replica
```
---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

### Built with ❤️ by the LedgerZero Team

**[⬆ Back to Top](#-ledgerzero)**

</div>
