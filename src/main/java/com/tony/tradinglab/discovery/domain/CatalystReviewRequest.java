package com.tony.tradinglab.discovery.domain;

import java.util.List;

public record CatalystReviewRequest(

        Long stockId,
        String symbol,

        DiscoverySource discoverySource,

        List<CatalystType> catalystTypes,

        List<CandidateQualityReason> triggerReasons

) {
}