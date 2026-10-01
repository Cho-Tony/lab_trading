package com.tony.tradinglab.discovery.quant;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuantBacktestSample(

        Long stockId,

        String symbol,

        /*
         * Quant Score를 계산한 기준일
         */
        LocalDate observationDate,

        /*
         * 실제 기준 가격.
         *
         * observationDate가 휴장일일 수도 있으므로
         * 실제 사용된 tradeDate도 따로 보존한다.
         */
        LocalDate entryPriceDate,

        BigDecimal entryPrice,

        /*
         * 당시 계산된 Quant Score.
         *
         * Growth / Quality / Valuation / Total
         * 모두 보존된다.
         */
        QuantScoreBreakdown score,

        /*
         * 3개월 후
         */
        LocalDate threeMonthPriceDate,

        BigDecimal threeMonthPrice,

        BigDecimal threeMonthReturnPct,

        /*
         * 6개월 후
         */
        LocalDate sixMonthPriceDate,

        BigDecimal sixMonthPrice,

        BigDecimal sixMonthReturnPct,

        /*
         * 12개월 후
         */
        LocalDate twelveMonthPriceDate,

        BigDecimal twelveMonthPrice,

        BigDecimal twelveMonthReturnPct

) {
}