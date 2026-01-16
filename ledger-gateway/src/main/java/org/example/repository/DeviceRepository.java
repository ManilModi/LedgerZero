package org.example.repository;

import org.example.model.User;
import org.example.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserDevice entity operations.
 * Manages trusted devices linked to users.
 */
@Repository
public interface DeviceRepository extends JpaRepository<UserDevice, String> {

    /**
     * Find device by its hardware ID.
     * @param deviceId The unique device identifier
     */
    Optional<UserDevice> findByDeviceId(String deviceId);

    /**
     * Find all devices belonging to a user.
     */
    List<UserDevice> findByUser(User user);

    /**
     * Find all trusted devices for a user.
     */
    List<UserDevice> findByUserAndIsTrusted(User user, boolean isTrusted);

    /**
     * Check if a device is already registered.
     */
    boolean existsByDeviceId(String deviceId);

    // -------------------------------------------------------------------------
    // 🛡️ SECURITY & BLOCKING QUERIES
    // -------------------------------------------------------------------------

    /**
     * Option 1: Block by Phone Number (Your existing method)
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserDevice set isTrusted=false where user.phoneNumber= :phoneNumber")
    void updateDeviceByUserPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Option 2: Block by List of User IDs (Required for AI Kill Switch)
     * NOTE: This assumes UserDevice has a relationship 'user' which has a field 'userId'
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserDevice d SET d.isTrusted = false WHERE d.user.userId IN :userIds")
    void blockDevicesForUsers(@Param("userIds") List<String> userIds);

}