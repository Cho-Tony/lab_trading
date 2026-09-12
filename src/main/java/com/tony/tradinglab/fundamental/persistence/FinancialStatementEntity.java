package com.tony.tradinglab.fundamental.persistence;

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
        name = "financial_statements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_financial_statements_stock_period_filed",
                        columnNames = {
                                "stock_id",
                                "period_end_date",
                                "filed_date"
                        }
                )
        }
)
@NoArgsConstructor(access = PROTECTED)
public class FinancialStatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(
            fetch = LAZY,
            optional = false
    )
    @JoinColumn(
            name = "stock_id",
            nullable = false
    )
    private Stock stock;


    @Column(
            name = "period_end_date",
            nullable = false
    )
    private LocalDate periodEndDate;


    @Column(
            name = "filed_date",
            nullable = false
    )
    private LocalDate filedDate;


    @Column(
            name = "fiscal_year",
            nullable = false
    )
    private Integer fiscalYear;


    @Column(
            name = "fiscal_quarter",
            nullable = false,
            length = 10
    )
    private String fiscalQuarter;


    @Column(
            precision = 38,
            scale = 6
    )
    private BigDecimal revenue;


    @Column(
            name = "operating_income",
            precision = 38,
            scale = 6
    )
    private BigDecimal operatingIncome;


    @Column(
            name = "net_income",
            precision = 38,
            scale = 6
    )
    private BigDecimal netIncome;


    @Column(
            name = "total_assets",
            precision = 38,
            scale = 6
    )
    private BigDecimal totalAssets;


    @Column(
            name = "total_equity",
            precision = 38,
            scale = 6
    )
    private BigDecimal totalEquity;


    @Column(
            name = "total_debt",
            precision = 38,
            scale = 6
    )
    private BigDecimal totalDebt;


    @Column(
            precision = 38,
            scale = 6
    )
    private BigDecimal cash;


    @Column(
            name = "operating_cash_flow",
            precision = 38,
            scale = 6
    )
    private BigDecimal operatingCashFlow;


    @Column(
            name = "capital_expenditure",
            precision = 38,
            scale = 6
    )
    private BigDecimal capitalExpenditure;


    @Column(
            name = "shares_outstanding",
            precision = 38,
            scale = 6
    )
    private BigDecimal sharesOutstanding;


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


    public FinancialStatementEntity(
            Stock stock,
            LocalDate periodEndDate,
            LocalDate filedDate,
            Integer fiscalYear,
            String fiscalQuarter,
            BigDecimal revenue,
            BigDecimal operatingIncome,
            BigDecimal netIncome,
            BigDecimal totalAssets,
            BigDecimal totalEquity,
            BigDecimal totalDebt,
            BigDecimal cash,
            BigDecimal operatingCashFlow,
            BigDecimal capitalExpenditure,
            BigDecimal sharesOutstanding
    ) {

        this.stock =
                stock;

        this.periodEndDate =
                periodEndDate;

        this.filedDate =
                filedDate;

        this.fiscalYear =
                fiscalYear;

        this.fiscalQuarter =
                fiscalQuarter;

        this.revenue =
                revenue;

        this.operatingIncome =
                operatingIncome;

        this.netIncome =
                netIncome;

        this.totalAssets =
                totalAssets;

        this.totalEquity =
                totalEquity;

        this.totalDebt =
                totalDebt;

        this.cash =
                cash;

        this.operatingCashFlow =
                operatingCashFlow;

        this.capitalExpenditure =
                capitalExpenditure;

        this.sharesOutstanding =
                sharesOutstanding;
    }
}