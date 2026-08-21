package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.dto.SecCompanyFactsResponse;
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

        Map<String, SecCompanyFactsResponse.Fact> usGaap =
                response.facts().get(US_GAAP);

        if (usGaap == null) {
            return List.of();
        }

        for (String tag : candidateTags) {

            SecCompanyFactsResponse.Fact fact =
                    usGaap.get(tag);

            if (fact == null) {
                continue;
            }

            List<SecCompanyFactsResponse.Unit> units =
                    fact.units().get(unit);

            if (units == null || units.isEmpty()) {
                continue;
            }

            return units.stream()
                    .filter(this::isSupportedForm)
                    .filter(u -> u.val() != null)
                    .filter(u -> u.end() != null)
                    .filter(u -> u.filed() != null)
                    .map(u -> toFactPoint(tag, u))
                    .sorted(
                            Comparator.comparing(
                                    SecFactPoint::filedDate
                            )
                    )
                    .toList();
        }

        return List.of();
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
}