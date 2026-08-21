package com.tony.tradinglab.marketdata.client;

import com.tony.tradinglab.marketdata.dto.DailyPrice;
import com.tony.tradinglab.marketdata.dto.TwelveDataResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class TwelveDataClient implements MarketDataClient {

    private final RestClient restClient;
    private final String apiKey;

    public TwelveDataClient(
            @Value("${market-data.twelve-data.base-url}") String baseUrl,
            @Value("${market-data.twelve-data.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.apiKey = apiKey;
    }

    @Override
    public List<DailyPrice> getDailyPrices(
            String symbol,
            LocalDate startDate,
            LocalDate endDate
    ) {

        TwelveDataResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/time_series")
                        .queryParam("symbol", symbol)
                        .queryParam("interval", "1day")
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", endDate)
                        .queryParam("adjust", "all")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .body(TwelveDataResponse.class);

        if (response == null) {
            throw new IllegalStateException("Twelve Data 응답이 없습니다.");
        }

        if (!"ok".equalsIgnoreCase(response.status())) {
            throw new IllegalStateException(
                    "Twelve Data API 오류: " + response.message()
            );
        }

        return response.values()
                .stream()
                .map(this::toDailyPrice)
                .sorted(Comparator.comparing(DailyPrice::tradeDate))
                .toList();
    }

    private DailyPrice toDailyPrice(TwelveDataResponse.Value value) {

        BigDecimal close = new BigDecimal(value.close());

        return new DailyPrice(
                LocalDate.parse(value.datetime().substring(0, 10)),
                new BigDecimal(value.open()),
                new BigDecimal(value.high()),
                new BigDecimal(value.low()),
                close,
                close,
                Long.valueOf(value.volume())
        );
    }
}