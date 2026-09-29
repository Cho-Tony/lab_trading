package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.universe.UniverseTarget;
import com.tony.tradinglab.universe.ValuationUniverse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DefaultQuantUniverseProvider
        implements QuantUniverseProvider {

    @Override
    public List<UniverseTarget> getUniverse(
            LocalDate observationDate
    ) {

        if (observationDate == null) {
            return List.of();
        }


        /*
         * 현재는 40종목 Valuation Universe를
         * Quant Discovery 검증용 Universe로 재사용한다.
         *
         * 이후 실제 Discovery 단계에서는
         * 미국 전체 종목을 PIT 기준으로 반환하는
         * Universe Provider로 교체한다.
         */
        return ValuationUniverse.targets();
    }
}