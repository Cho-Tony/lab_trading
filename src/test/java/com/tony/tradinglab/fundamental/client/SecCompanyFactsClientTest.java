package com.tony.tradinglab.fundamental.client;

import com.tony.tradinglab.fundamental.client.dto.SecCompanyFactsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecCompanyFactsClientTest {

    @Autowired
    private SecCompanyFactsClient secCompanyFactsClient;

    @Test
    void getAaplCompanyFacts() {

        SecCompanyFactsResponse response =
                secCompanyFactsClient.getCompanyFacts("AAPL");

        System.out.println("CIK = " + response.cik());
        System.out.println("Company = " + response.entityName());

        System.out.println(
                "Taxonomies = " + response.facts().keySet()
        );

        System.out.println(
                "US-GAAP fact count = "
                        + response.facts()
                        .get("us-gaap")
                        .size()
        );

        assertThat(response.entityName()).isNotBlank();
        assertThat(response.facts()).containsKey("us-gaap");
        assertThat(response.facts().get("us-gaap"))
                .containsKey("NetIncomeLoss");
    }
}