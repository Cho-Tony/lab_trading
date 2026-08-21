package com.tony.tradinglab.fundamental.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecTickerResolverTest {

    @Autowired
    private SecTickerResolver secTickerResolver;

    @Test
    void resolveAaplCik() {

        String cik = secTickerResolver.resolveCik("AAPL");

        System.out.println("AAPL CIK = " + cik);

        assertThat(cik).isEqualTo("0000320193");
    }
}