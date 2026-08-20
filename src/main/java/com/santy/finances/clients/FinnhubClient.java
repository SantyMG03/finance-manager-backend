package com.santy.finances.clients;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FinnhubClient {

    private final FinnhubApiClient finnhubApiClient;

    public FinnhubClient(FinnhubApiClient finnhubApiClient) {
        this.finnhubApiClient = finnhubApiClient;
    }

    /**
     * Gets the real-time price of a ticker. Cached externally, resilient internally:
     * a cache hit never consumes rate-limit permits or circuit breaker state, and on
     * failure the resilience layer falls back to a zero price.
     *
     * @param ticker The stock symbol (e.g., AAPL).
     * @return The current market price, or BigDecimal.ZERO if the external API fails.
     */
    @Cacheable(value = "quotes", key = "#ticker")
    public BigDecimal getCurrentPrice(String ticker) {
        return finnhubApiClient.fetchQuote(ticker);
    }
}