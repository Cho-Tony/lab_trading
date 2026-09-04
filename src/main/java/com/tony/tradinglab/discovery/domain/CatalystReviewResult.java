package com.tony.tradinglab.discovery.domain;

import java.time.LocalDateTime;
import java.util.List;

public record CatalystReviewResult(

        Long stockId,

        String symbol,

        List<CatalystEvidence> evidences,

        LocalDateTime reviewedAt

) {
}