package space.gavinklfong.invest.forex.clients;

import space.gavinklfong.invest.forex.dtos.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

public interface TestConstants {

    ForexRate GBP_USD_RATE =  ForexRate.builder().baseCurrency("GBP")
            .counterCurrency("USD")
            .buyRate(1.1)
            .sellRate(1.2)
            .timestamp(Instant.now())
            .build();

    ForexRate GBP_EUR_RATE =  ForexRate.builder().baseCurrency("GBP")
            .counterCurrency("EUR")
            .buyRate(0.95)
            .sellRate(1.19)
            .timestamp(Instant.now())
            .build();

    List<ForexRate> FOREX_RATES = asList(GBP_USD_RATE, GBP_EUR_RATE);

    ForexRateBookingReq RATE_BOOKING_REQ = ForexRateBookingReq.builder()
            .baseCurrency("GBP")
            .counterCurrency("USD")
            .customerId(1L)
            .tradeAction(TradeAction.BUY)
            .baseCurrencyAmount(BigDecimal.valueOf(1500))
            .build();

    ForexRateBooking RATE_BOOKING = ForexRateBooking.builder()
            .bookingRef(UUID.randomUUID().toString())
            .baseCurrency(RATE_BOOKING_REQ.getBaseCurrency())
            .counterCurrency(RATE_BOOKING_REQ.getCounterCurrency())
            .customerId(RATE_BOOKING_REQ.getCustomerId())
            .tradeAction(RATE_BOOKING_REQ.getTradeAction())
            .timestamp(Instant.now())
            .id(1l)
            .expiryTime(Instant.now().plus(Duration.ofMinutes(15)))
            .rate(1.25)
            .build();

    ForexTradeDealReq TRADE_DEAL_REQ = ForexTradeDealReq.builder()
            .tradeAction(TradeAction.BUY)
            .baseCurrency("GBP")
            .counterCurrency("USD")
            .customerId(1L)
            .rate(1.25)
            .rateBookingRef(UUID.randomUUID().toString())
            .baseCurrencyAmount(BigDecimal.valueOf(1500))
            .build();
}
