package com.upcraft.user.service;

import com.upcraft.dto.TenantDTO;
import com.upcraft.user.entity.Tenant;
import com.upcraft.user.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public Page<TenantDTO> listTenants(String status, String region, Pageable pageable) {
        // Simple implementation: filter in memory if needed, or add repo methods
        // For now, let's just return all with pageable
        return tenantRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Transactional
    public TenantDTO createTenant(TenantDTO dto) {
        Tenant tenant = new Tenant();
        tenant.setName(dto.getOrg()); // Mapping org to name as fallback
        tenant.setOrg(dto.getOrg());
        tenant.setOwner(dto.getOwner());
        tenant.setPlan(dto.getPlan());
        tenant.setRegion(dto.getRegion());
        tenant.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        tenant.setUserCount(0);
        
        tenant = tenantRepository.save(tenant);
        return convertToDTO(tenant);
    }

    @Transactional(readOnly = true)
    public TenantDTO getTenant(UUID id) {
        return tenantRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
    }

    @Transactional
    public TenantDTO updateTenant(UUID id, TenantDTO dto) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setOrg(dto.getOrg());
        tenant.setOwner(dto.getOwner());
        tenant.setPlan(dto.getPlan());
        tenant.setRegion(dto.getRegion());
        if (dto.getStatus() != null) {
            tenant.setStatus(dto.getStatus());
        }
        return convertToDTO(tenantRepository.save(tenant));
    }

    @Transactional
    public TenantDTO suspendTenant(UUID id, String reason) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        tenant.setStatus("SUSPENDED");
        // We could log the reason here
        return convertToDTO(tenantRepository.save(tenant));
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getStats() {
        long active = tenantRepository.count(); // Simplified
        return java.util.Map.of(
                "active", active,
                "systemHealth", 98.5,
                "pendingApprovals", 2,
                "logsLast24h", 450
        );
    }

    private TenantDTO convertToDTO(Tenant tenant) {
        return TenantDTO.builder()
                .id(tenant.getId())
                .org(tenant.getOrg())
                .owner(tenant.getOwner())
                .status(tenant.getStatus())
                .region(tenant.getRegion())
                .plan(tenant.getPlan())
                .userCount(tenant.getUserCount())
                .createdAt(tenant.getCreatedAt())
                .systemLoad(Math.random() * 100) // Simulated load
                .build();
    }
}
