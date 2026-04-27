package com.research.management.foundation;

import java.math.BigDecimal;

public record ResearchContractRecord(
        String id,
        String projectId,
        String taskBookItemId,
        String name,
        BigDecimal amount,
        String status,
        String taskName,
        BigDecimal taskBudget,
        String signingUnit,
        String signingDate,
        String deliveryDate,
        String content,
        String deliveryTerms,
        String paymentTerms
) {
}
