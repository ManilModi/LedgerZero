package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices", indexes = {
        // 🚀 Critical: Prevents table scans when fetching devices by User ID
        @Index(name = "idx_device_user", columnList = "user_id"),

        // 🚀 Critical: Optimizes Fraud Engine queries filtering for trusted/untrusted devices
        @Index(name = "idx_device_trusted", columnList = "is_trusted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice {

    @Id
    @Column(name = "device_id")
    private String deviceId; // Hardware ID

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "model_name")
    private String modelName;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "is_trusted")
    private boolean isTrusted = true;

    @Column(name = "last_login_ip")
    private String lastLoginIp;

    @Column(name = "first_seen_at")
    private LocalDateTime firstSeenAt = LocalDateTime.now();
}