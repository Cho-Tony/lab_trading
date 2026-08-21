package com.tony.tradinglab.fundamental.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SecCompanyFactsResponse(

        String cik,
        String entityName,

        Map<String, Map<String, Fact>> facts

) {

    public record Fact(
            String label,
            String description,
            Map<String, List<Unit>> units
    ) {
    }

    public record Unit(
            BigDecimal val,
            String accn,
            Integer fy,
            String fp,
            String form,
            String filed,
            String start,
            String end,
            String frame
    ) {
    }
}