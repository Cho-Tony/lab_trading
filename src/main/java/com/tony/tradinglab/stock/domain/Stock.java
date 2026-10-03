package com.tony.tradinglab.stock.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @Column(name = "data_source", length = 32)
    private String dataSource;


    @Column(name = "source_security_id", length = 64)
    private String sourceSecurityId;


    @Column(name = "security_type", length = 64)
    private String securityType;


    @Column(name = "listing_start_date")
    private LocalDate listingStartDate;


    @Column(name = "listing_end_date")
    private LocalDate listingEndDate;


    @Column(name = "delisting_reason", length = 255)
    private String delistingReason;

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

    public void applyHistoricalMasterData(
            String name,
            String market,
            String currency,
            boolean active,
            String dataSource,
            String sourceSecurityId,
            String securityType,
            LocalDate listingStartDate,
            LocalDate listingEndDate,
            String delistingReason
    ) {

        if (name != null && !name.isBlank()) {
            this.name = name;
        }

        if (market != null && !market.isBlank()) {
            this.market = market;
        }

        if (currency != null && !currency.isBlank()) {
            this.currency = currency;
        }

        this.active = active;

        this.dataSource = dataSource;
        this.sourceSecurityId = sourceSecurityId;
        this.securityType = securityType;

        this.listingStartDate = listingStartDate;
        this.listingEndDate = listingEndDate;

        this.delistingReason = delistingReason;
    }
}