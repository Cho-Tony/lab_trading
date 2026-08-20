package com.tony.tradinglab.stock.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
        name = "stocks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stocks_symbol_exchange",
                        columnNames = {"symbol", "exchange"}
                )
        }
)
@NoArgsConstructor(access = PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String exchange;

    @Column(length = 30)
    private String market;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Stock(
            String symbol,
            String name,
            String exchange,
            String market,
            String currency
    ) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
        this.market = market;
        this.currency = currency;
    }
}