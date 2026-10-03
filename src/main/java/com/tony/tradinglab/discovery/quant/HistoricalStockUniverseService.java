package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricalStockUniverseService {

    private final StockRepository stockRepository;


    public List<Stock> getStocksAsOf(
            LocalDate observationDate
    ) {

        if (observationDate == null) {
            return List.of();
        }


        return stockRepository
                .findCommonStocksListedAsOf(
                        observationDate
                );
    }
}