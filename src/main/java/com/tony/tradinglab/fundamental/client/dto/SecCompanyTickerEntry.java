package com.tony.tradinglab.fundamental.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SecCompanyTickerEntry(

        @JsonProperty("cik_str")
        Integer cik,

        String ticker,

        String title

) {
}