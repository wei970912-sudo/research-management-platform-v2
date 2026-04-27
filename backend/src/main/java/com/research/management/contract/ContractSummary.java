package com.research.management.contract;

import java.math.BigDecimal;

public record ContractSummary(
        String id,
        String projectId,
        String name,
        String projectName,
        String taskBookItemId,
        BigDecimal amount,
        String status,
        String taskName,
        BigDecimal taskBudget,
        BigDecimal usedAmount,
        BigDecimal remainingAmount,
        String signingUnit,
        String signingDate,
        String deliveryDate,
        String content,
        String deliveryTerms,
        String paymentTerms
) {
}
