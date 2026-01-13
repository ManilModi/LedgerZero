package org.example.repository;

import org.example.model.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempUserRepo extends JpaRepository<TempUser, Long> {
    TempUser findByPhoneNumber(String phoneNumber);
}
