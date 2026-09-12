package com.tony.tradinglab.fundamental.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialStatementRepository
        extends JpaRepository<FinancialStatementEntity, Long> {


    /*
     * 동일 공시 데이터가 이미 저장되어 있는지 확인할 때 사용.
     *
     * unique key:
     * stock_id + period_end_date + filed_date
     */
    boolean existsByStockIdAndPeriodEndDateAndFiledDate(
            Long stockId,
            LocalDate periodEndDate,
            LocalDate filedDate
    );


    /*
     * 특정 공시 1건 조회.
     */
    Optional<FinancialStatementEntity>
    findByStockIdAndPeriodEndDateAndFiledDate(
            Long stockId,
            LocalDate periodEndDate,
            LocalDate filedDate
    );


    /*
     * 특정 종목의 전체 재무 데이터.
     *
     * API → DB sync 후 검증할 때도 사용할 수 있다.
     */
    List<FinancialStatementEntity>
    findByStockIdOrderByPeriodEndDateAscFiledDateAsc(
            Long stockId
    );


    /*
     * Point-in-Time 조회.
     *
     * asOfDate 시점까지 시장에 공개된
     * 재무정보만 조회한다.
     *
     * filed_date <= asOfDate
     */
    List<FinancialStatementEntity>
    findByStockIdAndFiledDateLessThanEqualOrderByPeriodEndDateAscFiledDateAsc(
            Long stockId,
            LocalDate asOfDate
    );
}