package com.tony.tradinglab.marketdata.client;

import com.tony.tradinglab.marketdata.dto.DailyPrice;

import java.time.LocalDate;
import java.util.List;

public interface MarketDataClient {

    List<DailyPrice> getDailyPrices(
            String symbol,
            LocalDate startDate,
            LocalDate endDate
    );
}