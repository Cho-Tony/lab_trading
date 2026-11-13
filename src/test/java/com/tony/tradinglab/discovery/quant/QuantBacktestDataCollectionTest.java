package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.fundamental.domain.*;
import com.tony.tradinglab.fundamental.service.*;
import com.tony.tradinglab.price.domain.StockPrice;
import com.tony.tradinglab.price.service.MarketPriceProvider;
import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import com.tony.tradinglab.universe.UniverseTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.tony.tradinglab.fundamental.domain.FundamentalDataCollectionPolicy.READ_ONLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercises the real service chain; all persistence, collection and return-data boundaries are mocked. */
class QuantBacktestDataCollectionTest {

    private static final LocalDate AS_OF = LocalDate.of(2021, 6, 30);
    private static final LocalDate FILED = LocalDate.of(2021, 5, 10);
    private static final UniverseTarget TARGET =
            new UniverseTarget("ABC", "ABC Inc.", "NASDAQ", "US", "USD", "Technology", "Software");

    private FinancialStatementQueryService query;
    private FundamentalDataSyncService sync;
    private PointInTimeFundamentalAnalysisService analysis;
    private TtmCashFlowCalculator cashFlows;
    private MarketPriceProvider prices;
    private PointInTimeValuationComparisonService comparison;
    private QuantBacktestSampleFactory samples;
    private PointInTimeValuationSnapshotService snapshots;
    private QuantDiscoveryEngine discovery;
    private QuantBacktestService backtest;

    @BeforeEach
    void setUp() {
        query = mock(FinancialStatementQueryService.class);
        sync = mock(FundamentalDataSyncService.class);
        analysis = mock(PointInTimeFundamentalAnalysisService.class);
        cashFlows = mock(TtmCashFlowCalculator.class);
        prices = mock(MarketPriceProvider.class);
        comparison = mock(PointInTimeValuationComparisonService.class);
        samples = mock(QuantBacktestSampleFactory.class);
        StockRepository stocks = mock(StockRepository.class);
        Stock stock = mock(Stock.class);
        when(stock.getId()).thenReturn(1L);
        when(stocks.findListedBySymbolAndExchangeAsOf("ABC", "NASDAQ", AS_OF))
                .thenReturn(Optional.of(stock));
        when(query.findStockAsOf("ABC", "NASDAQ", AS_OF)).thenReturn(stock);

        snapshots = new PointInTimeValuationSnapshotService(stocks, new FundamentalDataProvider(query, sync),
                prices, analysis, cashFlows, new ValuationMetricsCalculator(),
                new ValuationPeerSnapshotInputFactory());
        QuantUniverseProvider universe = mock(QuantUniverseProvider.class);
        when(universe.getUniverse(AS_OF)).thenReturn(List.of(TARGET));
        discovery = new DefaultQuantDiscoveryEngine(universe, snapshots, comparison, new DefaultQuantScorer());
        backtest = new QuantBacktestService(discovery, universe, samples);
    }

    @Test
    void backtestDoesNotCollectOrScoreWhenNoFinancialDataIsStored() {
        when(query.findAll(1L)).thenReturn(List.of());
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of());

        assertThat(backtest.run(AS_OF)).isEmpty();

