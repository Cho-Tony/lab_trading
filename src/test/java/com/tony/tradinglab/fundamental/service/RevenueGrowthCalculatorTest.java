package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.SecCompanyFactsClient;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.RevenueGrowth;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.dto.SecCompanyFactsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RevenueGrowthCalculatorTest {

    @Autowired
    private SecCompanyFactsClient companyFactsClient;

    @Autowired
    private SecFactExtractor factExtractor;

    @Autowired
    private SecQuarterNormalizer quarterNormalizer;

    @Autowired
    private RevenueGrowthCalculator revenueGrowthCalculator;

    @Test
    void calculateAaplRevenueGrowth() {

        SecCompanyFactsResponse response =
                companyFactsClient.getCompanyFacts("AAPL");

        List<SecFactPoint> revenues =
                factExtractor.extract(
                        response,
                        List.of(
                                "RevenueFromContractWithCustomerExcludingAssessedTax",
                                "Revenues",
                                "SalesRevenueNet"
                        ),
                        "USD"
                );

        List<QuarterlyFact> quarters =
                quarterNormalizer.normalize(revenues);

        List<RevenueGrowth> growths =
                revenueGrowthCalculator.calculate(quarters);

        assertThat(growths).isNotEmpty();

        growths.stream()
                .skip(Math.max(
                        0,
                        growths.size() - 8
                ))
                .forEach(growth ->
                        System.out.println(
                                "FY"
                                        + growth.fiscalYear()
                                        + " "
                                        + growth.fiscalQuarter()
                                        + " | revenue="
                                        + growth.revenue()
                                        + " | previous="
                                        + growth.previousYearRevenue()
                                        + " | YoY="
                                        + growth.yoyGrowthPct()
                                        + "%"
                                        + " | filed="
                                        + growth.filedDate()
                        )
                );
    }
}