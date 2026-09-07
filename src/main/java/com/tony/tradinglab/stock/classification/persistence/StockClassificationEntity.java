package com.tony.tradinglab.stock.classification.persistence;

import com.tony.tradinglab.stock.classification.domain.ClassificationSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(
        name = "stock_classifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_stock_classifications_stock_from",
                        columnNames = {
                                "stock_id",
                                "effective_from"
                        }
                )
        }
)
@NoArgsConstructor(access = PROTECTED)
public class StockClassificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
            name = "stock_id",
            nullable = false
    )
    private Long stockId;


    @Column(
            nullable = false,
            length = 100
    )
    private String sector;


    @Column(
            nullable = false,
            length = 150
    )
    private String industry;


    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom;


    @Column(
            name = "effective_to"
    )
    private LocalDate effectiveTo;


    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private ClassificationSource source;


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


    public StockClassificationEntity(
            Long stockId,
            String sector,
            String industry,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            ClassificationSource source
    ) {

        this.stockId = stockId;
        this.sector = sector;
        this.industry = industry;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.source = source;
    }
}