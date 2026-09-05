package com.tony.tradinglab.fundamental.domain;

import java.util.List;

public record PeerUniverse(

        ValuationPeerSnapshot target,

        List<ValuationPeerSnapshot> peers,

        PeerSelectionLevel selectionLevel

) {
}