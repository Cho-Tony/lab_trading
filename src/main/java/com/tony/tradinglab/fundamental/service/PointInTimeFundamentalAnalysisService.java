package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PointInTimeFundamentalAnalysisService {

    private final PointInTimeTtmFinancialsAnalyzer ttmAnalyzer;
    private final PointInTimeGrowthAnalyzer growthAnalyzer;
    private final PointInTimeProfitabilityAnalyzer profitabilityAnalyzer;


    public PointInTimeFundamentalAnalysisService(
            PointInTimeTtmFinancialsAnalyzer ttmAnalyzer,
            PointInTimeGrowthAnalyzer growthAnalyzer,
            PointInTimeProfitabilityAnalyzer profitabilityAnalyzer
    ) {

        this.ttmAnalyzer =
                ttmAnalyzer;

        this.growthAnalyzer =
                growthAnalyzer;

        this.profitabilityAnalyzer =
                profitabilityAnalyzer;
    }


    public Optional<PointInTimeFundamentalAnalysis> analyze(
            PointInTimeFundamentalContext context,
            List<QuarterlyFact> revenueFacts
    ) {

        if (context == null) {

            return Optional.empty();
        }


        Optional<TtmFinancials> ttm =
                ttmAnalyzer.analyze(
                        context
                );


        /*
         * Valuation의 핵심 재료이므로
         * TTM이 없으면 분석 자체를 만들지 않는다.
         */
        if (ttm.isEmpty()) {

            return Optional.empty();
        }


        /*
         * Growth / Profitability는 일부 종목에서
         * 데이터 부족으로 계산되지 않을 수 있다.
         *
         * 그렇다고 Percentile / Robust Z까지
         * 못 쓰게 할 필요는 없으므로 null 허용.
         */
        GrowthTrendAnalysis growth =
                growthAnalyzer.analyze(

                                revenueFacts,

                                context.asOfDate()
                        )

                        .orElse(null);


        ProfitabilityTrendAnalysis profitability =
                profitabilityAnalyzer.analyze(
                                context
                        )

                        .orElse(null);


        return Optional.of(
                new PointInTimeFundamentalAnalysis(

                        context.stockId(),

                        context.symbol(),

                        context.asOfDate(),

                        ttm.get(),

                        growth,

                        profitability
                )
        );
    }
}