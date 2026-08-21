package com.tony.tradinglab.marketdata.dto;

import java.util.List;

public record TwelveDataResponse(
        List<Value> values,
        String status,
        Integer code,
        String message
) {

    public record Value(
            String datetime,
            String open,
            String high,
            String low,
            String close,
            String volume
    ) {
    }
}