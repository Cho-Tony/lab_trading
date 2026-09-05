package com.tony.tradinglab.fundamental.sec;

import com.tony.tradinglab.fundamental.client.dto.SecCompanyTickerEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class SecTickerResolver {

    private final RestClient restClient;
    private final String userAgent;

    private Map<String, SecCompanyTickerEntry> tickerMap;

    public SecTickerResolver(
            @Value("${sec.base-url}") String baseUrl,
            @Value("${sec.user-agent}") String userAgent
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.userAgent = userAgent;
    }

    public String resolveCik(String symbol) {

        Map<String, SecCompanyTickerEntry> companies = getTickerMap();

        return companies.values()
                .stream()
                .filter(company ->
                        company.ticker().equalsIgnoreCase(symbol)
                )
                .findFirst()
                .map(company ->
                        String.format("%010d", company.cik())
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "SEC CIK를 찾을 수 없습니다: " + symbol
                        )
                );
    }

    private Map<String, SecCompanyTickerEntry> getTickerMap() {

        if (tickerMap != null) {
            return tickerMap;
        }

        tickerMap = restClient.get()
                .uri("/files/company_tickers.json")
                .header(HttpHeaders.USER_AGENT, userAgent)
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                Map<String, SecCompanyTickerEntry>
                                >() {
                        }
                );

        if (tickerMap == null) {
            throw new IllegalStateException(
                    "SEC ticker 정보를 가져오지 못했습니다."
            );
        }

        return tickerMap;
    }
}