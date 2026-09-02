package com.tony.tradinglab.fundamental.service;

import com.tony.tradinglab.fundamental.client.SecCompanyFactsClient;
import com.tony.tradinglab.fundamental.domain.QuarterlyFact;
import com.tony.tradinglab.fundamental.domain.SecFactPoint;
import com.tony.tradinglab.fundamental.dto.SecCompanyFactsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecCashFlowQuarterNormalizerTest {

    @Autowired
    private SecCompanyFactsClient companyFactsClient;

    @Autowired
    private SecFactExtractor factExtractor;

    @Autowired
    private SecCashFlowQuarterNormalizer cashFlowQuarterNormalizer;

    @Test
    void normalizeAaplCashFlow() {

        // 1. SEC에서 AAPL 데이터 가져오기
        SecCompanyFactsResponse response =
                companyFactsClient.getCompanyFacts("AAPL");


        // 2. Operating Cash Flow 추출
        List<SecFactPoint> operatingCashFlowFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "NetCashProvidedByUsedInOperatingActivities"
                        ),
                        "USD"
                );


        // 3. Capital Expenditure 추출
        List<SecFactPoint> capexFacts =
                factExtractor.extract(
                        response,
                        List.of(
                                "PaymentsToAcquirePropertyPlantAndEquipment",
                                "PaymentsForAdditionsToPropertyPlantAndEquipment"
                        ),
                        "USD"
                );


        // 4. 누적 현금흐름 → 실제 분기값 변환
        List<QuarterlyFact> operatingCashFlows =
                cashFlowQuarterNormalizer.normalize(
                        operatingCashFlowFacts
                );

        List<QuarterlyFact> capitalExpenditures =
                cashFlowQuarterNormalizer.normalize(
                        capexFacts
                );


        assertThat(operatingCashFlows)
                .isNotEmpty();

        assertThat(capitalExpenditures)
                .isNotEmpty();


        System.out.println();
        System.out.println(
                "========== Operating Cash Flow =========="
        );

        printRecent(
                operatingCashFlows
        );


        System.out.println();
        System.out.println(
                "========== Capital Expenditure =========="
        );

        printRecent(
                capitalExpenditures
        );
    }

    private void printRecent(
            List<QuarterlyFact> facts
    ) {

        facts.stream()
                .skip(
                        Math.max(
                                0,
                                facts.size() - 12
                        )
                )
                .forEach(fact ->
                        System.out.println(
                                "FY"
                                        + fact.fiscalYear()
                                        + " "
                                        + fact.fiscalQuarter()

                                        + " | value="
                                        + fact.value()

                                        + " | filed="
                                        + fact.filedDate()

                                        + " | derived="
                                        + fact.derived()
                        )
                );
    }
}