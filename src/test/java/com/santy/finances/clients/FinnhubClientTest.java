package com.santy.finances.clients;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinnhubClientTest {

    @Mock
    private FinnhubApiClient finnhubApiClient;

    @InjectMocks
    private FinnhubClient finnhubClient;

    @Test
    void getCurrentPrice_delegatesToApiClient() {
        when(finnhubApiClient.fetchQuote("AAPL")).thenReturn(new BigDecimal("150.50"));

        BigDecimal result = finnhubClient.getCurrentPrice("AAPL");

        assertThat(result).isEqualByComparingTo(new BigDecimal("150.50"));
        verify(finnhubApiClient).fetchQuote("AAPL");
    }
}