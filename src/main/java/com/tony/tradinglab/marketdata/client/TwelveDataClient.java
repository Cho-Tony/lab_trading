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

    private final long minRequestIntervalMillis;


    private final Object rateLimitLock =
            new Object();

    private long lastRequestTimeMillis = 0L;


    public TwelveDataClient(
            @Value("${market-data.twelve-data.base-url}")
            String baseUrl,

            @Value("${market-data.twelve-data.api-key}")
            String apiKey,

            @Value("${market-data.twelve-data.min-request-interval-ms:8000}")
            long minRequestIntervalMillis
    ) {

        this.restClient =
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .build();

        this.apiKey = apiKey;

        this.minRequestIntervalMillis =
                minRequestIntervalMillis;
    }


    @Override
    public List<DailyPrice> getDailyPrices(
            String symbol,
            LocalDate startDate,
            LocalDate endDate
    ) {

        waitForRateLimit();


        TwelveDataResponse response =
                restClient.get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path("/time_series")
                                                .queryParam(
                                                        "symbol",
                                                        symbol
                                                )
                                                .queryParam(
                                                        "interval",
                                                        "1day"
                                                )
                                                .queryParam(
                                                        "start_date",
                                                        startDate
                                                )
                                                .queryParam(
                                                        "end_date",
                                                        endDate
                                                )
                                                .queryParam(
                                                        "adjust",
                                                        "all"
                                                )
                                                .queryParam(
                                                        "apikey",
                                                        apiKey
                                                )
                                                .build()
                        )
                        .retrieve()
                        .body(
                                TwelveDataResponse.class
                        );


        if (response == null) {

            throw new IllegalStateException(
                    "Twelve Data 응답이 없습니다."
            );
        }


        if (!"ok".equalsIgnoreCase(
                response.status()
        )) {

            throw new IllegalStateException(
                    "Twelve Data API 오류: "
                            + response.message()
            );
        }


        if (response.values() == null
                || response.values().isEmpty()) {

            return List.of();
        }


        return response.values()
                .stream()
                .map(
                        this::toDailyPrice
                )
                .sorted(
                        Comparator.comparing(
                                DailyPrice::tradeDate
                        )
                )
                .toList();
    }


    private void  waitForRateLimit() {

        synchronized (rateLimitLock) {

            long now =
                    System.currentTimeMillis();

            long elapsed =
                    now - lastRequestTimeMillis;

            long waitMillis =
                    minRequestIntervalMillis
                            - elapsed;


            if (lastRequestTimeMillis != 0L
                    && waitMillis > 0) {

                try {

                    Thread.sleep(
                            waitMillis
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread()
                            .interrupt();

                    throw new IllegalStateException(
                            "Twelve Data rate limit 대기 중 interrupt 발생",
                            e
                    );
                }
            }


            lastRequestTimeMillis =
                    System.currentTimeMillis();
        }
    }


    private DailyPrice toDailyPrice(
            TwelveDataResponse.Value value
    ) {

        BigDecimal close =
                new BigDecimal(
                        value.close()
                );


        return new DailyPrice(
                LocalDate.parse(
                        value.datetime()
                                .substring(
                                        0,
                                        10
                                )
                ),
                new BigDecimal(
                        value.open()
                ),
                new BigDecimal(
                        value.high()
                ),
                new BigDecimal(
                        value.low()
                ),
                close,
                close,
                Long.valueOf(
                        value.volume()
                )
        );
    }
}