package com.tony.tradinglab.smoke;

import com.tony.tradinglab.fundamental.client.FundamentalDataClient;
import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.domain.*;
import com.tony.tradinglab.fundamental.service.*;
import com.tony.tradinglab.marketdata.client.MarketDataClient;
import com.tony.tradinglab.marketdata.dto.DailyPrice;
import com.tony.tradinglab.price.domain.StockPrice;
import com.tony.tradinglab.price.repository.StockPriceRepository;
import com.tony.tradinglab.price.service.MarketDataSyncService;
import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationEntity;
import com.tony.tradinglab.stock.classification.persistence.StockClassificationRepository;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@Profile("smoke")
public class RealFundamentalSmokeRunner
        implements CommandLineRunner {

    private final FundamentalDataClient fundamentalDataClient;
    private final TtmFinancialsCalculator ttmFinancialsCalculator;
    private final RevenueGrowthCalculator revenueGrowthCalculator;
    private final GrowthAccelerationCalculator growthAccelerationCalculator;
    private final GrowthTrendAnalyzer growthTrendAnalyzer;
    private final ProfitabilityCalculator profitabilityCalculator;
    private final ProfitabilityTrendCalculator profitabilityTrendCalculator;
    private final ProfitabilityTrendAnalyzer profitabilityTrendAnalyzer;
    private final PointInTimeFundamentalAnalysisService
            pointInTimeFundamentalAnalysisService;
    private final MarketDataClient marketDataClient;
    private final ValuationMetricsCalculator valuationMetricsCalculator;
    private final TtmCashFlowCalculator ttmCashFlowCalculator;
    private final StockRepository stockRepository;
    private final ValuationPeerSnapshotFactory valuationPeerSnapshotFactory;
    private final PeerUniverseBuilder peerUniverseBuilder;
    private final StockClassificationRepository
            stockClassificationRepository;
    private final MarketDataSyncService marketDataSyncService;

    private final StockPriceRepository stockPriceRepository;

    public RealFundamentalSmokeRunner(
            FundamentalDataClient fundamentalDataClient,
            TtmFinancialsCalculator ttmFinancialsCalculator,
            RevenueGrowthCalculator revenueGrowthCalculator,
            GrowthAccelerationCalculator growthAccelerationCalculator,
            GrowthTrendAnalyzer growthTrendAnalyzer,
            ProfitabilityCalculator profitabilityCalculator,
            ProfitabilityTrendCalculator profitabilityTrendCalculator,
            ProfitabilityTrendAnalyzer profitabilityTrendAnalyzer,
            PointInTimeFundamentalAnalysisService pointInTimeFundamentalAnalysisService,
            MarketDataClient marketDataClient,
            ValuationMetricsCalculator valuationMetricsCalculator,
            TtmCashFlowCalculator ttmCashFlowCalculator,
            StockRepository stockRepository,
            ValuationPeerSnapshotFactory valuationPeerSnapshotFactory,
            PeerUniverseBuilder peerUniverseBuilder,
            StockClassificationRepository stockClassificationRepository,
            MarketDataSyncService marketDataSyncService,
            StockPriceRepository stockPriceRepository
    ) {

        this.fundamentalDataClient =
                fundamentalDataClient;

        this.ttmFinancialsCalculator =
                ttmFinancialsCalculator;

        this.revenueGrowthCalculator =
                revenueGrowthCalculator;

        this.growthAccelerationCalculator =
                growthAccelerationCalculator;

        this.growthTrendAnalyzer =
                growthTrendAnalyzer;

        this.profitabilityCalculator =
                profitabilityCalculator;

        this.profitabilityTrendCalculator =
                profitabilityTrendCalculator;

        this.profitabilityTrendAnalyzer =
                profitabilityTrendAnalyzer;

        this.pointInTimeFundamentalAnalysisService =
                pointInTimeFundamentalAnalysisService;

        this.marketDataClient =
                marketDataClient;

        this.valuationMetricsCalculator =
                valuationMetricsCalculator;

        this.ttmCashFlowCalculator =
                ttmCashFlowCalculator;

        this.stockRepository =
                stockRepository;

        this.valuationPeerSnapshotFactory =
                valuationPeerSnapshotFactory;

        this.peerUniverseBuilder =
                peerUniverseBuilder;

        this.stockClassificationRepository =
                stockClassificationRepository;

        this.marketDataSyncService =
                marketDataSyncService;

        this.stockPriceRepository =
                stockPriceRepository;
    }


    @Override
    public void run(
            String... args
    ) {

//        String symbol =
//                "AAPL";
//
//
//        System.out.println();
//        System.out.println(
//                "======================================"
//        );
//
//        System.out.println(
//                " REAL FUNDAMENTAL SMOKE TEST"
//        );
//
//        System.out.println(
//                " SYMBOL = " + symbol
//        );
//
//        System.out.println(
//                "======================================"
//        );
//
//
//        List<FinancialStatementData> statements =
//                fundamentalDataClient
//                        .getFinancialStatements(
//                                symbol
//                        );
//
//
//        System.out.println(
//                "Received statements = "
//                        + statements.size()
//        );
//
//
//        List<FinancialStatementData> sorted =
//                statements.stream()
//
//                        .sorted(
//                                Comparator.comparing(
//                                        FinancialStatementData::periodEndDate
//                                )
//                        )
//
//                        .toList();
//
//
//        sorted.stream()
//
//                .skip(
//                        Math.max(
//                                0,
//                                sorted.size() - 8
//                        )
//                )
//
//                .forEach(
//                        this::print
//                );
//
//        printRealAnalysis(
//                statements
//        );
//
//        printTtmCalculatorAnalysis(
//                statements
//        );
//
//        printRevenueGrowthAnalysis(
//                statements
//        );
//
//        printGrowthAccelerationAnalysis(
//                statements
//        );
//
//        printGrowthTrendAnalysis(
//                statements
//        );
//
//        printProfitabilityAnalysis(
//                statements
//        );
//
//        printProfitabilityTrendAnalysis(
//                statements
//        );
//
//        printProfitabilityTrendFinalAnalysis(
//                statements
//        );
//
//        printPointInTimeFundamentalAnalysis(
//                statements
//        );
//
//        printRealMarketPrice();
//
//        printRealValuation(
//                statements
//        );
//
//        seedRealPeerStocks();
//
//        printRealPeerUniverse();

        syncAaplPricesToDatabase();

        System.out.println(
                "======================================"
        );

        System.out.println(
                " END"
        );

        System.out.println(
                "======================================"
        );
    }


    private void print(
            FinancialStatementData data
    ) {

        System.out.println();

        System.out.println(
                "Period End     : "
                        + data.periodEndDate()
        );

        System.out.println(
                "Filed Date     : "
                        + data.filedDate()
        );

        System.out.println(
                "Fiscal Year    : "
                        + data.fiscalYear()
        );

        System.out.println(
                "Fiscal Quarter : "
                        + data.fiscalQuarter()
        );

        System.out.println(
                "Revenue        : "
                        + data.revenue()
        );

        System.out.println(
                "Operating Inc  : "
                        + data.operatingIncome()
        );

        System.out.println(
                "Net Income     : "
                        + data.netIncome()
        );

        System.out.println(
                "Assets         : "
                        + data.totalAssets()
        );

        System.out.println(
                "Equity         : "
                        + data.totalEquity()
        );

        System.out.println(
                "Debt           : "
                        + data.totalDebt()
        );

        System.out.println(
                "Cash           : "
                        + data.cash()
        );

        System.out.println(
                "OCF            : "
                        + data.operatingCashFlow()
        );

        System.out.println(
                "CapEx          : "
                        + data.capitalExpenditure()
        );

        System.out.println(
                "Shares         : "
                        + data.sharesOutstanding()
        );

        System.out.println(
                "--------------------------------------"
        );
    }

    private void printRealAnalysis(
            List<FinancialStatementData> statements
    ) {

        List<FinancialStatementData> sorted =
                statements.stream()

                        .sorted(
                                Comparator.comparing(
                                        FinancialStatementData::periodEndDate
                                )
                        )

                        .toList();


        if (sorted.size() < 8) {

            System.out.println(
                    "Not enough data for analysis."
            );

            return;
        }


        List<FinancialStatementData> latestFour =
                sorted.subList(
                        sorted.size() - 4,
                        sorted.size()
                );


        BigDecimal ttmRevenue =
                latestFour.stream()

                        .map(
                                FinancialStatementData::revenue
                        )

                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal ttmOperatingIncome =
                latestFour.stream()

                        .map(
                                FinancialStatementData::operatingIncome
                        )

                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal ttmNetIncome =
                latestFour.stream()

                        .map(
                                FinancialStatementData::netIncome
                        )

                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal ttmOcf =
                latestFour.stream()

                        .map(
                                FinancialStatementData::operatingCashFlow
                        )

                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal ttmCapex =
                latestFour.stream()

                        .map(
                                FinancialStatementData::capitalExpenditure
                        )

                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        BigDecimal ttmFcf =
                ttmOcf.subtract(
                        ttmCapex
                );


        BigDecimal operatingMargin =
                percentage(
                        ttmOperatingIncome,
                        ttmRevenue
                );


        BigDecimal netMargin =
                percentage(
                        ttmNetIncome,
                        ttmRevenue
                );


        BigDecimal fcfMargin =
                percentage(
                        ttmFcf,
                        ttmRevenue
                );


        FinancialStatementData latest =
                sorted.get(
                        sorted.size() - 1
                );


        FinancialStatementData yearAgo =
                sorted.get(
                        sorted.size() - 5
                );


        BigDecimal latestRevenueYoy =
                latest.revenue()

                        .subtract(
                                yearAgo.revenue()
                        )

                        .divide(
                                yearAgo.revenue(),
                                8,
                                RoundingMode.HALF_UP
                        )

                        .multiply(
                                new BigDecimal("100")
                        );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL AAPL ANALYSIS"
        );

        System.out.println(
                "======================================"
        );


        System.out.println(
                "Latest Period        : "
                        + latest.periodEndDate()
        );


        System.out.println(
                "TTM Revenue          : "
                        + ttmRevenue
        );


        System.out.println(
                "TTM Operating Income : "
                        + ttmOperatingIncome
        );


        System.out.println(
                "TTM Net Income       : "
                        + ttmNetIncome
        );


        System.out.println(
                "TTM OCF              : "
                        + ttmOcf
        );


        System.out.println(
                "TTM CapEx            : "
                        + ttmCapex
        );


        System.out.println(
                "TTM FCF              : "
                        + ttmFcf
        );


        System.out.println(
                "Operating Margin     : "
                        + operatingMargin
                        + "%"
        );


        System.out.println(
                "Net Margin           : "
                        + netMargin
                        + "%"
        );


        System.out.println(
                "FCF Margin           : "
                        + fcfMargin
                        + "%"
        );


        System.out.println(
                "Latest Revenue YoY   : "
                        + latestRevenueYoy
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        + "%"
        );


        System.out.println(
                "======================================"
        );
    }

    private BigDecimal percentage(
            BigDecimal numerator,
            BigDecimal denominator
    ) {

        return numerator

                .divide(
                        denominator,
                        8,
                        RoundingMode.HALF_UP
                )

                .multiply(
                        new BigDecimal("100")
                )

                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private void printTtmCalculatorAnalysis(
            List<FinancialStatementData> statements
    ) {

        List<QuarterlyFinancials> quarterlyFinancials =
                statements.stream()

                        .map(
                                data ->
                                        new QuarterlyFinancials(

                                                data.fiscalYear(),
                                                data.fiscalQuarter(),

                                                data.revenue(),
                                                data.operatingIncome(),
                                                data.netIncome(),

                                                data.filedDate()
                                        )
                        )

                        .toList();


        List<TtmFinancials> ttmFinancials =
                ttmFinancialsCalculator.calculate(
                        quarterlyFinancials
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL TTM CALCULATOR"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "TTM result count : "
                        + ttmFinancials.size()
        );


        if (ttmFinancials.isEmpty()) {

            System.out.println(
                    "No TTM result."
            );

            return;
        }


        TtmFinancials latest =
                ttmFinancials.get(
                        ttmFinancials.size() - 1
                );


        System.out.println(
                "Fiscal Period        : "
                        + latest.fiscalYear()
                        + " "
                        + latest.fiscalQuarter()
        );

        System.out.println(
                "Filed Date           : "
                        + latest.filedDate()
        );

        System.out.println(
                "TTM Revenue          : "
                        + latest.revenue()
        );

        System.out.println(
                "TTM Operating Income : "
                        + latest.operatingIncome()
        );

        System.out.println(
                "TTM Net Income       : "
                        + latest.netIncome()
        );

        System.out.println(
                "======================================"
        );
    }

    private void printRevenueGrowthAnalysis(
            List<FinancialStatementData> statements
    ) {

        List<QuarterlyFact> revenueFacts =
                statements.stream()

                        .map(
                                data ->
                                        new QuarterlyFact(

                                                "Revenue",

                                                data.revenue(),

                                                null,

                                                data.periodEndDate(),

                                                data.filedDate(),

                                                data.fiscalYear(),

                                                data.fiscalQuarter(),

                                                false
                                        )
                        )

                        .toList();


        List<RevenueGrowth> growths =
                revenueGrowthCalculator.calculate(
                        revenueFacts
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL REVENUE GROWTH"
        );

        System.out.println(
                "======================================"
        );


        growths.stream()

                .skip(
                        Math.max(
                                0,
                                growths.size() - 8
                        )
                )

                .forEach(
                        growth -> {

                            System.out.println(
                                    growth.fiscalYear()
                                            + " "
                                            + growth.fiscalQuarter()
                                            + " | Revenue = "
                                            + growth.revenue()
                                            + " | Previous = "
                                            + growth.previousYearRevenue()
                                            + " | YoY = "
                                            + growth.yoyGrowthPct()
                                            + "%"
                            );
                        }
                );


        System.out.println(
                "======================================"
        );
    }

    private void printGrowthAccelerationAnalysis(
            List<FinancialStatementData> statements
    ) {

        List<QuarterlyFact> revenueFacts =
                statements.stream()

                        .map(
                                data ->
                                        new QuarterlyFact(
                                                "Revenue",
                                                data.revenue(),
                                                null,
                                                data.periodEndDate(),
                                                data.filedDate(),
                                                data.fiscalYear(),
                                                data.fiscalQuarter(),
                                                false
                                        )
                        )

                        .toList();


        List<RevenueGrowth> growths =
                revenueGrowthCalculator.calculate(
                        revenueFacts
                );


        List<GrowthAcceleration> accelerations =
                growthAccelerationCalculator.calculate(
                        growths
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL GROWTH ACCELERATION"
        );

        System.out.println(
                "======================================"
        );


        accelerations.stream()

                .skip(
                        Math.max(
                                0,
                                accelerations.size() - 8
                        )
                )

                .forEach(
                        acceleration -> {

                            System.out.println(
                                    acceleration.fiscalYear()
                                            + " "
                                            + acceleration.fiscalQuarter()
                                            + " | YoY = "
                                            + acceleration.yoyGrowthPct()
                                            + "%"
                                            + " | Previous YoY = "
                                            + acceleration.previousQuarterYoyGrowthPct()
                                            + "%"
                                            + " | Acceleration = "
                                            + acceleration.accelerationPctPoint()
                                            + "%p"
                            );
                        }
                );


        System.out.println(
                "======================================"
        );
    }

    private void printGrowthTrendAnalysis(
            List<FinancialStatementData> statements
    ) {

        List<QuarterlyFact> revenueFacts =
                statements.stream()
                        .map(
                                data ->
                                        new QuarterlyFact(
                                                "Revenue",
                                                data.revenue(),
                                                null,
                                                data.periodEndDate(),
                                                data.filedDate(),
                                                data.fiscalYear(),
                                                data.fiscalQuarter(),
                                                false
                                        )
                        )
                        .toList();


        List<RevenueGrowth> growths =
                revenueGrowthCalculator.calculate(
                        revenueFacts
                );


        List<GrowthAcceleration> accelerations =
                growthAccelerationCalculator.calculate(
                        growths
                );


        GrowthTrendAnalysis analysis =
                growthTrendAnalyzer.analyze(
                        accelerations
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL GROWTH TREND"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Growth Trend          : "
                        + analysis.trend()
        );

        System.out.println(
                "Latest YoY Growth     : "
                        + analysis.latestYoyGrowthPct()
                        + "%"
        );

        System.out.println(
                "Average Acceleration  : "
                        + analysis.averageAccelerationPctPoint()
                        + "%p"
        );

        System.out.println(
                "Positive Count        : "
                        + analysis.positiveAccelerationCount()
        );

        System.out.println(
                "Negative Count        : "
                        + analysis.negativeAccelerationCount()
        );

        System.out.println(
                "Analyzed Quarters     : "
                        + analysis.analyzedQuarterCount()
        );

        System.out.println(
                "======================================"
        );
    }

    private void printProfitabilityAnalysis(
            List<FinancialStatementData> statements
    ) {

        List<QuarterlyFinancials> quarterlyFinancials =
                statements.stream()

                        .map(
                                data ->
                                        new QuarterlyFinancials(
                                                data.fiscalYear(),
                                                data.fiscalQuarter(),
                                                data.revenue(),
                                                data.operatingIncome(),
                                                data.netIncome(),
                                                data.filedDate()
                                        )
                        )

                        .toList();


        List<Profitability> profitability =
                profitabilityCalculator.calculate(
                        quarterlyFinancials
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL PROFITABILITY"
        );

        System.out.println(
                "======================================"
        );


        profitability.stream()

                .skip(
                        Math.max(
                                0,
                                profitability.size() - 8
                        )
                )

                .forEach(
                        System.out::println
                );


        System.out.println(
                "======================================"
        );
    }

    private void printProfitabilityTrendAnalysis(
            List<FinancialStatementData> statements
    ) {

        List<QuarterlyFinancials> quarterlyFinancials =
                statements.stream()

                        .map(
                                data ->
                                        new QuarterlyFinancials(
                                                data.fiscalYear(),
                                                data.fiscalQuarter(),
                                                data.revenue(),
                                                data.operatingIncome(),
                                                data.netIncome(),
                                                data.filedDate()
                                        )
                        )

                        .toList();


        List<Profitability> profitabilities =
                profitabilityCalculator.calculate(
                        quarterlyFinancials
                );


        List<ProfitabilityTrend> trends =
                profitabilityTrendCalculator.calculate(
                        profitabilities
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL PROFITABILITY TREND"
        );

        System.out.println(
                "======================================"
        );


        trends.stream()

                .skip(
                        Math.max(
                                0,
                                trends.size() - 8
                        )
                )

                .forEach(
                        System.out::println
                );


        System.out.println(
                "======================================"
        );
    }

    private void printProfitabilityTrendFinalAnalysis(
            List<FinancialStatementData> statements
    ) {

        List<QuarterlyFinancials> quarterlyFinancials =
                statements.stream()

                        .map(
                                data ->
                                        new QuarterlyFinancials(
                                                data.fiscalYear(),
                                                data.fiscalQuarter(),
                                                data.revenue(),
                                                data.operatingIncome(),
                                                data.netIncome(),
                                                data.filedDate()
                                        )
                        )

                        .toList();


        List<Profitability> profitabilities =
                profitabilityCalculator.calculate(
                        quarterlyFinancials
                );


        List<ProfitabilityTrend> trends =
                profitabilityTrendCalculator.calculate(
                        profitabilities
                );


        ProfitabilityTrendAnalysis analysis =
                profitabilityTrendAnalyzer.analyze(
                        trends
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL PROFITABILITY TREND ANALYSIS"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                analysis
        );

        System.out.println(
                "======================================"
        );
    }

    private void printPointInTimeFundamentalAnalysis(
            List<FinancialStatementData> statements
    ) {

        LocalDate asOfDate =
                LocalDate.of(
                        2026,
                        7,
                        31
                );


        List<QuarterlyFinancials> quarterlyFinancials =
                statements.stream()

                        /*
                         * 기준일 당시 시장에 공개된 데이터만 사용
                         */
                        .filter(
                                data ->
                                        data.filedDate() != null
                                                && !data.filedDate()
                                                .isAfter(asOfDate)
                        )

                        .map(
                                data ->
                                        new QuarterlyFinancials(
                                                data.fiscalYear(),
                                                data.fiscalQuarter(),
                                                data.revenue(),
                                                data.operatingIncome(),
                                                data.netIncome(),
                                                data.filedDate()
                                        )
                        )

                        .toList();


        List<QuarterlyFact> revenueFacts =
                statements.stream()

                        .filter(
                                data ->
                                        data.filedDate() != null
                                                && !data.filedDate()
                                                .isAfter(asOfDate)
                        )

                        .map(
                                data ->
                                        new QuarterlyFact(
                                                "Revenue",
                                                data.revenue(),
                                                null,
                                                data.periodEndDate(),
                                                data.filedDate(),
                                                data.fiscalYear(),
                                                data.fiscalQuarter(),
                                                false
                                        )
                        )

                        .toList();


        PointInTimeFundamentalContext context =
                new PointInTimeFundamentalContext(
                        1L,
                        "AAPL",
                        asOfDate,
                        quarterlyFinancials
                );


        Optional<PointInTimeFundamentalAnalysis> result =
                pointInTimeFundamentalAnalysisService.analyze(
                        context,
                        revenueFacts
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL POINT-IN-TIME FUNDAMENTAL ANALYSIS"
        );

        System.out.println(
                "======================================"
        );


        if (result.isEmpty()) {

            System.out.println(
                    "No analysis result."
            );

            System.out.println(
                    "======================================"
            );

            return;
        }


        System.out.println(
                result.get()
        );


        System.out.println(
                "======================================"
        );
    }

    private void printRealMarketPrice() {

        LocalDate startDate =
                LocalDate.of(
                        2026,
                        7,
                        30
                );

        LocalDate endDate =
                LocalDate.of(
                        2026,
                        8,
                        3
                );


        List<DailyPrice> prices =
                marketDataClient.getDailyPrices(
                        "AAPL",
                        startDate,
                        endDate
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL AAPL MARKET PRICE"
        );

        System.out.println(
                "======================================"
        );

        System.out.println(
                "Received prices = "
                        + prices.size()
        );


        prices.forEach(
                System.out::println
        );


        System.out.println(
                "======================================"
        );
    }

    private void printRealValuation(
            List<FinancialStatementData> statements
    ) {

        LocalDate priceDate =
                LocalDate.of(
                        2026,
                        7,
                        31
                );


        List<FinancialStatementData> available =
                statements.stream()

                        .filter(
                                data ->
                                        data.filedDate() != null
                                                && !data.filedDate()
                                                .isAfter(priceDate)
                        )

                        .toList();


        /*
         * 1. TTM Financials
         */
        List<QuarterlyFinancials> quarterlyFinancials =
                available.stream()

                        .map(
                                data ->
                                        new QuarterlyFinancials(
                                                data.fiscalYear(),
                                                data.fiscalQuarter(),
                                                data.revenue(),
                                                data.operatingIncome(),
                                                data.netIncome(),
                                                data.filedDate()
                                        )
                        )

                        .toList();


        List<TtmFinancials> ttms =
                ttmFinancialsCalculator.calculate(
                        quarterlyFinancials
                );


        TtmFinancials latestTtm =
                ttms.get(
                        ttms.size() - 1
                );


        /*
         * 2. Quarterly Cash Flow
         */
        List<QuarterlyCashFlow> quarterlyCashFlows =
                available.stream()

                        .filter(
                                data ->
                                        data.operatingCashFlow() != null
                                                && data.capitalExpenditure() != null
                        )

                        .map(
                                data -> {

                                    BigDecimal freeCashFlow =
                                            data.operatingCashFlow()
                                                    .subtract(
                                                            data.capitalExpenditure()
                                                    );


                                    return new QuarterlyCashFlow(
                                            data.fiscalYear(),
                                            data.fiscalQuarter(),
                                            data.operatingCashFlow(),
                                            data.capitalExpenditure(),
                                            freeCashFlow,
                                            data.filedDate()
                                    );
                                }
                        )

                        .toList();


        /*
         * 3. TTM Cash Flow
         */
        List<TtmCashFlow> ttmCashFlows =
                ttmCashFlowCalculator.calculate(
                        quarterlyCashFlows
                );


        TtmCashFlow latestTtmCashFlow =
                ttmCashFlows.stream()

                        .filter(
                                cashFlow ->
                                        cashFlow.fiscalYear()
                                                .equals(
                                                        latestTtm.fiscalYear()
                                                )
                        )

                        .filter(
                                cashFlow ->
                                        cashFlow.fiscalQuarter()
                                                .equals(
                                                        latestTtm.fiscalQuarter()
                                                )
                        )

                        .max(
                                Comparator.comparing(
                                        TtmCashFlow::filedDate
                                )
                        )

                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "동일 분기의 TTM Cash Flow가 없습니다."
                                        )
                        );


        /*
         * 4. 실제 AAPL 가격
         */
        DailyPrice price =
                marketDataClient.getDailyPrices(
                                "AAPL",
                                priceDate,
                                priceDate.plusDays(1)
                        )

                        .stream()

                        .filter(
                                dailyPrice ->
                                        dailyPrice.tradeDate()
                                                .equals(priceDate)
                        )

                        .findFirst()

                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "AAPL 가격이 없습니다: "
                                                        + priceDate
                                        )
                        );


        /*
         * 5. 기준일 당시 최신 Shares Outstanding
         */
        BigDecimal sharesOutstanding =
                available.stream()

                        .filter(
                                data ->
                                        data.sharesOutstanding() != null
                        )

                        .max(
                                Comparator.comparing(
                                        FinancialStatementData::filedDate
                                )
                        )

                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Shares Outstanding 데이터가 없습니다."
                                        )
                        )

                        .sharesOutstanding();


        /*
         * 6. Valuation 계산
         */
        ValuationMetrics valuation =
                valuationMetricsCalculator.calculate(
                        latestTtm,
                        latestTtmCashFlow,
                        priceDate,
                        price.close(),
                        sharesOutstanding
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL AAPL VALUATION"
        );

        System.out.println(
                "======================================"
        );


        System.out.println(
                "TTM OCF              : "
                        + latestTtmCashFlow.operatingCashFlow()
        );

        System.out.println(
                "TTM CapEx            : "
                        + latestTtmCashFlow.capitalExpenditure()
        );

        System.out.println(
                "TTM FCF              : "
                        + latestTtmCashFlow.freeCashFlow()
        );


        System.out.println();

        System.out.println(
                valuation
        );


        System.out.println(
                "======================================"
        );
    }

    private void printRealPeerUniverse() {

        LocalDate observationDate =
                LocalDate.of(
                        2026,
                        7,
                        31
                );


        /*
         * AAPL을 포함한 Technology 후보군.
         *
         * 일부 종목이 SEC / DB classification 등의 이유로
         * 탈락해도 최소 Peer 수를 확보하기 위해
         * 여유 있게 넣는다.
         */
        List<String> symbols =
                List.of(
                        "AAPL",
                        "MSFT",
                        "NVDA",
                        "AVGO",
                        "AMD",
                        "QCOM",
                        "INTC",
                        "TXN",
                        "MU",
                        "AMAT",
                        "LRCX",
                        "KLAC",
                        "CSCO",
                        "ORCL",
                        "ADBE",
                        "CRM"
                );


        /*
         * DB에 실제 등록되어 있는 Stock 목록.
         *
         * 여기 있는 실제 stockId를 사용해야
         * StockClassificationService가
         * PIT sector / industry를 찾을 수 있다.
         */
        List<Stock> registeredStocks =
                stockRepository.findAll();


        List<ValuationPeerSnapshot> snapshots =
                new ArrayList<>();


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL PEER SNAPSHOT BUILD"
        );

        System.out.println(
                " Observation Date = "
                        + observationDate
        );

        System.out.println(
                "======================================"
        );


        for (String symbol : symbols) {

            try {

                /*
                 * 1. 실제 Stock DB row 찾기
                 */
                Optional<Stock> stockOptional =
                        registeredStocks.stream()

                                .filter(
                                        stock ->
                                                symbol.equalsIgnoreCase(
                                                        stock.getSymbol()
                                                )
                                )

                                .findFirst();


                if (stockOptional.isEmpty()) {

                    System.out.println(
                            "[SKIP] "
                                    + symbol
                                    + " : stocks 테이블에 없음"
                    );

                    continue;
                }


                Stock stock =
                        stockOptional.get();


                /*
                 * 2. 실제 SEC Fundamental
                 */
                List<FinancialStatementData> statements =
                        fundamentalDataClient
                                .getFinancialStatements(
                                        symbol
                                );


                /*
                 * observationDate 당시
                 * 시장에 공개되어 있던 데이터만 사용.
                 */
                List<FinancialStatementData> available =
                        statements.stream()

                                .filter(
                                        data ->
                                                data.filedDate() != null
                                                        && !data.filedDate()
                                                        .isAfter(
                                                                observationDate
                                                        )
                                )

                                .toList();


                if (available.isEmpty()) {

                    System.out.println(
                            "[SKIP] "
                                    + symbol
                                    + " : PIT Fundamental 없음"
                    );

                    continue;
                }


                /*
                 * 3. Quarterly Financials
                 */
                List<QuarterlyFinancials> quarterlyFinancials =
                        available.stream()

                                .map(
                                        data ->
                                                new QuarterlyFinancials(
                                                        data.fiscalYear(),
                                                        data.fiscalQuarter(),
                                                        data.revenue(),
                                                        data.operatingIncome(),
                                                        data.netIncome(),
                                                        data.filedDate()
                                                )
                                )

                                .toList();


                /*
                 * 4. Revenue Facts
                 */
                List<QuarterlyFact> revenueFacts =
                        available.stream()

                                .map(
                                        data ->
                                                new QuarterlyFact(
                                                        "Revenue",
                                                        data.revenue(),
                                                        null,
                                                        data.periodEndDate(),
                                                        data.filedDate(),
                                                        data.fiscalYear(),
                                                        data.fiscalQuarter(),
                                                        false
                                                )
                                )

                                .toList();


                /*
                 * 5. PIT Fundamental Analysis
                 */
                PointInTimeFundamentalContext context =
                        new PointInTimeFundamentalContext(
                                stock.getId(),
                                symbol,
                                observationDate,
                                quarterlyFinancials
                        );


                Optional<PointInTimeFundamentalAnalysis>
                        fundamentalOptional =
                        pointInTimeFundamentalAnalysisService
                                .analyze(
                                        context,
                                        revenueFacts
                                );


                if (fundamentalOptional.isEmpty()) {

                    System.out.println(
                            "[SKIP] "
                                    + symbol
                                    + " : Fundamental Analysis 실패"
                    );

                    continue;
                }


                PointInTimeFundamentalAnalysis fundamental =
                        fundamentalOptional.get();


                TtmFinancials ttmFinancials =
                        fundamental.ttmFinancials();


                /*
                 * 6. Quarterly Cash Flow
                 */
                List<QuarterlyCashFlow> quarterlyCashFlows =
                        available.stream()

                                .filter(
                                        data ->
                                                data.operatingCashFlow() != null
                                                        && data.capitalExpenditure() != null
                                )

                                .map(
                                        data -> {

                                            BigDecimal freeCashFlow =
                                                    data.operatingCashFlow()
                                                            .subtract(
                                                                    data.capitalExpenditure()
                                                            );


                                            return new QuarterlyCashFlow(
                                                    data.fiscalYear(),
                                                    data.fiscalQuarter(),
                                                    data.operatingCashFlow(),
                                                    data.capitalExpenditure(),
                                                    freeCashFlow,
                                                    data.filedDate()
                                            );
                                        }
                                )

                                .toList();


                List<TtmCashFlow> ttmCashFlows =
                        ttmCashFlowCalculator.calculate(
                                quarterlyCashFlows
                        );


                Optional<TtmCashFlow> cashFlowOptional =
                        ttmCashFlows.stream()

                                .filter(
                                        cashFlow ->
                                                cashFlow.fiscalYear()
                                                        .equals(
                                                                ttmFinancials
                                                                        .fiscalYear()
                                                        )
                                )

                                .filter(
                                        cashFlow ->
                                                cashFlow.fiscalQuarter()
                                                        .equals(
                                                                ttmFinancials
                                                                        .fiscalQuarter()
                                                        )
                                )

                                .max(
                                        Comparator.comparing(
                                                TtmCashFlow::filedDate
                                        )
                                );


                if (cashFlowOptional.isEmpty()) {

                    System.out.println(
                            "[SKIP] "
                                    + symbol
                                    + " : TTM Cash Flow 없음"
                    );

                    continue;
                }


                TtmCashFlow ttmCashFlow =
                        cashFlowOptional.get();


                /*
                 * 7. 실제 Historical Price
                 */
                Optional<DailyPrice> priceOptional =
                        marketDataClient
                                .getDailyPrices(
                                        symbol,
                                        observationDate,
                                        observationDate.plusDays(1)
                                )

                                .stream()

                                .filter(
                                        price ->
                                                price.tradeDate()
                                                        .equals(
                                                                observationDate
                                                        )
                                )

                                .findFirst();


                if (priceOptional.isEmpty()) {

                    System.out.println(
                            "[SKIP] "
                                    + symbol
                                    + " : 가격 없음"
                    );

                    continue;
                }


                DailyPrice price =
                        priceOptional.get();


                /*
                 * 8. PIT Shares Outstanding
                 */
                Optional<FinancialStatementData> sharesSource =
                        available.stream()

                                .filter(
                                        data ->
                                                data.sharesOutstanding() != null
                                )

                                .max(
                                        Comparator.comparing(
                                                FinancialStatementData
                                                        ::filedDate
                                        )
                                );


                if (sharesSource.isEmpty()) {

                    System.out.println(
                            "[SKIP] "
                                    + symbol
                                    + " : Shares Outstanding 없음"
                    );

                    continue;
                }


                BigDecimal sharesOutstanding =
                        sharesSource.get()
                                .sharesOutstanding();


                /*
                 * 9. Valuation
                 */
                ValuationMetrics valuation =
                        valuationMetricsCalculator.calculate(
                                ttmFinancials,
                                ttmCashFlow,
                                observationDate,
                                price.close(),
                                sharesOutstanding
                        );


                /*
                 * 10. PIT Classification까지 결합해서
                 *     최종 ValuationPeerSnapshot 생성
                 */
                Optional<ValuationPeerSnapshot> snapshotOptional =
                        valuationPeerSnapshotFactory.create(
                                stock.getId(),
                                symbol,
                                observationDate,
                                valuation,
                                fundamental.growth(),
                                fundamental.profitability()
                        );


                if (snapshotOptional.isEmpty()) {

                    System.out.println(
                            "[SKIP] "
                                    + symbol
                                    + " : PIT classification 없음"
                    );

                    continue;
                }


                ValuationPeerSnapshot snapshot =
                        snapshotOptional.get();


                snapshots.add(
                        snapshot
                );


                System.out.println(
                        "[OK] "
                                + symbol
                                + " | sector="
                                + snapshot.sector()
                                + " | industry="
                                + snapshot.industry()
                                + " | MarketCap="
                                + valuation.marketCap()
                                + " | PE="
                                + valuation.peRatio()
                                + " | PS="
                                + valuation.psRatio()
                                + " | PFCF="
                                + valuation.priceToFcfRatio()
                );

            } catch (Exception e) {

                /*
                 * 한 종목에 문제가 있어도
                 * 전체 Peer Smoke Test는 계속 진행한다.
                 */
                System.out.println(
                        "[ERROR] "
                                + symbol
                                + " : "
                                + e.getMessage()
                );
            }
        }


        System.out.println();
        System.out.println(
                "Created snapshots = "
                        + snapshots.size()
        );


        /*
         * Target AAPL
         */
        ValuationPeerSnapshot target =
                snapshots.stream()

                        .filter(
                                snapshot ->
                                        "AAPL".equalsIgnoreCase(
                                                snapshot.symbol()
                                        )
                        )

                        .findFirst()

                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "AAPL Snapshot이 생성되지 않았습니다."
                                        )
                        );


        /*
         * 실제 Peer Universe 선정
         */
        PeerUniverse universe =
                peerUniverseBuilder.build(
                        target,
                        snapshots
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " REAL AAPL PEER UNIVERSE"
        );

        System.out.println(
                "======================================"
        );


        /*
         * PeerUniverse record의 실제 필드명을
         * 추측해서 accessor 오류를 만들지 않도록
         * 일단 record 전체 출력.
         */
        System.out.println(
                universe
        );


        System.out.println(
                "======================================"
        );
    }

    private void seedRealPeerStocks() {

        LocalDate effectiveFrom =
                LocalDate.of(
                        2020,
                        1,
                        1
                );


        record SeedData(
                String symbol,
                String name,
                String exchange,
                String sector,
                String industry
        ) {
        }


        List<SeedData> seeds =
                List.of(

                        new SeedData(
                                "AAPL",
                                "Apple Inc.",
                                "NASDAQ",
                                "Technology",
                                "Consumer Electronics"
                        ),

                        new SeedData(
                                "MSFT",
                                "Microsoft Corporation",
                                "NASDAQ",
                                "Technology",
                                "Software - Infrastructure"
                        ),

                        new SeedData(
                                "NVDA",
                                "NVIDIA Corporation",
                                "NASDAQ",
                                "Technology",
                                "Semiconductors"
                        ),

                        new SeedData(
                                "AVGO",
                                "Broadcom Inc.",
                                "NASDAQ",
                                "Technology",
                                "Semiconductors"
                        ),

                        new SeedData(
                                "AMD",
                                "Advanced Micro Devices, Inc.",
                                "NASDAQ",
                                "Technology",
                                "Semiconductors"
                        ),

                        new SeedData(
                                "QCOM",
                                "QUALCOMM Incorporated",
                                "NASDAQ",
                                "Technology",
                                "Semiconductors"
                        ),

                        new SeedData(
                                "INTC",
                                "Intel Corporation",
                                "NASDAQ",
                                "Technology",
                                "Semiconductors"
                        ),

                        new SeedData(
                                "TXN",
                                "Texas Instruments Incorporated",
                                "NASDAQ",
                                "Technology",
                                "Semiconductors"
                        ),

                        new SeedData(
                                "MU",
                                "Micron Technology, Inc.",
                                "NASDAQ",
                                "Technology",
                                "Semiconductors"
                        ),

                        new SeedData(
                                "AMAT",
                                "Applied Materials, Inc.",
                                "NASDAQ",
                                "Technology",
                                "Semiconductor Equipment & Materials"
                        ),

                        new SeedData(
                                "LRCX",
                                "Lam Research Corporation",
                                "NASDAQ",
                                "Technology",
                                "Semiconductor Equipment & Materials"
                        ),

                        new SeedData(
                                "KLAC",
                                "KLA Corporation",
                                "NASDAQ",
                                "Technology",
                                "Semiconductor Equipment & Materials"
                        ),

                        new SeedData(
                                "CSCO",
                                "Cisco Systems, Inc.",
                                "NASDAQ",
                                "Technology",
                                "Communication Equipment"
                        ),

                        new SeedData(
                                "ORCL",
                                "Oracle Corporation",
                                "NYSE",
                                "Technology",
                                "Software - Infrastructure"
                        ),

                        new SeedData(
                                "ADBE",
                                "Adobe Inc.",
                                "NASDAQ",
                                "Technology",
                                "Software - Application"
                        ),

                        new SeedData(
                                "CRM",
                                "Salesforce, Inc.",
                                "NYSE",
                                "Technology",
                                "Software - Application"
                        )
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " SEED REAL PEER STOCKS"
        );

        System.out.println(
                "======================================"
        );


        for (SeedData seed : seeds) {

            /*
             * 1. Stock
             */
            Stock stock =
                    stockRepository
                            .findBySymbolAndExchange(
                                    seed.symbol(),
                                    seed.exchange()
                            )

                            .orElseGet(
                                    () ->
                                            stockRepository.save(
                                                    new Stock(
                                                            seed.symbol(),
                                                            seed.name(),
                                                            seed.exchange(),
                                                            "US",
                                                            "USD"
                                                    )
                                            )
                            );


            /*
             * 2. Classification
             *
             * 같은 effectiveFrom 데이터가 이미 있으면
             * 다시 INSERT 하지 않는다.
             */
            boolean classificationExists =
                    stockClassificationRepository
                            .findByStockIdOrderByEffectiveFromAsc(
                                    stock.getId()
                            )

                            .stream()

                            .anyMatch(
                                    classification ->
                                            effectiveFrom.equals(
                                                    classification
                                                            .getEffectiveFrom()
                                            )
                            );


            if (!classificationExists) {

                StockClassificationEntity classification =
                        new StockClassificationEntity(

                                stock.getId(),

                                seed.sector(),
                                seed.industry(),

                                effectiveFrom,
                                null,

                                ClassificationSource.MANUAL
                        );


                stockClassificationRepository.save(
                        classification
                );
            }


            System.out.println(
                    "[OK] "
                            + seed.symbol()
                            + " | stockId="
                            + stock.getId()
                            + " | "
                            + seed.sector()
                            + " / "
                            + seed.industry()
            );
        }


        System.out.println(
                "======================================"
        );
    }

    private void syncAaplPricesToDatabase() {

        String symbol =
                "AAPL";

        String exchange =
                "NASDAQ";

        LocalDate startDate =
                LocalDate.of(
                        2026,
                        1,
                        1
                );

        LocalDate endDate =
                LocalDate.of(
                        2026,
                        7,
                        31
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " AAPL MARKET DATA DB SYNC"
        );

        System.out.println(
                "======================================"
        );


        /*
         * Stock master가 아직 없기 때문에
         * 이번 smoke test에서 AAPL만 등록한다.
         *
         * 가격 데이터 자체는 아래 MarketDataSyncService가
         * Twelve Data API에서 실제로 받아온다.
         */
        Stock stock =
                stockRepository
                        .findBySymbolAndExchange(
                                symbol,
                                exchange
                        )

                        .orElseGet(
                                () ->
                                        stockRepository.save(
                                                new Stock(
                                                        "AAPL",
                                                        "Apple Inc.",
                                                        "NASDAQ",
                                                        "US",
                                                        "USD"
                                                )
                                        )
                        );


        System.out.println(
                "Stock ID       : "
                        + stock.getId()
        );


        System.out.println(
                "Symbol         : "
                        + stock.getSymbol()
        );


        System.out.println(
                "Sync Period    : "
                        + startDate
                        + " ~ "
                        + endDate
        );


        /*
         * 실제 Twelve Data API
         *      ↓
         * DailyPrice
         *      ↓
         * StockPrice
         *      ↓
         * MySQL INSERT
         */
        int insertedCount =
                marketDataSyncService.sync(
                        symbol,
                        exchange,
                        startDate,
                        endDate
                );


        System.out.println(
                "Newly Inserted : "
                        + insertedCount
        );


        /*
         * 실제 DB에서 다시 조회한다.
         */
        List<StockPrice> storedPrices =
                stockPriceRepository
                        .findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(
                                stock.getId(),
                                startDate,
                                endDate
                        );


        System.out.println(
                "Stored Rows    : "
                        + storedPrices.size()
        );


        if (!storedPrices.isEmpty()) {

            StockPrice first =
                    storedPrices.get(0);

            StockPrice last =
                    storedPrices.get(
                            storedPrices.size() - 1
                    );


            System.out.println();
            System.out.println(
                    "----- FIRST DB PRICE -----"
            );

            System.out.println(
                    "Date           : "
                            + first.getTradeDate()
            );

            System.out.println(
                    "Open           : "
                            + first.getOpen()
            );

            System.out.println(
                    "High           : "
                            + first.getHigh()
            );

            System.out.println(
                    "Low            : "
                            + first.getLow()
            );

            System.out.println(
                    "Close          : "
                            + first.getClose()
            );

            System.out.println(
                    "Adjusted Close : "
                            + first.getAdjustedClose()
            );

            System.out.println(
                    "Volume         : "
                            + first.getVolume()
            );


            System.out.println();
            System.out.println(
                    "----- LAST DB PRICE -----"
            );

            System.out.println(
                    "Date           : "
                            + last.getTradeDate()
            );

            System.out.println(
                    "Open           : "
                            + last.getOpen()
            );

            System.out.println(
                    "High           : "
                            + last.getHigh()
            );

            System.out.println(
                    "Low            : "
                            + last.getLow()
            );

            System.out.println(
                    "Close          : "
                            + last.getClose()
            );

            System.out.println(
                    "Adjusted Close : "
                            + last.getAdjustedClose()
            );

            System.out.println(
                    "Volume         : "
                            + last.getVolume()
            );
        }


        System.out.println(
                "======================================"
        );
    }
}



