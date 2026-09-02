package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.CandidateQualityAssessment;
import com.tony.tradinglab.discovery.domain.CandidateQualityDecision;
import com.tony.tradinglab.discovery.domain.CandidateQualityReason;
import com.tony.tradinglab.fundamental.domain.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CandidateQualityGate {

    private static final BigDecimal SHORT_RUNWAY_YEARS =
            new BigDecimal("1.00");

    public CandidateQualityAssessment evaluate(
            GrowthTrendAnalysis revenueGrowth,
            ProfitabilityTrendAnalysis profitability,
            FreeCashFlowTrendAnalysis freeCashFlow,
            FinancialHealthAnalysis financialHealth,
            LiquidityMetrics liquidity,
            CapitalInvestmentMetrics capitalInvestment
    ) {

        List<CandidateQualityReason> reasons =
                new ArrayList<>();

        int positiveSignals = 0;
        int riskSignals = 0;


        /*
         * 1. Revenue
         */

        if (revenueGrowth != null) {

            if (revenueGrowth.trend()
                    == GrowthTrend.ACCELERATING) {

                positiveSignals++;

                reasons.add(
                        CandidateQualityReason
                                .REVENUE_ACCELERATING
                );

            } else if (
                    revenueGrowth.trend()
                            == GrowthTrend.DECELERATING
            ) {

                riskSignals++;

                reasons.add(
                        CandidateQualityReason
                                .REVENUE_DECELERATING
                );
            }
        }


        /*
         * 2. Profitability
         */

        if (profitability != null) {

            boolean improving =
                    profitability.operatingMarginDirection()
                            == ProfitabilityDirection.IMPROVING

                            || profitability.netMarginDirection()
                            == ProfitabilityDirection.IMPROVING;

            boolean deteriorating =
                    profitability.operatingMarginDirection()
                            == ProfitabilityDirection.DETERIORATING

                            && profitability.netMarginDirection()
                            == ProfitabilityDirection.DETERIORATING;

            if (improving) {

                positiveSignals++;

                reasons.add(
                        CandidateQualityReason
                                .PROFITABILITY_IMPROVING
                );
            }

            if (deteriorating) {

                riskSignals++;

                reasons.add(
                        CandidateQualityReason
                                .PROFITABILITY_DETERIORATING
                );
            }
        }


        /*
         * 3. Free Cash Flow
         */

        if (freeCashFlow != null) {

            switch (freeCashFlow.direction()) {

                case IMPROVING -> {

                    positiveSignals++;

                    reasons.add(
                            CandidateQualityReason
                                    .FCF_IMPROVING
                    );
                }

                case TURNAROUND -> {

                    positiveSignals += 2;

                    reasons.add(
                            CandidateQualityReason
                                    .FCF_TURNAROUND
                    );
                }

                case DETERIORATING -> {

                    riskSignals++;

                    reasons.add(
                            CandidateQualityReason
                                    .FCF_DETERIORATING
                    );
                }

                case PERSISTENT_NEGATIVE -> {

                    riskSignals++;

                    reasons.add(
                            CandidateQualityReason
                                    .PERSISTENT_NEGATIVE_FCF
                    );
                }

                default -> {
                    // STABLE / MIXED / INSUFFICIENT_DATA
                }
            }
        }


        /*
         * 4. Financial Health
         */

        if (financialHealth != null) {

            switch (financialHealth.debtBurdenLevel()) {

                case NET_CASH -> {

                    positiveSignals++;

                    reasons.add(
                            CandidateQualityReason
                                    .NET_CASH
                    );
                }

                case LOW -> {

                    positiveSignals++;

                    reasons.add(
                            CandidateQualityReason
                                    .LOW_DEBT_BURDEN
                    );
                }

                case HIGH -> {

                    riskSignals++;

                    reasons.add(
                            CandidateQualityReason
                                    .HIGH_DEBT_BURDEN
                    );
                }

                case VERY_HIGH -> {

                    riskSignals += 2;

                    reasons.add(
                            CandidateQualityReason
                                    .VERY_HIGH_DEBT_BURDEN
                    );
                }

                default -> {
                    // MODERATE / UNASSESSABLE
                }
            }


            if (financialHealth.debtContextSignal()
                    == DebtContextSignal.GROWTH_INVESTMENT_CANDIDATE) {

                reasons.add(
                        CandidateQualityReason
                                .GROWTH_INVESTMENT_CANDIDATE
                );
            }


            if (financialHealth.debtContextSignal()
                    == DebtContextSignal.RISK_WARNING) {

                riskSignals++;

                reasons.add(
                        CandidateQualityReason
                                .DEBT_RISK_WARNING
                );
            }
        }


        /*
         * 5. Liquidity
         */

        if (liquidity != null) {

            if (!liquidity.operatingCashFlowPositive()) {

                riskSignals++;

                reasons.add(
                        CandidateQualityReason
                                .NEGATIVE_OPERATING_CASH_FLOW
                );
            }


            if (liquidity.cashRunwayYears() != null
                    && liquidity.cashRunwayYears()
                    .compareTo(
                            SHORT_RUNWAY_YEARS
                    ) < 0) {

                riskSignals += 2;

                reasons.add(
                        CandidateQualityReason
                                .SHORT_CASH_RUNWAY
                );
            }


            if (liquidity.capexDrivenNegativeFcf()) {

                reasons.add(
                        CandidateQualityReason
                                .CAPEX_DRIVEN_NEGATIVE_FCF
                );
            }
        }


        /*
         * 6. Capital Investment
         */

        boolean capexExpanding =
                capitalInvestment != null
                        && capitalInvestment.capexGrowthYoYPct() != null
                        && capitalInvestment.capexGrowthYoYPct()
                        .compareTo(
                                new BigDecimal("20.00")
                        ) >= 0;

        if (capexExpanding) {

            reasons.add(
                    CandidateQualityReason
                            .CAPEX_EXPANDING
            );
        }


        /*
         * 7. 성장투자 / 턴어라운드 예외
         */

        boolean growthInvestmentException =
                financialHealth != null
                        && financialHealth.debtContextSignal()
                        == DebtContextSignal
                        .GROWTH_INVESTMENT_CANDIDATE;

        boolean turnaroundException =
                freeCashFlow != null
                        && freeCashFlow.direction()
                        == FreeCashFlowDirection.TURNAROUND;

        boolean investmentReviewException =
                liquidity != null
                        && liquidity.capexDrivenNegativeFcf()
                        && capexExpanding;


        /*
         * 8. 최종 Gate
         */

        CandidateQualityDecision decision;


        /*
         * 여러 강한 위험 신호가 동시에 존재하고
         * 성장/턴어라운드 예외도 없다면 REJECT.
         */
        if (riskSignals >= 4
                && !growthInvestmentException
                && !turnaroundException
                && !investmentReviewException) {

            decision =
                    CandidateQualityDecision.REJECT;


            /*
             * 정량적으로 건강하거나 개선 신호가 충분하고
             * 위험 신호가 적다면 PASS.
             */
        } else if (
                positiveSignals >= 2
                        && riskSignals <= 1
                        && !growthInvestmentException
                        && !investmentReviewException
        ) {

            decision =
                    CandidateQualityDecision.PASS;


            /*
             * 나머지는 AI / Catalyst 확인 가치가 있는 REVIEW.
             */
        } else {

            decision =
                    CandidateQualityDecision.REVIEW;
        }


        return new CandidateQualityAssessment(
                decision,
                List.copyOf(reasons),
                positiveSignals,
                riskSignals
        );
    }
}