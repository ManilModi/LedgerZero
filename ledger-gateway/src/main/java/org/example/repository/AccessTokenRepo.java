package org.example.repository;

import org.example.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessTokenRepo extends JpaRepository<AccessToken, Long> {
    AccessToken findByToken(String token);
}
