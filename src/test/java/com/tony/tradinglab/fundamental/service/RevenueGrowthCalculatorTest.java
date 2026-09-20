package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.SecCompanyFactsClient;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.RevenueGrowth;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.client.dto.SecCompanyFactsResponse;
import com.tony.tradinglab.fundamental.sec.SecFactExtractor;
import com.tony.tradinglab.fundamental.sec.SecQuarterNormalizer;
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
    }
}