package org.example;

import java.io.File;

public class PathCheck {
    public static void main(String[] args) {
        // This is the path you pasted
        String path = "C:\\Users\\ASUS\\IdeaProjects\\LedgerZero_EX\\ledger-switch\\src\\main\\resources\\rl_fraud_policy_v2.onnx";

        System.out.println("🔍 DIAGNOSTIC CHECK");
        System.out.println("------------------------------------------------");
        System.out.println("Target: " + path);

        File f = new File(path);
        if (f.exists()) {
            System.out.println("✅ FOUND! File exists.");
            System.out.println("📦 Size: " + f.length() + " bytes");
        } else {
            System.out.println("❌ NOT FOUND.");
            System.out.println("   Troubleshooting:");
            System.out.println("   1. Copy the path from File Explorer again.");
            System.out.println("   2. Make sure the filename is exactly 'rl_fraud_policy_v2.onnx' (check for double extensions like .onnx.onnx)");
        }
    }
}