package com.upcraft.user.service;

import com.upcraft.dto.GrowthDTO;
import com.upcraft.dto.PlatformStatsDTO;
import com.upcraft.dto.TierDistributionDTO;
import com.upcraft.user.repository.TenantRepository;
import com.upcraft.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlatformService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public PlatformStatsDTO getPlatformStats() {
        long totalTenants = tenantRepository.count();
        long activeUsers = userRepository.count(); // Simplified
        
        return PlatformStatsDTO.builder()
                .totalTenants(totalTenants)
                .activeUsers(activeUsers)
                .systemUptime("99.99%")
                .mrr(totalTenants * 499.0) // Mock calculation
                .tenantGrowth(Arrays.asList(
                    new PlatformStatsDTO.GrowthDataPoint("Jan", 10.0),
                    new PlatformStatsDTO.GrowthDataPoint("Feb", 15.0),
                    new PlatformStatsDTO.GrowthDataPoint("Mar", 25.0)
                ))
                .build();
    }

    public GrowthDTO getGrowth(String period) {
        // Mock data based on period
        List<String> labels = Arrays.asList("W1", "W2", "W3", "W4");
        List<Double> values = Arrays.asList(5.0, 12.0, 8.0, 20.0);
        
        return GrowthDTO.builder()
                .labels(labels)
                .values(values)
                .build();
    }

    public TierDistributionDTO getTierDistribution() {
        Map<String, Double> dist = new HashMap<>();
        dist.put("ENTERPRISE", 20.0);
        dist.put("PROFESSIONAL", 50.0);
        dist.put("STARTER", 30.0);
        
        return TierDistributionDTO.builder()
                .distribution(dist)
                .build();
    }
}
