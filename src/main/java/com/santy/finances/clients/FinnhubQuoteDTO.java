package com.santy.finances.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FinnhubQuoteDTO implements Serializable {

    // c represents current price in Finnhub
    @JsonProperty("c")
    private BigDecimal currentPrice;

    // Other values will be ignored
}
