package com.tony.tradinglab.discovery.domain;

public enum EvidenceQuality {

    SEC_OR_GOVERNMENT(5),

    COMPANY_OFFICIAL(4),

    EARNINGS_OR_PRESENTATION(3),

    REPUTABLE_MEDIA(2),

    LOW_QUALITY_SOURCE(1);


    private final int score;


    EvidenceQuality(
            int score
    ) {

        this.score = score;
    }


    public int score() {
        return score;
    }
}