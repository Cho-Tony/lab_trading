package com.tony.tradinglab.discovery.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalystReviewResultTest {

    @Test
    void createCatalystReviewResult() {

        CatalystEvidence capacityEvidence =
                new CatalystEvidence(

                        CatalystType.CAPACITY_EXPANSION,

                        CatalystEvidenceStatus.VERIFIED,

                        EvidenceQuality.COMPANY_OFFICIAL,

                        "AI Data Center Expansion",

                        "Production capacity will double.",

                        "Company Investor Relations",

                        "https://example.com/ir",

                        LocalDate.of(
                                2026,
                                8,
                                20
                        )
                );


        CatalystEvidence contractEvidence =
                new CatalystEvidence(

                        CatalystType.LARGE_CONTRACT,

                        CatalystEvidenceStatus.VERIFIED,

                        EvidenceQuality.SEC_OR_GOVERNMENT,

                        "Large Customer Contract",

                        "A material customer contract was disclosed.",

                        "SEC",

                        "https://example.com/sec",

                        LocalDate.of(
                                2026,
                                8,
                                25
                        )
                );


        CatalystReviewResult result =
                new CatalystReviewResult(

                        1L,

                        "TEST",

                        List.of(
                                capacityEvidence,
                                contractEvidence
                        ),

                        LocalDateTime.of(
                                2026,
                                9,
                                4,
                                20,
                                0
                        )
                );


        assertThat(
                result.evidences()
        ).hasSize(2);


        assertThat(
                result.evidences()
        ).extracting(
                CatalystEvidence::catalystType
        ).containsExactly(

                CatalystType.CAPACITY_EXPANSION,
                CatalystType.LARGE_CONTRACT
        );


        assertThat(
                contractEvidence.evidenceQuality().score()
        ).isEqualTo(5);
    }
}