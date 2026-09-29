package com.tony.tradinglab.discovery.quant;

import com.tony.tradinglab.universe.UniverseTarget;

import java.time.LocalDate;
import java.util.List;

public interface QuantUniverseProvider {

    List<UniverseTarget> getUniverse(
            LocalDate observationDate
    );
}