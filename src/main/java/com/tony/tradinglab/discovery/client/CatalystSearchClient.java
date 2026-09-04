package com.tony.tradinglab.discovery.client;

import com.tony.tradinglab.discovery.domain.CatalystReviewRequest;
import com.tony.tradinglab.discovery.domain.CatalystReviewResult;

public interface CatalystSearchClient {

    CatalystReviewResult review(
            CatalystReviewRequest request
    );
}