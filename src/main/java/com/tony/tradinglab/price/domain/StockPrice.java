package com.tony.tradinglab.price.domain;

import com.tony.tradinglab.stock.domain.Stock;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
        name = "stock_prices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_prices_stock_date",
                        columnNames = {"stock_id", "trade_date"}
                )
        }
)
@NoArgsConstructor(access = PROTECTED)
public class StockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal open;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal high;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal low;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal close;

    @Column(name = "adjusted_close", precision = 19, scale = 6)
    private BigDecimal adjustedClose;

    @Column(nullable = false)
    private Long volume;

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private LocalDateTime updatedAt;

    public StockPrice(
            Stock stock,
            LocalDate tradeDate,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal adjustedClose,
            Long volume
    ) {
        this.stock = stock;
        this.tradeDate = tradeDate;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.adjustedClose = adjustedClose;
        this.volume = volume;
    }
}
