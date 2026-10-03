package com.tony.tradinglab.stock.ingest;

import java.util.List;

public interface HistoricalStockMasterSource {

    List<HistoricalStockMasterRecord> loadAll();
}