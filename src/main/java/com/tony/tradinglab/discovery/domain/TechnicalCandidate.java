package com.tony.tradinglab.discovery.domain;

import java.math.BigDecimal;

public record TechnicalCandidate(

        Long stockId,
        String symbol,

        BigDecimal close,

        // 이동평균
        BigDecimal sma20,
        BigDecimal sma50,
        BigDecimal sma200,

        // 200일선 자체가 상승하고 있는지
        BigDecimal sma200Slope20DayPct,

        // 52주 최고가 대비 현재 위치
        // 예: -12.5 = 고점 대비 12.5% 아래
        BigDecimal distanceFrom52WeekHighPct,

        // 모멘텀
        BigDecimal return1MonthPct,
        BigDecimal return3MonthPct,
        BigDecimal return6MonthPct,

        // 최근 20일 평균 거래량 / 이전 평균 거래량
        // 예: 1.4 = 최근 거래량이 40% 증가
        BigDecimal volumeTrendRatio,

        // 전체 시장 내 상대강도 순위 0~100
        int relativeStrengthScore,

        // 최종 기술점수 0~100
        int technicalScore

) {
}