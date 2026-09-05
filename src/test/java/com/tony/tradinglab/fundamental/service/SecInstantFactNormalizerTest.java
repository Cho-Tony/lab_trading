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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecInstantFactNormalizerTest {

    @Autowired
    private SecCompanyFactsClient companyFactsClient;

    @Autowired
    private SecFactExtractor factExtractor;

    @Autowired
    private SecInstantFactNormalizer instantFactNormalizer;

    @Test
    void AnormalizeAaplBalanceSheetFacts() {

        SecCompanyFactsResponse response =
                companyFactsClient.getCompanyFacts("AAPL");


        List<SecFactPoint> cashFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "CashAndCashEquivalentsAtCarryingValue",
                                "CashCashEquivalentsRestrictedCashAndRestrictedCashEquivalents"
                        ),
                        "USD"
                );


        List<SecFactPoint> assetFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "Assets"
                        ),
                        "USD"
                );


        List<SecFactPoint> equityFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "StockholdersEquity",
                                "StockholdersEquityIncludingPortionAttributableToNoncontrollingInterest"
                        ),
                        "USD"
                );


        List<QuarterlyFact> cash =
                instantFactNormalizer.normalize(
                        cashFacts
                );

        List<QuarterlyFact> assets =
                instantFactNormalizer.normalize(
                        assetFacts
                );

        List<QuarterlyFact> equity =
                instantFactNormalizer.normalize(
                        equityFacts
                );


        assertThat(cash).isNotEmpty();
        assertThat(assets).isNotEmpty();
        assertThat(equity).isNotEmpty();


        System.out.println();
        System.out.println(
                "========== Cash =========="
        );

        printRecent(cash);


        System.out.println();
        System.out.println(
                "========== Assets =========="
        );

        printRecent(assets);


        System.out.println();
        System.out.println(
                "========== Equity =========="
        );

        printRecent(equity);
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