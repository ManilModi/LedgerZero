package org.example.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PaymentRequest;
import org.example.model.SuspiciousEntity;
import org.example.repository.SuspiciousEntityRepository;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.LongStream;

/**
 * Hybrid Fraud Detection Engine
 * Coordinates:
 * 1. Deterministic Rules (Fast Fail)
 * 2. Graph Neural Network (Deep Insight)
 * 3. RL Agent (Strategic Decision)
 * 4. GraphRAG (Forensic Explanation)
 */
@Service
@Slf4j
public class FraudDetectionService {

    @Autowired
    private SuspiciousEntityRepository suspiciousEntityRepository;

    @Autowired
    private Driver neo4jDriver;

    @Autowired
    private RestTemplate restTemplate;

    // ✅ Inject the Reinforcement Learning Engine
    @Autowired
    private RLDecisionEngine rlEngine;

    @Value("${gateway.service.url:http://localhost:8080}")
    private String gatewayUrl;

    @Value("${graphrag.service.url:http://localhost:8000}")
    private String graphRagUrl;

    @Autowired
    private JedisPool redisPool;

    private OrtEnvironment env;
    private OrtSession session;
    private final ObjectMapper mapper = new ObjectMapper();

    // Constants for Normalization
    private static final double MAX_TXN_AMOUNT = 100000.0;
    private static final double RULE_BASED_BLOCK_SCORE = 1.0;

