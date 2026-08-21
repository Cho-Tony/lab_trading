package com.tony.tradinglab.fundamental.client;

import com.tony.tradinglab.fundamental.dto.SecCompanyFactsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SecCompanyFactsClient {

    private final RestClient restClient;
    private final SecTickerResolver tickerResolver;
    private final String userAgent;

    public SecCompanyFactsClient(
            @Value("${sec.data-base-url}") String baseUrl,
            @Value("${sec.user-agent}") String userAgent,
            SecTickerResolver tickerResolver
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.userAgent = userAgent;
        this.tickerResolver = tickerResolver;
    }

    public SecCompanyFactsResponse getCompanyFacts(String symbol) {

        String cik = tickerResolver.resolveCik(symbol);

        SecCompanyFactsResponse response = restClient.get()
                .uri(
                        "/api/xbrl/companyfacts/CIK{cik}.json",
                        cik
                )
                .header(HttpHeaders.USER_AGENT, userAgent)
                .retrieve()
                .body(SecCompanyFactsResponse.class);

        if (response == null) {
            throw new IllegalStateException(
                    "SEC Company Facts 응답이 없습니다: " + symbol
            );
        }

        return response;
    }
}