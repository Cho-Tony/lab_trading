package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.SecCompanyFactsClient;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.client.dto.SecCompanyFactsResponse;
import com.tony.tradinglab.fundamental.sec.SecFactExtractor;
import com.tony.tradinglab.fundamental.sec.SecInstantFactNormalizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SecDebtFactTest {

    @Autowired
    private SecCompanyFactsClient companyFactsClient;

    @Autowired
    private SecFactExtractor factExtractor;

    @Autowired
    private SecInstantFactNormalizer instantFactNormalizer;

    @Test
    void extractAaplDebt() {

        SecCompanyFactsResponse response =
                companyFactsClient.getCompanyFacts("AAPL");


        // 1. 단기 차입금
        List<SecFactPoint> shortTermDebtFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "ShortTermBorrowings",
                                "CommercialPaper"
                        ),
                        "USD"
                );


        // 2. 1년 이내 상환 예정 장기부채
        List<SecFactPoint> currentLongTermDebtFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "LongTermDebtCurrent"
                        ),
                        "USD"
                );


        // 3. 장기부채
        List<SecFactPoint> longTermDebtFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "LongTermDebtNoncurrent"
                        ),
                        "USD"
                );


        List<QuarterlyFact> shortTermDebt =
                instantFactNormalizer.normalize(
                        shortTermDebtFacts
                );

        List<QuarterlyFact> currentLongTermDebt =
                instantFactNormalizer.normalize(
                        currentLongTermDebtFacts
                );

        List<QuarterlyFact> longTermDebt =
                instantFactNormalizer.normalize(
                        longTermDebtFacts
                );


        System.out.println();
        System.out.println(
                "========== Short-Term Debt =========="
        );

        printRecent(shortTermDebt);


        System.out.println();
        System.out.println(
                "========== Current Long-Term Debt =========="
        );

        printRecent(currentLongTermDebt);


        System.out.println();
        System.out.println(
                "========== Long-Term Debt =========="
        );

        printRecent(longTermDebt);
    }

    private void printRecent(
            List<QuarterlyFact> facts
    ) {

        facts.stream()
                .skip(
                        Math.max(
                                0,
                                facts.size() - 12
                        )
                )
                .forEach(fact ->
                        System.out.println(

                                "FY"
                                        + fact.fiscalYear()
                                        + " "
                                        + fact.fiscalQuarter()

                                        + " | value="
                                        + fact.value()

                                        + " | end="
                                        + fact.endDate()

                                        + " | filed="
                                        + fact.filedDate()
                        )
                );
    }
}