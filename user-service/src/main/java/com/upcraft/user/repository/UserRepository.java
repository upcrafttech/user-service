package com.upcraft.user.repository;

import com.upcraft.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByTenantIdAndUsername(UUID tenantId, String username);

    List<User> findByTenantId(UUID tenantId);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.role = :role")
    List<User> findByTenantIdAndRole(@Param("tenantId") UUID tenantId, @Param("role") String role);
}
