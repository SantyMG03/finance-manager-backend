package com.santy.finances.clients;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinnhubApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    private FinnhubApiClient client;

    @BeforeEach
    void setUp() {
        client = new FinnhubApiClient(restTemplate, "test-api-key");
    }

    @Test
    void fetchQuote_returnsPriceOnSuccess() {
        FinnhubQuoteDTO quote = new FinnhubQuoteDTO();
        quote.setCurrentPrice(new BigDecimal("150.50"));
        when(restTemplate.getForObject(anyString(), eq(FinnhubQuoteDTO.class))).thenReturn(quote);

        BigDecimal result = client.fetchQuote("AAPL");

        assertThat(result).isEqualByComparingTo(new BigDecimal("150.50"));
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate).getForObject(urlCaptor.capture(), eq(FinnhubQuoteDTO.class));
        assertThat(urlCaptor.getValue()).contains("symbol=AAPL").contains("token=test-api-key");
    }

    @Test
    void fetchQuote_throwsWhenResponseIsNull() {
        when(restTemplate.getForObject(anyString(), eq(FinnhubQuoteDTO.class))).thenReturn(null);

        assertThatThrownBy(() -> client.fetchQuote("AAPL"))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    void fetchQuote_throwsWhenPriceIsNull() {
        FinnhubQuoteDTO quote = new FinnhubQuoteDTO();
        when(restTemplate.getForObject(anyString(), eq(FinnhubQuoteDTO.class))).thenReturn(quote);

        assertThatThrownBy(() -> client.fetchQuote("AAPL"))
                .isInstanceOf(RestClientException.class);
    }

    @Test
    void fetchQuote_propagatesRestTemplateErrors() {
        when(restTemplate.getForObject(anyString(), eq(FinnhubQuoteDTO.class)))
                .thenThrow(new RestClientException("connection refused"));

        assertThatThrownBy(() -> client.fetchQuote("AAPL"))
                .isInstanceOf(RestClientException.class)
                .hasMessage("connection refused");
    }

    @Test
    void fallbackPrice_returnsZero() throws Exception {
        Method fallback = FinnhubApiClient.class.getDeclaredMethod("fallbackPrice", String.class, Throwable.class);
        fallback.setAccessible(true);

        BigDecimal result = (BigDecimal) fallback.invoke(client, "AAPL", new RuntimeException("boom"));

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}