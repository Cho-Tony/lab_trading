package com.tony.tradinglab.discovery.domain;

import java.time.LocalDate;

public record CatalystEvidence(

        CatalystType catalystType,

        CatalystEvidenceStatus status,

        EvidenceQuality evidenceQuality,

        String title,

        String summary,

        String sourceName,

        String sourceUrl,

        LocalDate publishedDate

) {
}