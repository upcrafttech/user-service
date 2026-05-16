package com.upcraft.payroll.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRunStatusDTO {
    private List<Stage> stages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Stage {
        private String name;
        private String date;
        private boolean done;
        private boolean active;
    }
}
