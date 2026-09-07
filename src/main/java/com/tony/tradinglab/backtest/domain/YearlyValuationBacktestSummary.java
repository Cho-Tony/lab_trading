package com.tony.tradinglab.backtest.domain;

public record YearlyValuationBacktestSummary(

        int year,

        ValuationMethodBacktestSummary summary

) {
}