package com.research.management.contract;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ContractUpsertRequest(
        @NotBlank String projectId,
        @NotBlank String taskBookItemId,
        @NotBlank String name,
        @NotNull BigDecimal amount,
        @NotBlank String status,
        @NotBlank String signingUnit,
        @NotBlank String signingDate,
        @NotBlank String deliveryDate,
        @NotBlank String content,
        @NotBlank String deliveryTerms,
        @NotBlank String paymentTerms
) {
}
