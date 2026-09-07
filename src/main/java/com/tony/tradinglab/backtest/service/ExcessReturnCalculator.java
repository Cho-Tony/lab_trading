package com.tony.tradinglab.backtest.service;

import com.tony.tradinglab.backtest.domain.ExcessReturnMetrics;
import com.tony.tradinglab.backtest.domain.ForwardReturnMetrics;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ExcessReturnCalculator {

    private static final int SCALE = 2;

    public ExcessReturnMetrics calculate(
            String benchmarkSymbol,
            ForwardReturnMetrics stockReturns,
            ForwardReturnMetrics benchmarkReturns
    ) {

        if (stockReturns == null) {
            throw new IllegalArgumentException(
                    "Stock returns must not be null."
            );
        }

        if (benchmarkReturns == null) {
            throw new IllegalArgumentException(
                    "Benchmark returns must not be null."
            );
        }


        return new ExcessReturnMetrics(

                benchmarkSymbol,

                stockReturns.return63dPct(),
                benchmarkReturns.return63dPct(),

                subtract(
                        stockReturns.return63dPct(),
                        benchmarkReturns.return63dPct()
                ),

                stockReturns.return126dPct(),
                benchmarkReturns.return126dPct(),

                subtract(
                        stockReturns.return126dPct(),
                        benchmarkReturns.return126dPct()
                ),

                stockReturns.return252dPct(),
                benchmarkReturns.return252dPct(),

                subtract(
                        stockReturns.return252dPct(),
                        benchmarkReturns.return252dPct()
                )
        );
    }


    private BigDecimal subtract(
            BigDecimal stockReturn,
            BigDecimal benchmarkReturn
    ) {

        if (stockReturn == null
                || benchmarkReturn == null) {

            return null;
        }

        return stockReturn
                .subtract(
                        benchmarkReturn
                )
                .setScale(
                        SCALE,
                        RoundingMode.HALF_UP
                );
    }
}