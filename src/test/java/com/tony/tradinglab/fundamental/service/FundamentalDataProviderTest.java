package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.dto.FinancialStatementData;
import com.tony.tradinglab.stock.domain.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.LocalDate;
import java.util.List;

import static com.tony.tradinglab.fundamental.domain.FundamentalDataCollectionPolicy.READ_ONLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class FundamentalDataProviderTest {

    private static final LocalDate AS_OF = LocalDate.of(2021, 6, 30);

    private FinancialStatementQueryService query;
    private FundamentalDataSyncService sync;
    private FundamentalDataProvider provider;

    @BeforeEach
    void setUp() {
        query = mock(FinancialStatementQueryService.class);
        sync = mock(FundamentalDataSyncService.class);
        provider = new FundamentalDataProvider(query, sync);
        Stock stock = mock(Stock.class);
        when(stock.getId()).thenReturn(1L);
        when(query.findStockAsOf("ABC", "NASDAQ", AS_OF)).thenReturn(stock);
    }

    @Test
    void readOnlyDoesNotSyncWhenNoFinancialDataIsStored() {
        when(query.findAll(1L)).thenReturn(List.of());
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of());

        assertThat(provider.getAsOf("ABC", "NASDAQ", AS_OF, READ_ONLY)).isEmpty();

        verify(query).findAsOf(1L, AS_OF);
        verifyNoInteractions(sync);
    }

    @Test
    void readOnlyReturnsOnlyTheAsOfQueryResult() {
        FinancialStatementData available = mock(FinancialStatementData.class);
        FinancialStatementData future = mock(FinancialStatementData.class);
        when(query.findAll(1L)).thenReturn(List.of(available, future));
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of(available));

        assertThat(provider.getAsOf("ABC", "NASDAQ", AS_OF, READ_ONLY)).containsExactly(available);

        verify(query).findAsOf(1L, AS_OF);
        verifyNoInteractions(sync);
    }

    @Test
    void readOnlyDoesNotUseStoredFutureDataOrTryToFillTheGap() {
        FinancialStatementData future = mock(FinancialStatementData.class);
        when(query.findAll(1L)).thenReturn(List.of(future));
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of());

        assertThat(provider.getAsOf("ABC", "NASDAQ", AS_OF, READ_ONLY)).isEmpty();

        verify(query).findAsOf(1L, AS_OF);
        verifyNoInteractions(sync);
    }

    @Test
    void existingApiStillSyncsEmptyStorageEvenForAPastObservationDate() {
        FinancialStatementData available = mock(FinancialStatementData.class);
        when(query.findAll(1L)).thenReturn(List.of());
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of(available));

        assertThat(provider.getAsOf("ABC", "NASDAQ", AS_OF)).containsExactly(available);

        InOrder order = inOrder(query, sync);
        order.verify(query).findStockAsOf("ABC", "NASDAQ", AS_OF);
        order.verify(query).findAll(1L);
        order.verify(sync).sync("ABC", "NASDAQ");
        order.verify(query).findAsOf(1L, AS_OF);
    }

    @Test
    void existingApiDoesNotSyncWhenDataIsAlreadyStored() {
        FinancialStatementData available = mock(FinancialStatementData.class);
        when(query.findAll(1L)).thenReturn(List.of(available));
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of(available));

        assertThat(provider.getAsOf("ABC", "NASDAQ", AS_OF)).containsExactly(available);

        verifyNoInteractions(sync);
    }

    @Test
    void existingApiRetainsItsNoRefreshBehaviorWhenOnlyFutureDataIsStored() {
        when(query.findAll(1L)).thenReturn(List.of(mock(FinancialStatementData.class)));
        when(query.findAsOf(1L, AS_OF)).thenReturn(List.of());

        assertThat(provider.getAsOf("ABC", "NASDAQ", AS_OF)).isEmpty();

        verifyNoInteractions(sync);
    }

    @Test
    void missingPolicyCannotFallBackToAutomaticCollection() {
        assertThatThrownBy(() -> provider.getAsOf("ABC", "NASDAQ", AS_OF, null))
                .isInstanceOf(NullPointerException.class).hasMessageContaining("collectionPolicy");

        verifyNoInteractions(query, sync);
    }
}
