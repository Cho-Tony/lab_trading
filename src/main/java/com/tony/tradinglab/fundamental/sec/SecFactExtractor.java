package com.tony.tradinglab.fundamental.sec;

import com.tony.tradinglab.fundamental.client.dto.SecCompanyFactsResponse;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SecFactExtractor {

    private static final String US_GAAP = "us-gaap";

    public List<SecFactPoint> extract(
            SecCompanyFactsResponse response,
            List<String> candidateTags,
            String unit
    ) {

        return extract(
                response,
                US_GAAP,
                candidateTags,
                unit
        );
    }


    public List<SecFactPoint> extract(
            SecCompanyFactsResponse response,
            String taxonomy,
            List<String> candidateTags,
            String unit
    ) {

        if (response == null
                || response.facts() == null
                || taxonomy == null
                || candidateTags == null
                || candidateTags.isEmpty()
                || unit == null) {

            return List.of();
        }


        Map<String, SecCompanyFactsResponse.Fact> taxonomyFacts =
                response.facts().get(
                        taxonomy
                );


        if (taxonomyFacts == null
                || taxonomyFacts.isEmpty()) {

            return List.of();
        }


        /*
         * 중요:
         *
         * candidateTags 중 첫 번째로 발견된 tag만
         * 사용하는 것이 아니라,
         *
         * 회사가 시기별로 서로 다른 XBRL tag를
         * 사용했을 가능성을 고려하여
         * 모든 candidate tag의 fact를 합친다.
         *
         * 예:
         *
         * RevenueFromContractWithCustomerExcludingAssessedTax
         * SalesRevenueNet
         * Revenues
         *
         * NVDA처럼 과거/현재 공시에서 tag가 달라질 수 있다.
         */
        return candidateTags.stream()

                .flatMap(
                        tag -> {

                            SecCompanyFactsResponse.Fact fact =
                                    taxonomyFacts.get(
                                            tag
                                    );


                            if (fact == null
                                    || fact.units() == null) {

                                return java.util.stream.Stream.empty();
                            }


                            List<SecCompanyFactsResponse.Unit> units =
                                    fact.units().get(
                                            unit
                                    );


                            if (units == null
                                    || units.isEmpty()) {

                                return java.util.stream.Stream.empty();
                            }


                            return units.stream()

                                    .filter(
                                            this::isSupportedForm
                                    )

                                    .filter(
                                            u ->
                                                    u.val() != null
                                    )

                                    .filter(
                                            u ->
                                                    u.end() != null
                                    )

                                    .filter(
                                            u ->
                                                    u.filed() != null
                                    )

                                    .map(
                                            u ->
                                                    toFactPoint(
                                                            tag,
                                                            u
                                                    )
                                    );
                        }
                )

                /*
                 * PIT 관점에서 먼저 공시된 데이터를
                 * 앞쪽에 둔다.
                 *
                 * 동일 filedDate에서는 candidateTags에
                 * 지정된 tag 순서가 유지된다.
                 */
                .sorted(
                        Comparator.comparing(
                                SecFactPoint::filedDate
                        )
                )

                .toList();
    }

    public Optional<SecFactPoint> findLatestAvailable(
            List<SecFactPoint> facts,
            LocalDate asOfDate
    ) {

        return facts.stream()
                .filter(f ->
                        !f.filedDate().isAfter(asOfDate)
                )
                .max(
                        Comparator.comparing(
                                SecFactPoint::filedDate
                        )
                );
    }

    private boolean isSupportedForm(
            SecCompanyFactsResponse.Unit unit
    ) {

        return "10-Q".equals(unit.form())
                || "10-K".equals(unit.form())
                || "10-Q/A".equals(unit.form())
                || "10-K/A".equals(unit.form());
    }

    private SecFactPoint toFactPoint(
            String tag,
            SecCompanyFactsResponse.Unit unit
    ) {

        return new SecFactPoint(
                tag,
                unit.val(),

                unit.start() != null
                        ? LocalDate.parse(unit.start())
                        : null,

                LocalDate.parse(unit.end()),
                LocalDate.parse(unit.filed()),

                unit.fy(),
                unit.fp(),
                unit.form(),
                unit.accn()
        );
    }

    public void printMatchingTags(
            SecCompanyFactsResponse response,
            String symbol,
            List<String> keywords
    ) {

        if (response == null
                || response.facts() == null
                || keywords == null
                || keywords.isEmpty()) {

            return;
        }


        response.facts()
                .forEach(
                        (taxonomy, taxonomyFacts) -> {

                            if (taxonomyFacts == null
                                    || taxonomyFacts.isEmpty()) {

                                return;
                            }


                            taxonomyFacts.forEach(
                                    (tag, fact) -> {

                                        if (tag == null
                                                || fact == null
                                                || fact.units() == null) {

                                            return;
                                        }


                                        String normalizedTag =
                                                tag.toLowerCase(
                                                        java.util.Locale.ROOT
                                                );


                                        boolean matches =
                                                keywords.stream()
                                                        .map(
                                                                keyword ->
                                                                        keyword.toLowerCase(
                                                                                java.util.Locale.ROOT
                                                                        )
                                                        )
                                                        .anyMatch(
                                                                normalizedTag::contains
                                                        );


                                        if (!matches) {

                                            return;
                                        }


                                        fact.units()
                                                .forEach(
                                                        (unitName, units) -> {

                                                            if (units == null
                                                                    || units.isEmpty()) {

                                                                return;
                                                            }


                                                            long supportedCount =
                                                                    units.stream()
                                                                            .filter(
                                                                                    this::isSupportedForm
                                                                            )
                                                                            .count();


                                                            System.out.println(
                                                                    "[FACT TAG]"
                                                                            + " symbol="
                                                                            + symbol
                                                                            + " | taxonomy="
                                                                            + taxonomy
                                                                            + " | tag="
                                                                            + tag
                                                                            + " | unit="
                                                                            + unitName
                                                                            + " | total="
                                                                            + units.size()
                                                                            + " | supported="
                                                                            + supportedCount
                                                            );
                                                        }
                                                );
                                    }
                            );
                        }
                );
    }
}