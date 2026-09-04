package com.tony.tradinglab.discovery.domain;

/* Potential Catalyst Investigation Type
* */
public enum CatalystType {
    //왜 설비투자가 증가했는지 조사
    CAPEX_EXPANSION,
    //공장, 데이터센터, 생산라인 등 실제 생산능력을 늘리고 있는지 조사
    CAPACITY_EXPANSION,
    //회사 실적을 크게 바꿀 만한 대형 계약이나 수주가 있는지 조사
    LARGE_CONTRACT,
    //새로운 대형 고객을 확보했는지 조사
    NEW_CUSTOMER,
    //아직 매출로 잡히지 않은 수주잔고가 늘고 있는지 조사
    BACKLOG_GROWTH,
    //중요한 신규 제품 출시가 있는지 조사
    NEW_PRODUCT,
    //연구개발 단계였던 기술이 실제 판매 단계로 넘어가는지 조사
    COMMERCIALIZATION,
    //정부기관과의 계약이 있는지 조사
    GOVERNMENT_CONTRACT,
    //FDA 같은 규제 승인이나 허가가 있는지 조사
    REGULATORY_APPROVAL,
    //회사가 속한 산업 자체가 구조적으로 성장하고 있는지 조사
    INDUSTRY_TAILWIND,
    //적자축소·흑자전환의 실제 원인이 무엇인지 조사
    TURNAROUND,
    //부채가 왜 늘었는지 조사
    DEBT_PURPOSE,
    //위 분류에 딱 맞지 않는 기타 Catalyst 조사
    OTHER
}