    @PostConstruct
    public void init() {
        try {
            log.info("🔍 STARTING FRAUD ENGINE (Native Loader)...");

            // 1. Initialize Redis FIRST (Critical: Prevents NPE if AI fails)
            try {
                log.info("✅ Redis Pool Initialized");
            } catch (Exception e) {
                log.error("❌ Redis Init Failed: {}", e.getMessage());
            }

            // 2. Initialize GNN AI (Using Native ClassLoader - Bypasses Spring)
            // Look for 'fraud_model_v2.onnx' in src/main/resources
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("fraud_model_v2.onnx")) {

                if (is == null) {
                    log.error("❌ GNN MODEL NOT FOUND via ClassLoader. Check 'src/main/resources/fraud_model_v2.onnx'");
                    return;
                }

                byte[] modelBytes = is.readAllBytes();
                log.info("📦 GNN Model Size: {} bytes", modelBytes.length);

                if (modelBytes.length < 100) {
                    log.error("❌ GNN FILE CORRUPTED (Maven Filtering Issue).");
                    return;
                }

                this.env = OrtEnvironment.getEnvironment();
                this.session = env.createSession(modelBytes, new OrtSession.SessionOptions());
                log.info("✅ GNN Model Loaded Successfully!");

            } catch (Exception e) {
                log.error("❌ Failed to load GNN Model", e);
            }

        } catch (Exception e) {
            log.error("❌ CRITICAL: FraudEngine Init Failed", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) session.close();
            if (env != null) env.close();
            if (redisPool != null) redisPool.close();
        } catch (Exception e) {
            log.warn("Error closing resources", e);
        }
    }

    /**
     * Main Entry Point: Orchestrates the defense layers.
     */
    public double calculateRiskScore(PaymentRequest request) {
        if (request == null) return 0.0;

        // --- LAYER 1: HARD RULES (Blocklist Check) ---
        if (request.getFraudCheckData() != null) {
            String ip = request.getFraudCheckData().getIpAddress();
            String deviceId = request.getFraudCheckData().getDeviceId();

            if (isEntityBlocked(ip) || isEntityBlocked(deviceId)) {
                log.warn("⛔ BLOCKED by Repository: IP/Device is in blocklist");
                return RULE_BASED_BLOCK_SCORE;
            }
        }

        // --- LAYER 2: AI ENGINE (GNN Risk Calculation) ---
        double gnnRisk = 0.0;
        // Check if GNN Session is loaded before running
        if (this.session != null && request.getPayerVpa() != null && request.getPayeeVpa() != null) {
            gnnRisk = runGraphAI(request.getPayerVpa(), request.getPayeeVpa());
        } else if (this.session == null) {
            log.warn("⚠️ GNN Session is NULL (Model missing). Skipping GNN check.");
        }

        // --- LAYER 3: RL AGENT (Strategic Decision) ---
        // 1. Fetch Context from Redis (History & Velocity)
        float oldRisk = 0.1f;    // Default: "Good User"
        float timeDelta = 1.0f;  // Default: "Long gap since last txn"

        if (redisPool != null) {
            try (Jedis redis = redisPool.getResource()) {
                String json = redis.get("user:" + request.getPayerVpa() + ":profile");
                // Parsing logic would go here
            } catch (Exception e) {
                log.debug("Redis context unavailable, using defaults");
            }
        }

        // 2. Normalize Amount
        float normAmount = (float) Math.min(request.getAmount().doubleValue() / MAX_TXN_AMOUNT, 1.0);

        // 3. Ask the RL Agent
        RLDecisionEngine.RLAction action = rlEngine.decide((float)gnnRisk, normAmount, oldRisk, timeDelta);

        // --- ACTION EXECUTION ---
        switch (action) {
            case BLOCK:
                log.warn("⛔ BLOCKED by RL Agent (GNN Score: {:.2f})", gnnRisk);
                triggerGraphRAGInvestigation(request, gnnRisk);
                return 1.0;

            case CHALLENGE:
                log.info("⚠️ CHALLENGED by RL Agent. Triggering Step-Up Auth.");
                return 0.65;

            case ALLOW:
            default:
                if (gnnRisk > 0.90) log.warn("👀 RL Allowed High-Risk Txn (Context Override)");
                return 0.0;
        }
    }

    /**
     * 🕵️‍♂️ Calls Python GraphRAG Service to generate a Forensic Report.
     */
    private void triggerGraphRAGInvestigation(PaymentRequest request, double score) {
        CompletableFuture.runAsync(() -> {
            try {
                String url = graphRagUrl + "/investigate/generate-report";
                Map<String, Object> payload = new HashMap<>();
                payload.put("txnId", request.getTxnId());
                payload.put("payerVpa", request.getPayerVpa());
                payload.put("payeeVpa", request.getPayeeVpa());
                payload.put("amount", request.getAmount());
                payload.put("reason", "RL Agent Blocked. GNN Risk: " + String.format("%.2f", score));

                log.info("📝 Triggering GraphRAG Forensic Report for Txn: {}", request.getTxnId());
                restTemplate.postForObject(url, payload, String.class);

            } catch (Exception e) {
                log.error("❌ Failed to trigger GraphRAG investigation: {}", e.getMessage());
            }
        });
    }

    /**
     * 🧠 GNN Inference Logic
     */
    private double runGraphAI(String userId, String targetUserId) {
        // Safety check for Redis Pool
        if (redisPool == null) return 0.0;

        try (Jedis redis = redisPool.getResource()) {
            List<Long> rawEdges = fetchSubgraphFromNeo4j(userId);
            long sourceNodeId = getNeo4jNodeId(userId);
            long targetNodeId = getNeo4jNodeId(targetUserId);

            rawEdges.add(sourceNodeId); rawEdges.add(targetNodeId);
            rawEdges.add(targetNodeId); rawEdges.add(sourceNodeId);

            Map<Long, Integer> nodeMapping = new HashMap<>();
            List<Long> uniqueNodes = new ArrayList<>();
            nodeMapping.put(sourceNodeId, 0);
            uniqueNodes.add(sourceNodeId);

            int localIndexCounter = 1;
            List<Integer> remappedEdgeIndex = new ArrayList<>();

            for (Long globalId : rawEdges) {
                if (!nodeMapping.containsKey(globalId)) {
                    nodeMapping.put(globalId, localIndexCounter++);
                    uniqueNodes.add(globalId);
                }
                remappedEdgeIndex.add(nodeMapping.get(globalId));
            }

            int numNodes = uniqueNodes.size();
            float[] flattenFeatures = new float[numNodes * 2];

            for (int i = 0; i < numNodes; i++) {
                if (i == 0) {
                    float[] userFeatures = fetchFeaturesFromRedis(redis, userId);
                    flattenFeatures[0] = userFeatures[0];
                    flattenFeatures[1] = userFeatures[1];
                } else {
                    flattenFeatures[i * 2] = 0.0f;
                    flattenFeatures[i * 2 + 1] = 0.0f;
                }
            }

            return runInference(flattenFeatures, numNodes, remappedEdgeIndex);

        } catch (Exception e) {
            log.error("⚠️ AI Engine Failure (Fail-Open): {}", e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    private double runInference(float[] flattenFeatures, int numNodes, List<Integer> remappedEdgeIndex) throws Exception {
        OnnxTensor x = OnnxTensor.createTensor(env, FloatBuffer.wrap(flattenFeatures), new long[]{numNodes, 2});

        int numEdges = remappedEdgeIndex.size() / 2;
        long[] srcs = new long[numEdges];
        long[] dsts = new long[numEdges];

        for (int i = 0; i < numEdges; i++) {
            srcs[i] = remappedEdgeIndex.get(2 * i);
            dsts[i] = remappedEdgeIndex.get(2 * i + 1);
        }

        long[] combined = LongStream.concat(LongStream.of(srcs), LongStream.of(dsts)).toArray();
        OnnxTensor edges = OnnxTensor.createTensor(env, LongBuffer.wrap(combined), new long[]{2, numEdges});

        var inputs = Map.of("x", x, "edge_index", edges);

        try (var results = session.run(inputs)) {
            float[][] output = (float[][]) results.get(0).getValue();
            return output[0][1];
        }
    }

    // --- 🛡️ AUTOMATED KILL SWITCH (Mule Ring Takedown) ---
    public void blockMuleRing(String sourceUserId, String targetUserId) {
        log.warn("🚨 FRAUD CONFIRMED: Initiating Mule Ring Takedown for {} and {}", sourceUserId, targetUserId);

        List<String> muleRing = new ArrayList<>();
        muleRing.add(sourceUserId);
        muleRing.add(targetUserId);

        String query = "MATCH (u:User {userId: $uid})-[*1..2]-(accomplice:User) RETURN DISTINCT accomplice.userId as uid";

        try (Session session = neo4jDriver.session()) {
            Result result = session.run(query, Map.of("uid", sourceUserId));
            while (result.hasNext()) {
                muleRing.add(result.next().get("uid").asString());
            }
        } catch (Exception e) {
            log.error("Failed to trace mule ring in Neo4j", e);
        }

        if (!muleRing.isEmpty()) {
            try {
                String url = gatewayUrl + "/api/internal/block-users";
                Map<String, Object> payload = new HashMap<>();
                payload.put("userIds", muleRing);
                payload.put("reason", "Detected by AI Fraud Engine");

                restTemplate.postForEntity(url, payload, String.class);
                log.info("✅ KILL SWITCH EXECUTED: Gateway blocked {} users.", muleRing.size());
            } catch (Exception e) {
                log.error("❌ FAILED to contact Gateway for blocking: {}", e.getMessage());
            }
        }
    }

    // --- Helper Methods ---

    private float[] fetchFeaturesFromRedis(Jedis redis, String userId) {
        try {
            String json = redis.get("user:" + userId + ":features");
            if (json != null) {
                return mapper.readValue(json, float[].class);
            }
        } catch (Exception e) {
            log.warn("Redis fetch error for user {}", userId);
        }
        return new float[]{0.0f, 1.0f};
    }

    private List<Long> fetchSubgraphFromNeo4j(String userId) {
        List<Long> edges = new ArrayList<>();
        String query = """
            MATCH (u:User {userId: $uid})-[r*1..2]-(n)
            RETURN id(startNode(last(r))) as src, id(endNode(last(r))) as dst
            LIMIT 50
        """;
        try (Session session = neo4jDriver.session()) {
            Result result = session.run(query, Map.of("uid", userId));
            while (result.hasNext()) {
                var rec = result.next();
                edges.add(rec.get("src").asLong());
                edges.add(rec.get("dst").asLong());
            }
        }
        return edges;
    }

    private long getNeo4jNodeId(String userId) {
        try (Session session = neo4jDriver.session()) {
            return session.run("MERGE (u:User {userId: $uid}) RETURN id(u)",
                    Map.of("uid", userId)).single().get(0).asLong();
        }
    }

    @Transactional(readOnly = true)
    public boolean isEntityBlocked(String entityValue) {
        if (entityValue == null) return false;
        Optional<SuspiciousEntity> entity = suspiciousEntityRepository.findByEntityValue(entityValue);
        return entity.isPresent() &&
                entity.get().getBlockedUntil() != null &&
                entity.get().getBlockedUntil().isAfter(LocalDateTime.now());
    }
}