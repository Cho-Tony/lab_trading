package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.SecCompanyFactsClient;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.QuarterlyFinancials;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.dto.SecCompanyFactsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QuarterlyFinancialsAssemblerTest {

    @Autowired
    private SecCompanyFactsClient companyFactsClient;

    @Autowired
    private SecFactExtractor factExtractor;

    @Autowired
    private SecQuarterNormalizer quarterNormalizer;

    @Autowired
    private QuarterlyFinancialsAssembler financialsAssembler;

    @Test
    void assembleAaplQuarterlyFinancials() {

        // 1. SEC에서 AAPL 전체 Company Facts 조회
        SecCompanyFactsResponse response =
                companyFactsClient.getCompanyFacts("AAPL");


        // 2. Revenue 추출
        List<SecFactPoint> revenueFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "RevenueFromContractWithCustomerExcludingAssessedTax",
                                "Revenues",
                                "SalesRevenueNet"
                        ),
                        "USD"
                );


        // 3. Operating Income 추출
        List<SecFactPoint> operatingIncomeFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "OperatingIncomeLoss"
                        ),
                        "USD"
                );


        // 4. Net Income 추출
        List<SecFactPoint> netIncomeFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "NetIncomeLoss",
                                "ProfitLoss"
                        ),
                        "USD"
                );


        // 5. SEC raw fact → 분기 데이터로 정규화
        List<QuarterlyFact> revenues =
                quarterNormalizer.normalize(
                        revenueFacts
                );

        List<QuarterlyFact> operatingIncomes =
                quarterNormalizer.normalize(
                        operatingIncomeFacts
                );

        List<QuarterlyFact> netIncomes =
                quarterNormalizer.normalize(
                        netIncomeFacts
                );


        // 6. 같은 FY / Quarter끼리 합치기
        List<QuarterlyFinancials> financials =
                financialsAssembler.assemble(
                        revenues,
                        operatingIncomes,
                        netIncomes
                );


        // 7. 검증
        assertThat(financials).isNotEmpty();


        // 8. 최근 8개 분기 출력
        financials.stream()
                .skip(
                        Math.max(
                                0,
                                financials.size() - 8
                        )
                )
                .forEach(f ->
                        System.out.println(
                                "FY"
                                        + f.fiscalYear()
                                        + " "
                                        + f.fiscalQuarter()

                                        + " | Revenue="
                                        + f.revenue()

                                        + " | OperatingIncome="
                                        + f.operatingIncome()

                                        + " | NetIncome="
                                        + f.netIncome()

                                        + " | filed="
                                        + f.filedDate()
                        )
                );
    }
}