package org.example.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;

@Service
public class RLDecisionEngine {

    private static final Logger log = LoggerFactory.getLogger(RLDecisionEngine.class);
    // ✅ Make sure this matches your NEW filename exactly
    private static final String MODEL_FILENAME = "rl_fraud_policy_v2_packed.onnx";

    private OrtEnvironment env;
    private OrtSession session;

    @PostConstruct
    public void init() {
        try {
            log.info("🤖 STARTING RL ENGINE (Packed File Loader)...");

            // 1. Load from Classpath (Standard robust way)
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(MODEL_FILENAME)) {

                if (is == null) {
                    log.error("❌ FILE NOT FOUND: src/main/resources/{}", MODEL_FILENAME);
                    return;
                }

                // 2. Read into memory
                byte[] modelBytes = is.readAllBytes();
                log.info("📦 Model Size: {} bytes", modelBytes.length);

                // 3. Corruption Check
                if (modelBytes.length < 100) {
                    log.error("❌ FILE CORRUPTED. Size is too small. Check Maven filtering.");
                    return;
                }

                // 4. Initialize AI
                this.env = OrtEnvironment.getEnvironment();
                this.session = env.createSession(modelBytes, new OrtSession.SessionOptions());
                log.info("✅✅✅ RL ENGINE STARTED SUCCESSFULLY! ✅✅✅");
            }

        } catch (Exception e) {
            log.error("❌ Failed to init RL Agent", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (session != null) try { session.close(); } catch (Exception e) {}
        if (env != null) try { env.close(); } catch (Exception e) {}
    }

    public RLAction decide(float gnnRisk, float amount, float oldRisk, float timeDelta) {
        if (session == null) {
            // Warn occasionally
            if (Math.random() < 0.1) log.warn("⚠️ RL Engine DOWN. Using Fallback.");
            return RLAction.CHALLENGE;
        }

        try {
            float[] inputData = new float[]{gnnRisk, amount, oldRisk, timeDelta};
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), new long[]{1, 4});
            OrtSession.Result result = session.run(Collections.singletonMap("state", inputTensor));
            float[][] logits = (float[][]) result.get(0).getValue();

            int bestAction = argmax(logits[0]);
            RLAction action = RLAction.values()[bestAction];

            log.info("🤖 RL DECISION: [R={:.2f}, A={:.2f}] -> {}", gnnRisk, amount, action);
            return action;

        } catch (Exception e) {
            log.error("Inference Error", e);
            return RLAction.CHALLENGE;
        }
    }

    private int argmax(float[] array) {
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIdx]) maxIdx = i;
        }
        return maxIdx;
    }

    public enum RLAction { ALLOW, CHALLENGE, BLOCK }
}