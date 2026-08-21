package com.tony.tradinglab.discovery.service;

import com.tony.tradinglab.discovery.domain.TechnicalCandidate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TechnicalScoreCalculator {

    public int calculate(TechnicalCandidate candidate) {

        int score = 0;

        score += calculateTrendScore(candidate);
        score += calculateMomentumScore(candidate);
        score += calculateRelativeStrengthScore(candidate);
        score += calculateVolumeScore(candidate);
        score += calculateHighPositionScore(candidate);

        return Math.min(score, 100);
    }

    private int calculateTrendScore(TechnicalCandidate c) {

        int score = 0;

        if (c.close().compareTo(c.sma20()) > 0) {
            score += 5;
        }

        if (c.sma20().compareTo(c.sma50()) > 0) {
            score += 10;
        }

        if (c.sma50().compareTo(c.sma200()) > 0) {
            score += 15;
        }

        if (c.sma200Slope20DayPct().compareTo(BigDecimal.ZERO) > 0) {
            score += 5;
        }

        return score;
    }

    private int calculateMomentumScore(TechnicalCandidate c) {

        int score = 0;

        double month1 = c.return1MonthPct().doubleValue();
        double month3 = c.return3MonthPct().doubleValue();
        double month6 = c.return6MonthPct().doubleValue();

        // 최근 너무 급등한 종목은 오히려 점수를 조금 낮춤
        if (month1 > 0 && month1 <= 15) {
            score += 5;
        } else if (month1 > 15) {
            score += 2;
        }

        if (month3 >= 10 && month3 <= 30) {
            score += 10;
        } else if (month3 > 0) {
            score += 5;
        }

        if (month6 >= 20 && month6 <= 60) {
            score += 10;
        } else if (month6 > 0) {
            score += 5;
        }

        return score;
    }

    private int calculateRelativeStrengthScore(TechnicalCandidate c) {

        // RS 100점 → 20점
        // RS 80점 → 16점
        // RS 50점 → 10점

        return (int) Math.round(
                c.relativeStrengthScore() * 0.20
        );
    }

    private int calculateVolumeScore(TechnicalCandidate c) {

        double ratio = c.volumeTrendRatio().doubleValue();

        if (ratio >= 1.5) {
            return 10;
        }

        if (ratio >= 1.2) {
            return 7;
        }

        if (ratio >= 1.0) {
            return 4;
        }

        return 0;
    }

    private int calculateHighPositionScore(TechnicalCandidate c) {

        double distance =
                c.distanceFrom52WeekHighPct().doubleValue();

        // 신고가 바로 아래에서 강하게 버티는 영역
        if (distance >= -20 && distance <= -5) {
            return 10;
        }

        // 신고가에 너무 붙어있는 경우
        if (distance > -5) {
            return 7;
        }

        if (distance >= -35) {
            return 5;
        }

        return 0;
    }
}