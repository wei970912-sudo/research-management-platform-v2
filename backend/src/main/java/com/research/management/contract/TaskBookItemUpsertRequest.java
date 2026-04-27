package com.research.management.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TaskBookItemUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String name,
        @NotNull BigDecimal budget,
        String note
) {
}
