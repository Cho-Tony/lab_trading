package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.SecCompanyFactsClient;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.dto.SecCompanyFactsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecFactExtractorTest {

    @Autowired
    private SecCompanyFactsClient companyFactsClient;

    @Autowired
    private SecFactExtractor factExtractor;

    @Test
    void extractAaplRevenue() {

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

        assertThat(revenues).isNotEmpty();

        revenues.stream()
                .skip(Math.max(0, revenues.size() - 10))
                .forEach(System.out::println);
    }
}