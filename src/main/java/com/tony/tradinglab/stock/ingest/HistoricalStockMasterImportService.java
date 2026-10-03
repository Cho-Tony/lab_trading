package com.tony.tradinglab.stock.ingest;

import com.tony.tradinglab.stock.domain.Stock;
import com.tony.tradinglab.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class HistoricalStockMasterImportService {

    private final HistoricalStockMasterSource stockMasterSource;
    private final StockRepository stockRepository;


    @Transactional
    public ImportResult importAll() {

        List<HistoricalStockMasterRecord> records =
                stockMasterSource.loadAll();


        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int conflicts = 0;


        for (HistoricalStockMasterRecord record : records) {

            if (!isUsable(record)) {

                skipped++;

                continue;
            }


            List<Stock> sourceMatches =
                    stockRepository
                            .findAllByDataSourceAndSourceSecurityId(
                                    record.dataSource(),
                                    record.sourceSecurityId()
                            );


            if (sourceMatches.size() > 1) {

                conflicts++;

                continue;
            }


            Stock stock;


            if (sourceMatches.size() == 1) {

                /*
                 * 가장 신뢰할 수 있는 매칭.
                 *
                 * 외부 provider permanent ID가
                 * 이미 우리 stock에 연결되어 있다.
                 */
                stock =
                        sourceMatches.get(0);


                applyHistoricalData(
                        stock,
                        record
                );


                stockRepository.save(
                        stock
                );


                updated++;

                continue;
            }


            /*
             * 아직 sourceSecurityId가 연결되지 않은
             * 기존 stock일 수 있다.
             *
             * 예:
             * 기존에 수동으로 넣어둔 AAPL / NASDAQ
             */
            List<Stock> symbolMatches =
                    stockRepository
                            .findAllBySymbolAndExchange(
                                    record.symbol(),
                                    record.exchange()
                            );


            if (symbolMatches.size() > 1) {

                /*
                 * V6 이후에는 ticker 재사용이 가능하므로
                 * 여러 개면 임의로 하나를 선택하지 않는다.
                 */
                conflicts++;

                continue;
            }


            if (symbolMatches.size() == 1) {

                stock =
                        symbolMatches.get(0);


                applyHistoricalData(
                        stock,
                        record
                );


                stockRepository.save(
                        stock
                );


                updated++;

                continue;
            }


            /*
             * 완전히 새로운 historical stock.
             *
             * 상장폐지된 회사도 여기서 그대로 생성된다.
             */
            stock =
                    new Stock(
                            record.symbol(),
                            record.name(),
                            record.exchange(),
                            record.market(),
                            record.currency()
                    );


            applyHistoricalData(
                    stock,
                    record
            );


            stockRepository.save(
                    stock
            );


            inserted++;
        }


        return new ImportResult(
                records.size(),
                inserted,
                updated,
                skipped,
                conflicts
        );
    }


    private void applyHistoricalData(
            Stock stock,
            HistoricalStockMasterRecord record
    ) {

        stock.applyHistoricalMasterData(
                record.name(),
                record.market(),
                record.currency(),
                record.active(),
                record.dataSource(),
                record.sourceSecurityId(),
                record.securityType(),
                record.listingStartDate(),
                record.listingEndDate(),
                record.delistingReason()
        );
    }


    private boolean isUsable(
            HistoricalStockMasterRecord record
    ) {

        if (record == null) {
            return false;
        }


        return hasText(record.dataSource())
                && hasText(record.sourceSecurityId())
                && hasText(record.symbol())
                && hasText(record.exchange())
                && hasText(record.currency());
    }


    private boolean hasText(
            String value
    ) {

        return value != null
                && !value.trim().isEmpty();
    }


    public record ImportResult(
            int sourceRecordCount,
            int insertedCount,
            int updatedCount,
            int skippedCount,
            int conflictCount
    ) {
    }
}