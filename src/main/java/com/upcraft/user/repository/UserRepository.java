package com.upcraft.user.repository;

import com.upcraft.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByTenantIdAndUsername(UUID tenantId, String username);

    List<User> findByTenantId(UUID tenantId);

    Page<User> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<User> findByEmail(String email);

    Optional<User> findByTenantIdAndId(UUID tenantId, UUID id);

    boolean existsByTenantIdAndUsername(UUID tenantId, String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.role = :role")
    List<User> findByTenantIdAndRole(@Param("tenantId") UUID tenantId, @Param("role") String role);

    Page<User> findByTenantIdAndRole(UUID tenantId, String role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:isActive IS NULL OR u.isActive = :isActive) " +
            "AND (:search IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findWithFilters(
            @Param("tenantId") UUID tenantId,
            @Param("role") String role,
            @Param("isActive") Boolean isActive,
            @Param("search") String search,
            Pageable pageable);
}
