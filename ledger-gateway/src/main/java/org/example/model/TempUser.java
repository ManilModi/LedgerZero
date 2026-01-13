package org.example.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "temp_users",
    indexes = {
        @Index(columnList = "phone_number", unique = true)
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "otp")
    private String otp;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
