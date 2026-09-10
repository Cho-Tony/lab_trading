package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.domain.SharesOutstandingSnapshot;
import com.tony.tradinglab.fundamental.domain.TtmCashFlow;
import com.tony.tradinglab.fundamental.domain.TtmFinancials;
import com.tony.tradinglab.fundamental.domain.ValuationMetrics;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class PointInTimeValuationAssembler {

    private final SharesOutstandingResolver sharesOutstandingResolver;
    private final ValuationMetricsCalculator valuationMetricsCalculator;

    public PointInTimeValuationAssembler(
            SharesOutstandingResolver sharesOutstandingResolver,
            ValuationMetricsCalculator valuationMetricsCalculator
    ) {
        this.sharesOutstandingResolver =
                sharesOutstandingResolver;

        this.valuationMetricsCalculator =
                valuationMetricsCalculator;
    }


    public Optional<ValuationMetrics> assemble(
            LocalDate priceDate,
            BigDecimal sharePrice,
            List<TtmFinancials> financials,
            List<TtmCashFlow> cashFlows,
            List<SecFactPoint> sharesFacts
    ) {

        /*
         * 1. 해당 날짜에 공개되어 있던
         * 가장 최신 TTM 재무정보
         */
        Optional<TtmFinancials> financialsOptional =
                findLatestAvailableFinancials(
                        financials,
                        priceDate
                );

        if (financialsOptional.isEmpty()) {
            return Optional.empty();
        }


        TtmFinancials latestFinancials =
                financialsOptional.orElseThrow();


        /*
         * 2. 당시 공개된 발행주식수
         */
        Optional<SharesOutstandingSnapshot> sharesOptional =
                sharesOutstandingResolver.resolve(
                        sharesFacts,
                        priceDate
                );

        if (sharesOptional.isEmpty()) {
            return Optional.empty();
        }


        SharesOutstandingSnapshot shares =
                sharesOptional.orElseThrow();


        /*
         * 3. Financials와 동일한 FY/Q의
         * TTM Cash Flow를 찾는다.
         *
         * 일부 기업에서 Cash Flow 데이터가 없더라도
         * P/E, P/S는 계산할 수 있으므로
         * Cash Flow는 nullable.
         */
        TtmCashFlow matchingCashFlow =
                findMatchingCashFlow(
                        cashFlows,
                        latestFinancials,
                        priceDate
                )
                        .orElse(null);


        /*
         * 4. 최종 Valuation 계산
         */
        return Optional.of(
                valuationMetricsCalculator.calculate(

                        latestFinancials,
                        matchingCashFlow,

                        priceDate,
                        sharePrice,

                        shares.sharesOutstanding()
                )
        );
    }


    private Optional<TtmFinancials> findLatestAvailableFinancials(
            List<TtmFinancials> financials,
            LocalDate asOfDate
    ) {

        return financials.stream()

                /*
                 * filedDate가 없는 데이터는
                 * PIT 백테스트에서 사용하지 않는다.
                 */
                .filter(
                        financial ->
                                financial.filedDate() != null
                )

                /*
                 * 미래에 공개된 데이터 제거
                 */
                .filter(
                        financial ->
                                !financial.filedDate()
                                        .isAfter(asOfDate)
                )

                /*
                 * 가장 최신 회계분기 선택
                 */
                .max(
                        Comparator
                                .comparing(
                                        TtmFinancials::fiscalYear
                                )
                                .thenComparingInt(
                                        financial ->
                                                quarterNumber(
                                                        financial.fiscalQuarter()
                                                )
                                )
                );
    }


    private Optional<TtmCashFlow> findMatchingCashFlow(
            List<TtmCashFlow> cashFlows,
            TtmFinancials financials,
            LocalDate asOfDate
    ) {

        return cashFlows.stream()

                .filter(
                        cashFlow ->
                                cashFlow.filedDate() != null
                )

                .filter(
                        cashFlow ->
                                !cashFlow.filedDate()
                                        .isAfter(asOfDate)
                )

                .filter(
                        cashFlow ->
                                cashFlow.fiscalYear()
                                        .equals(
                                                financials.fiscalYear()
                                        )
                )

                .filter(
                        cashFlow ->
                                cashFlow.fiscalQuarter()
                                        .equals(
                                                financials.fiscalQuarter()
                                        )
                )

                /*
                 * 동일 FY/Q가 여러 개라면
                 * asOfDate 당시 가장 최근 filing 사용.
                 */
                .max(
                        Comparator.comparing(
                                TtmCashFlow::filedDate
                        )
                );
    }


    private int quarterNumber(
            String fiscalQuarter
    ) {

        return switch (fiscalQuarter) {

            case "Q1" -> 1;
            case "Q2" -> 2;
            case "Q3" -> 3;
            case "Q4" -> 4;

            default ->
                    throw new IllegalArgumentException(
                            "Unknown fiscal quarter: "
                                    + fiscalQuarter
                    );
        };
    }
}