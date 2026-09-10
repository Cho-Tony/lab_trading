package com.tony.tradinglab.fundamental.client;

import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.client.dto.SecCompanyFactsResponse;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.sec.SecCashFlowQuarterNormalizer;
import com.tony.tradinglab.fundamental.sec.SecFactExtractor;
import com.tony.tradinglab.fundamental.sec.SecInstantFactNormalizer;
import com.tony.tradinglab.fundamental.sec.SecQuarterNormalizer;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SecFundamentalDataClient
        implements FundamentalDataClient {

    private static final List<String> REVENUE_TAGS =
            List.of(
                    "RevenueFromContractWithCustomerExcludingAssessedTax",
                    "SalesRevenueNet",
                    "Revenues"
            );


    private static final List<String> OPERATING_INCOME_TAGS =
            List.of(
                    "OperatingIncomeLoss"
            );


    private static final List<String> NET_INCOME_TAGS =
            List.of(
                    "NetIncomeLoss",
                    "ProfitLoss"
            );


    private static final List<String> ASSET_TAGS =
            List.of(
                    "Assets"
            );


    private static final List<String> EQUITY_TAGS =
            List.of(
                    "StockholdersEquity",
                    "StockholdersEquityIncludingPortionAttributableToNoncontrollingInterest"
            );


    private static final List<String> DEBT_TAGS =
            List.of(
                    "LongTermDebtAndFinanceLeaseObligations",
                    "LongTermDebtAndCapitalLeaseObligations",
                    "LongTermDebt"
            );


    private static final List<String> CASH_TAGS =
            List.of(
                    "CashAndCashEquivalentsAtCarryingValue"
            );


    private static final List<String> OCF_TAGS =
            List.of(
                    "NetCashProvidedByUsedInOperatingActivities"
            );


    private static final List<String> CAPEX_TAGS =
            List.of(
                    "PaymentsToAcquirePropertyPlantAndEquipment"
            );


    private final SecCompanyFactsClient companyFactsClient;

    private final SecFactExtractor factExtractor;

    private final SecQuarterNormalizer quarterNormalizer;

    private final SecCashFlowQuarterNormalizer cashFlowNormalizer;

    private final SecInstantFactNormalizer instantFactNormalizer;


    public SecFundamentalDataClient(
            SecCompanyFactsClient companyFactsClient,
            SecFactExtractor factExtractor,
            SecQuarterNormalizer quarterNormalizer,
            SecCashFlowQuarterNormalizer cashFlowNormalizer,
            SecInstantFactNormalizer instantFactNormalizer
    ) {

        this.companyFactsClient =
                companyFactsClient;

        this.factExtractor =
                factExtractor;

        this.quarterNormalizer =
                quarterNormalizer;

        this.cashFlowNormalizer =
                cashFlowNormalizer;

        this.instantFactNormalizer =
                instantFactNormalizer;
    }


    @Override
    public List<FinancialStatementData> getFinancialStatements(
            String symbol
    ) {

        SecCompanyFactsResponse response =
                companyFactsClient.getCompanyFacts(
                        symbol
                );


        List<QuarterlyFact> revenues =
                quarterNormalizer.normalize(
                        factExtractor.extract(
                                response,
                                REVENUE_TAGS,
                                "USD"
                        )
                );


        List<QuarterlyFact> operatingIncomes =
                quarterNormalizer.normalize(
                        factExtractor.extract(
                                response,
                                OPERATING_INCOME_TAGS,
                                "USD"
                        )
                );


        List<QuarterlyFact> netIncomes =
                quarterNormalizer.normalize(
                        factExtractor.extract(
                                response,
                                NET_INCOME_TAGS,
                                "USD"
                        )
                );


        List<QuarterlyFact> assets =
                instantFactNormalizer.normalize(
                        factExtractor.extract(
                                response,
                                ASSET_TAGS,
                                "USD"
                        )
                );


        List<QuarterlyFact> equities =
                instantFactNormalizer.normalize(
                        factExtractor.extract(
                                response,
                                EQUITY_TAGS,
                                "USD"
                        )
                );


        List<QuarterlyFact> debts =
                instantFactNormalizer.normalize(
                        factExtractor.extract(
                                response,
                                DEBT_TAGS,
                                "USD"
                        )
                );


        List<QuarterlyFact> cash =
                instantFactNormalizer.normalize(
                        factExtractor.extract(
                                response,
                                CASH_TAGS,
                                "USD"
                        )
                );


        List<QuarterlyFact> operatingCashFlows =
                cashFlowNormalizer.normalize(
                        factExtractor.extract(
                                response,
                                OCF_TAGS,
                                "USD"
                        )
                );


        List<QuarterlyFact> capitalExpenditures =
                cashFlowNormalizer.normalize(
                        factExtractor.extract(
                                response,
                                CAPEX_TAGS,
                                "USD"
                        )
                );


        List<QuarterlyFact> sharesOutstanding =
                loadSharesOutstanding(
                        response
                );


        Map<QuarterKey, QuarterlyFact> operatingIncomeMap =
                toMap(
                        operatingIncomes
                );

        Map<QuarterKey, QuarterlyFact> netIncomeMap =
                toMap(
                        netIncomes
                );

        Map<QuarterKey, QuarterlyFact> assetMap =
                toMap(
                        assets
                );

        Map<QuarterKey, QuarterlyFact> equityMap =
                toMap(
                        equities
                );

        Map<QuarterKey, QuarterlyFact> debtMap =
                toMap(
                        debts
                );

        Map<QuarterKey, QuarterlyFact> cashMap =
                toMap(
                        cash
                );

        Map<QuarterKey, QuarterlyFact> ocfMap =
                toMap(
                        operatingCashFlows
                );

        Map<QuarterKey, QuarterlyFact> capexMap =
                toMap(
                        capitalExpenditures
                );

        Map<QuarterKey, QuarterlyFact> sharesMap =
                toMap(
                        sharesOutstanding
                );


        return revenues.stream()

                .filter(
                        revenue ->
                                revenue.fiscalYear() != null
                                        && revenue.fiscalQuarter() != null
                )

                .map(
                        revenue -> {

                            QuarterKey key =
                                    keyOf(
                                            revenue
                                    );


                            QuarterlyFact operatingIncome =
                                    operatingIncomeMap.get(
                                            key
                                    );

                            QuarterlyFact netIncome =
                                    netIncomeMap.get(
                                            key
                                    );

                            QuarterlyFact asset =
                                    assetMap.get(
                                            key
                                    );

                            QuarterlyFact equity =
                                    equityMap.get(
                                            key
                                    );

                            QuarterlyFact debt =
                                    debtMap.get(
                                            key
                                    );

                            QuarterlyFact cashFact =
                                    cashMap.get(
                                            key
                                    );

                            QuarterlyFact ocf =
                                    ocfMap.get(
                                            key
                                    );

                            QuarterlyFact capex =
                                    capexMap.get(
                                            key
                                    );

                            QuarterlyFact shares =
                                    sharesMap.get(
                                            key
                                    );


                            LocalDate filedDate =
                                    latestFiledDate(

                                            revenue,
                                            operatingIncome,
                                            netIncome,

                                            asset,
                                            equity,
                                            debt,
                                            cashFact,

                                            ocf,
                                            capex,

                                            shares
                                    );


                            return new FinancialStatementData(

                                    symbol,

                                    revenue.endDate(),

                                    filedDate,

                                    revenue.fiscalYear(),

                                    revenue.fiscalQuarter(),

                                    revenue.value(),


                                    valueOf(
                                            operatingIncome
                                    ),

                                    valueOf(
                                            netIncome
                                    ),

                                    valueOf(
                                            asset
                                    ),

                                    valueOf(
                                            equity
                                    ),

                                    valueOf(
                                            debt
                                    ),

                                    valueOf(
                                            cashFact
                                    ),

                                    valueOf(
                                            ocf
                                    ),

                                    valueOf(
                                            capex
                                    ),

                                    valueOf(
                                            shares
                                    )
                            );
                        }
                )

                .sorted(
                        Comparator.comparing(
                                FinancialStatementData
                                        ::periodEndDate
                        )
                )

                .toList();
    }


    private List<QuarterlyFact> loadSharesOutstanding(
            SecCompanyFactsResponse response
    ) {

        /*
         * 가장 일반적인 SEC shares outstanding는
         * dei taxonomy에 존재한다.
         */
        List<SecFactPoint> deiFacts =
                factExtractor.extract(

                        response,

                        "dei",

                        List.of(
                                "EntityCommonStockSharesOutstanding"
                        ),

                        "shares"
                );


        if (!deiFacts.isEmpty()) {

            return instantFactNormalizer.normalize(
                    deiFacts
            );
        }


        /*
         * 일부 issuer fallback.
         */
        List<SecFactPoint> usGaapFacts =
                factExtractor.extract(

                        response,

                        "us-gaap",

                        List.of(
                                "CommonStockSharesOutstanding"
                        ),

                        "shares"
                );


        return instantFactNormalizer.normalize(
                usGaapFacts
        );
    }


    private Map<QuarterKey, QuarterlyFact> toMap(
            List<QuarterlyFact> facts
    ) {

        return facts.stream()

                .filter(
                        fact ->
                                fact.fiscalYear() != null
                                        && fact.fiscalQuarter() != null
                )

                .collect(
                        Collectors.toMap(

                                this::keyOf,

                                Function.identity(),

                                /*
                                 * Normalizer에서 이미 대부분
                                 * 중복 제거되지만 방어적으로
                                 * 먼저 들어온 값을 유지.
                                 */
                                (first, second) ->
                                        first
                        )
                );
    }


    private QuarterKey keyOf(
            QuarterlyFact fact
    ) {

        return new QuarterKey(

                fact.fiscalYear(),
                fact.fiscalQuarter()
        );
    }


    private java.math.BigDecimal valueOf(
            QuarterlyFact fact
    ) {

        if (fact == null) {
            return null;
        }

        return fact.value();
    }


    private LocalDate latestFiledDate(
            QuarterlyFact... facts
    ) {

        return Stream.of(
                        facts
                )

                .filter(
                        fact ->
                                fact != null
                                        && fact.filedDate() != null
                )

                .map(
                        QuarterlyFact::filedDate
                )

                .max(
                        Comparator.naturalOrder()
                )

                .orElse(null);
    }


    private record QuarterKey(
            Integer fiscalYear,
            String fiscalQuarter
    ) {
    }
}