        verify(query).findAsOf(1L, AS_OF);
        verifyNoInteractions(sync, analysis, cashFlows, prices, comparison, samples);
    }

    @Test
    void backtestBuildsSnapshotAndPreservesExistingQuantScoresFromStoredData() {
        FinancialStatementData statement = statement(FILED);
        when(query.findAll(1L)).thenReturn(List.of(statement));
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of(statement));
        enableSuccessfulSnapshotAndComparison();
        QuantBacktestSample sample = mock(QuantBacktestSample.class);
        when(samples.create(any(QuantDiscoveryCandidate.class), eq("NASDAQ"))).thenReturn(Optional.of(sample));

        assertThat(backtest.run(AS_OF)).containsExactly(sample);

        ArgumentCaptor<QuantDiscoveryCandidate> candidate = ArgumentCaptor.forClass(QuantDiscoveryCandidate.class);
        verify(samples).create(candidate.capture(), eq("NASDAQ"));
        assertCandidate(candidate.getValue());
        ArgumentCaptor<PointInTimeFundamentalContext> context =
                ArgumentCaptor.forClass(PointInTimeFundamentalContext.class);
        verify(analysis).analyze(context.capture(), anyList());
        assertThat(context.getValue().asOfDate()).isEqualTo(AS_OF);
        assertThat(context.getValue().quarterlyFinancials()).hasSize(1);
        verify(query).findAsOf(1L, AS_OF);
        verifyNoInteractions(sync);
    }

    @Test
    void backtestDoesNotUseFutureOnlyStoredFinancialsOrCollectMissingHistory() {
        when(query.findAll(1L)).thenReturn(List.of(statement(AS_OF.plusDays(1))));
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of());

        assertThat(backtest.run(AS_OF)).isEmpty();

        verify(query).findAsOf(1L, AS_OF);
        verifyNoInteractions(sync, analysis, cashFlows, prices, comparison, samples);
    }

    @Test
    void existingPitGuardStillRejectsAFutureStatementEvenIfTheQueryReturnsIt() {
        FinancialStatementData future = statement(AS_OF.plusDays(1));
        when(query.findAll(1L)).thenReturn(List.of(future));
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of(future));

        assertThat(backtest.run(AS_OF)).isEmpty();

        verifyNoInteractions(sync, analysis, cashFlows, prices, comparison, samples);
    }

    @Test
    void snapshotFailureStillSkipsTheStockWithoutRetryingCollection() {
        FinancialStatementData statement = statement(FILED);
        when(query.findAll(1L)).thenReturn(List.of(statement));
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of(statement));
        when(analysis.analyze(any(PointInTimeFundamentalContext.class), anyList()))
                .thenThrow(new IllegalStateException("Unavailable fundamental analysis"));

        assertThat(backtest.run(AS_OF)).isEmpty();

        verify(analysis).analyze(any(PointInTimeFundamentalContext.class), anyList());
        verifyNoInteractions(sync, cashFlows, prices, comparison, samples);
    }

    @Test
    void existingDiscoveryApiStillAutoSyncsAndProducesTheSameScores() {
        when(query.findAll(1L)).thenReturn(List.of());
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of(statement(FILED)));
        enableSuccessfulSnapshotAndComparison();

        assertThat(discovery.discover(AS_OF)).singleElement().satisfies(this::assertCandidate);

        InOrder order = inOrder(query, sync);
        order.verify(query).findStockAsOf("ABC", "NASDAQ", AS_OF);
        order.verify(query).findAll(1L);
        order.verify(sync).sync("ABC", "NASDAQ");
        order.verify(query).findAsOf(1L, AS_OF);
    }

    @Test
    void existingSnapshotApiStillAllowsAutoSync() {
        when(query.findAll(1L)).thenReturn(List.of());
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of(statement(FILED)));
        enableSuccessfulSnapshotAndComparison();

        assertThat(snapshots.analyze("ABC", "NASDAQ", AS_OF)).isPresent();

        verify(sync).sync("ABC", "NASDAQ");
    }

    @Test
    void backtestCollectionUsesReadOnlyForEveryDistinctObservationDate() {
        QuantDiscoveryEngine engine = mock(QuantDiscoveryEngine.class);
        QuantUniverseProvider universe = mock(QuantUniverseProvider.class);
        QuantBacktestSampleFactory factory = mock(QuantBacktestSampleFactory.class);
        when(universe.getUniverse(any(LocalDate.class))).thenReturn(List.of(TARGET));
        when(engine.discover(any(LocalDate.class), eq(READ_ONLY))).thenReturn(List.of());
        QuantBacktestService service = new QuantBacktestService(engine, universe, factory);
        LocalDate later = AS_OF.plusMonths(3);

        assertThat(service.run(Arrays.asList(later, AS_OF, AS_OF, null))).isEmpty();

        InOrder order = inOrder(engine);
        order.verify(engine).discover(AS_OF, READ_ONLY);
        order.verify(engine).discover(later, READ_ONLY);
        verifyNoMoreInteractions(engine);
        verifyNoInteractions(factory);
    }

    private void enableSuccessfulSnapshotAndComparison() {
        TtmFinancials ttm = new TtmFinancials(2021, "Q1", decimal("1000"), decimal("200"),
                decimal("100"), FILED);
        GrowthTrendAnalysis growth = new GrowthTrendAnalysis(GrowthTrend.ACCELERATING,
                decimal("25"), decimal("2"), 2, 0, 3);
        PointInTimeFundamentalAnalysis fundamental =
                new PointInTimeFundamentalAnalysis(1L, "ABC", AS_OF, ttm, growth, null);
        when(analysis.analyze(any(PointInTimeFundamentalContext.class), anyList()))
                .thenReturn(Optional.of(fundamental));
        when(cashFlows.calculate(anyList())).thenReturn(List.of(
                new TtmCashFlow(2021, "Q1", decimal("100"), decimal("20"), decimal("80"), FILED)));
        StockPrice price = mock(StockPrice.class);
        when(price.getTradeDate()).thenReturn(AS_OF);
        when(price.getClose()).thenReturn(decimal("20"));
        when(prices.getAsOf("ABC", "NASDAQ", AS_OF)).thenReturn(Optional.of(price));
        PercentileValuationAssessment percentile = new PercentileValuationAssessment("ABC",
                decimal("50"), decimal("60"), decimal("50"), 10, 10, 10);
        when(comparison.compare(eq(1L), eq(AS_OF), anyList())).thenReturn(Optional.of(
                new ValuationComparisonResult(1L, "ABC", AS_OF, PeerSelectionLevel.INDUSTRY_AND_SIZE,
                        10, percentile, null, null)));
    }

    private void assertCandidate(QuantDiscoveryCandidate candidate) {
        assertThat(candidate.stockId()).isEqualTo(1L);
        assertThat(candidate.symbol()).isEqualTo("ABC");
        assertThat(candidate.observationDate()).isEqualTo(AS_OF);
        // Growth = 25/(25+25)*100 + 2; quality = 45*10/30 + 45*8/28.
        assertThat(candidate.score().growthScore()).isEqualByComparingTo("52.00");
        assertThat(candidate.score().qualityScore()).isEqualByComparingTo("27.86");
        // Valuation = (50 + 40 + 50)/3; total = (52 + 27.86 + 46.67)/3.
        assertThat(candidate.score().valuationScore()).isEqualByComparingTo("46.67");
        assertThat(candidate.score().valuationAvailable()).isTrue();
        assertThat(candidate.score().totalScore()).isEqualByComparingTo("42.18");
    }

    private FinancialStatementData statement(LocalDate filedDate) {
        return new FinancialStatementData("ABC", LocalDate.of(2021, 3, 31), filedDate, 2021, "Q1",
                decimal("1000"), decimal("200"), decimal("100"), null, null, null, null,
                decimal("100"), decimal("20"), decimal("10"));
    }

    private BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
