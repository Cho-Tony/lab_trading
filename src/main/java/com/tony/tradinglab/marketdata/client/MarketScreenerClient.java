package com.tony.tradinglab.marketdata.client;

import com.tony.tradinglab.marketdata.dto.MarketCandidate;

import java.util.List;

public interface MarketScreenerClient {

    List<MarketCandidate> findCandidates();
}