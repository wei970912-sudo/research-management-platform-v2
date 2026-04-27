package com.research.management.contract;

import java.math.BigDecimal;

public record TaskBookItemSummary(
        String id,
        String projectId,
        String projectName,
        String name,
        BigDecimal budget,
        BigDecimal usedAmount,
        BigDecimal remainingAmount,
        int contractCount,
        String note
) {
}
