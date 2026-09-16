package com.tony.tradinglab.smoke;

import com.tony.tradinglab.fundamental.domain.ValuationPeerSnapshotInput;
import com.tony.tradinglab.fundamental.service.PointInTimeValuationSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@Profile("smoke")
@RequiredArgsConstructor
public class RealFundamentalSmokeRunner
        implements CommandLineRunner {

    private final PointInTimeValuationSnapshotService
            pointInTimeValuationSnapshotService;


    @Override
    public void run(
            String... args
    ) {

        testPointInTimeValuationSnapshotService();


        System.out.println(
                "======================================"
        );

        System.out.println(
                " END"
        );

        System.out.println(
                "======================================"
        );
    }


    private void testPointInTimeValuationSnapshotService() {

        String symbol =
                "AAPL";

        String exchange =
                "NASDAQ";

        LocalDate observationDate =
                LocalDate.of(
                        2026,
                        7,
                        31
                );


        System.out.println();
        System.out.println(
                "======================================"
        );

        System.out.println(
                " POINT-IN-TIME VALUATION SNAPSHOT"
        );

        System.out.println(
                "======================================"
        );


        Optional<ValuationPeerSnapshotInput> result =
                pointInTimeValuationSnapshotService
                        .analyze(
                                symbol,
                                exchange,
                                observationDate
                        );


        if (result.isEmpty()) {

            System.out.println(
                    "Valuation Snapshot 생성 실패"
            );

            System.out.println(
                    "======================================"
            );

            return;
        }


        ValuationPeerSnapshotInput snapshot =
                result.get();


        System.out.println(
                "Stock ID         : "
                        + snapshot.stockId()
        );

        System.out.println(
                "Symbol           : "
                        + snapshot.symbol()
        );

        System.out.println(
                "Observation Date : "
                        + snapshot.observationDate()
        );


        System.out.println();
        System.out.println(
                "----- VALUATION -----"
        );

        System.out.println(
                "Price Date       : "
                        + snapshot
                        .valuation()
                        .priceDate()
        );

        System.out.println(
                "Share Price      : "
                        + snapshot
                        .valuation()
                        .sharePrice()
        );

        System.out.println(
                "Shares           : "
                        + snapshot
                        .valuation()
                        .sharesOutstanding()
        );

        System.out.println(
                "Market Cap       : "
                        + snapshot
                        .valuation()
                        .marketCap()
        );

        System.out.println(
                "P/E              : "
                        + snapshot
                        .valuation()
                        .peRatio()
        );

        System.out.println(
                "P/S              : "
                        + snapshot
                        .valuation()
                        .psRatio()
        );

        System.out.println(
                "P/FCF            : "
                        + snapshot
                        .valuation()
                        .priceToFcfRatio()
        );


        System.out.println();
        System.out.println(
                "----- GROWTH -----"
        );

        System.out.println(
                snapshot.growth()
        );


        System.out.println();
        System.out.println(
                "----- PROFITABILITY -----"
        );

        System.out.println(
                snapshot.profitability()
        );


        System.out.println();
        System.out.println(
                "----- COMPLETE SNAPSHOT -----"
        );

        System.out.println(
                snapshot
        );


        System.out.println(
                "======================================"
        );
    }
}