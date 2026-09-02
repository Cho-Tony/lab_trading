package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.SecCompanyFactsClient;
import com.tony.tradinglab.fundamental.domain.QuarterlyBalanceSheet;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.dto.SecCompanyFactsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QuarterlyBalanceSheetAssemblerTest {

    @Autowired
    private SecCompanyFactsClient companyFactsClient;

    @Autowired
    private SecFactExtractor factExtractor;

    @Autowired
    private SecInstantFactNormalizer instantFactNormalizer;

    @Autowired
    private QuarterlyBalanceSheetAssembler balanceSheetAssembler;

    @Test
    void assembleAaplBalanceSheet() {

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


        List<QuarterlyBalanceSheet> result =
                balanceSheetAssembler.assemble(
                        cash,
                        assets,
                        equity
                );


        assertThat(result)
                .isNotEmpty();


        result.stream()
                .skip(
                        Math.max(
                                0,
                                result.size() - 8
                        )
                )
                .forEach(balanceSheet ->
                        System.out.println(

                                "FY"
                                        + balanceSheet.fiscalYear()
                                        + " "
                                        + balanceSheet.fiscalQuarter()

                                        + " | Cash="
                                        + balanceSheet.cash()

                                        + " | Assets="
                                        + balanceSheet.totalAssets()

                                        + " | Equity="
                                        + balanceSheet.totalEquity()

                                        + " | end="
                                        + balanceSheet.periodEndDate()

                                        + " | filed="
                                        + balanceSheet.filedDate()
                        )
                );
    }
}