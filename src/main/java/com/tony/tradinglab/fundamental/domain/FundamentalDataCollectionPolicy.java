package com.tony.tradinglab.fundamental.domain;

/** Explicit collection policy; it is never inferred from the observation date. */
public enum FundamentalDataCollectionPolicy {
    AUTO_SYNC_IF_EMPTY,
    READ_ONLY
}
