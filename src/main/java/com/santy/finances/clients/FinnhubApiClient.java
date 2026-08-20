package com.santy.finances.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class FinnhubApiClient {

    private static final Logger log = LoggerFactory.getLogger(FinnhubApiClient.class);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl = "https://finnhub.io/api/v1/quote";

    public FinnhubApiClient(RestTemplate restTemplate, @Value("${finnhub.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    /**
     * Raw call to the Finnhub API. Exceptions are intentionally NOT swallowed so
     * the resilience4j aspects can record them as failures.
     *
     * @param ticker The stock symbol (e.g., AAPL).
     * @return The current market price.
     */
    @CircuitBreaker(name = "finnhub")
    @RateLimiter(name = "finnhub")
    @Retry(name = "finnhub", fallbackMethod = "fallbackPrice")
    public BigDecimal fetchQuote(String ticker) {
        String url = baseUrl + "?symbol=" + ticker + "&token=" + apiKey;

        FinnhubQuoteDTO response = restTemplate.getForObject(url, FinnhubQuoteDTO.class);

        if (response == null || response.getCurrentPrice() == null) {
            throw new RestClientException("Empty or invalid quote response for ticker " + ticker);
        }

        return response.getCurrentPrice();
    }

    /**
     * Fallback used by resilience4j when the call fails after retries or the
     * circuit breaker / rate limiter rejects it. Preserves the previous graceful
     * degradation behavior: TransactionService treats a zero price as "use the
     * weighted average price".
     *
     * @param ticker The stock symbol that failed.
     * @param throwable The cause of the failure.
     * @return BigDecimal.ZERO so downstream calculations are not disrupted.
     */
    private BigDecimal fallbackPrice(String ticker, Throwable throwable) {
        log.warn("Finnhub quote failed for ticker {}: {}", ticker, throwable.getMessage());
        return BigDecimal.ZERO;
    }
}