package com.tony.tradinglab.backtest.outcome;

import com.tony.tradinglab.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TerminalOutcomeResolver {

    private static final int ACTION_LOOKAHEAD_DAYS = 7;


    private static final String FIND_ACTIONS_SQL = """
            SELECT
                action_date,
                action_type
            FROM sharadar_corporate_actions
            WHERE stock_id = ?
              AND action_date >= ?
              AND action_date <= ?
            ORDER BY
                action_date,
                id
            """;


    private final JdbcTemplate jdbcTemplate;


    public TerminalOutcomeType resolve(
            Stock stock
    ) {

        if (stock == null) {

            throw new IllegalArgumentException(
                    "stock must not be null."
            );
        }


        if (stock.getId() == null) {

            throw new IllegalArgumentException(
                    "stock.id must not be null."
            );
        }


        if (stock.getListingEndDate() == null) {

            throw new IllegalArgumentException(
                    "stock.listingEndDate must not be null."
            );
        }


        return resolve(
                stock.getId(),
                stock.getListingEndDate()
        );
    }


    public TerminalOutcomeType resolve(
            Long stockId,
            LocalDate listingEndDate
    ) {

        if (stockId == null) {

            throw new IllegalArgumentException(
                    "stockId must not be null."
            );
        }


        if (listingEndDate == null) {

            throw new IllegalArgumentException(
                    "listingEndDate must not be null."
            );
        }


        LocalDate actionWindowEnd =
                listingEndDate.plusDays(
                        ACTION_LOOKAHEAD_DAYS
                );


        List<ActionRecord> actions =
                jdbcTemplate.query(
                        FIND_ACTIONS_SQL,
                        (rs, rowNum) ->
                                new ActionRecord(
                                        rs.getObject(
                                                "action_date",
                                                LocalDate.class
                                        ),
                                        normalizeActionType(
                                                rs.getString(
                                                        "action_type"
                                                )
                                        )
                                ),
                        stockId,
                        listingEndDate,
                        actionWindowEnd
                );


        return classify(
                listingEndDate,
                actions
        );
    }


    private TerminalOutcomeType classify(
            LocalDate listingEndDate,
            List<ActionRecord> actions
    ) {

        boolean bankruptcyLiquidation =
                hasActionOnListingEndDate(
                        actions,
                        listingEndDate,
                        "bankruptcyliquidation"
                );


        boolean acquisition =
                hasActionStartingWithOnListingEndDate(
                        actions,
                        listingEndDate,
                        "acquisition"
                );


        boolean merger =
                hasAnyActionOnListingEndDate(
                        actions,
                        listingEndDate,
                        "mergerfrom",
                        "mergerto",
                        "spacmerger"
                );


        boolean regulatoryDelisting =
                hasActionOnListingEndDate(
                        actions,
                        listingEndDate,
                        "regulatorydelisting"
                );


        boolean voluntaryDelisting =
                hasActionOnListingEndDate(
                        actions,
                        listingEndDate,
                        "voluntarydelisting"
                );


        /*
         * YHOO처럼 listing 종료 후 며칠 뒤
         * spinoff가 기록되는 사례가 있으므로
         * spinoff는 +7일 window 전체를 확인한다.
         */
        boolean spinOff =
                hasAction(
                        actions,
                        "spinoff"
                );


        /*
         * 파산/청산은 가장 강한 terminal 원인으로 본다.
         */
        if (bankruptcyLiquidation) {

            return TerminalOutcomeType
                    .BANKRUPTCY_LIQUIDATION;
        }


        /*
         * 예:
         * BHVN1
         *
         * acquisitioncash + spinoff
         *
         * 단순 acquisition과 terminal wealth 계산 방식이
         * 달라질 수 있으므로 별도 분류한다.
         */
        if (acquisition
                && spinOff) {

            return TerminalOutcomeType
                    .ACQUISITION_WITH_SPINOFF;
        }


        if (acquisition) {

            return TerminalOutcomeType
                    .ACQUISITION;
        }


        /*
         * 예:
         * YHOO -> AABA
         * LGF.B -> LION + STRZ
         *
         * acquisition 없이 spinoff만 존재하는
         * 특수 continuation 유형.
         */
        if (spinOff) {

            return TerminalOutcomeType
                    .SPINOFF_CONTINUATION;
        }


        if (merger) {

            return TerminalOutcomeType
                    .MERGER;
        }


        if (regulatoryDelisting) {

            return TerminalOutcomeType
                    .REGULATORY_DELISTING;
        }


        if (voluntaryDelisting) {

            return TerminalOutcomeType
                    .VOLUNTARY_DELISTING;
        }


        return TerminalOutcomeType
                .OTHER_DELISTING;
    }


    private boolean hasAction(
            List<ActionRecord> actions,
            String actionType
    ) {

        return actions.stream()
                .anyMatch(
                        action ->
                                actionType.equals(
                                        action.actionType()
                                )
                );
    }


    private boolean hasActionOnListingEndDate(
            List<ActionRecord> actions,
            LocalDate listingEndDate,
            String actionType
    ) {

        return actions.stream()
                .anyMatch(
                        action ->
                                listingEndDate.equals(
                                        action.actionDate()
                                )
                                        && actionType.equals(
                                        action.actionType()
                                )
                );
    }


    private boolean hasActionStartingWithOnListingEndDate(
            List<ActionRecord> actions,
            LocalDate listingEndDate,
            String prefix
    ) {

        return actions.stream()
                .anyMatch(
                        action ->
                                listingEndDate.equals(
                                        action.actionDate()
                                )
                                        && action.actionType()
                                        .startsWith(
                                                prefix
                                        )
                );
    }


    private boolean hasAnyActionOnListingEndDate(
            List<ActionRecord> actions,
            LocalDate listingEndDate,
            String... actionTypes
    ) {

        for (String actionType : actionTypes) {

            if (hasActionOnListingEndDate(
                    actions,
                    listingEndDate,
                    actionType
            )) {

                return true;
            }
        }


        return false;
    }


    private String normalizeActionType(
            String actionType
    ) {

        if (actionType == null) {
            return "";
        }


        return actionType
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }


    private record ActionRecord(
            LocalDate actionDate,
            String actionType
    ) {
    }
}