package space.gavinklfong.invest.forex.clients;

import space.gavinklfong.invest.forex.dtos.ForexRate;
import space.gavinklfong.invest.forex.dtos.ForexRateBooking;
import space.gavinklfong.invest.forex.dtos.ForexRateBookingReq;
import space.gavinklfong.invest.forex.dtos.TradeAction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

public interface TestConstants {

    ForexRate GBP_USD_RATE =  ForexRate.builder().baseCurrency("GBP")
            .counterCurrency("USD")
            .buyRate(1.1)
            .sellRate(1.2)
            .timestamp(LocalDateTime.now())
            .build();

    ForexRate GBP_EUR_RATE =  ForexRate.builder().baseCurrency("GBP")
            .counterCurrency("EUR")
            .buyRate(0.95)
            .sellRate(1.19)
            .timestamp(LocalDateTime.now())
            .build();

    List<ForexRate> FOREX_RATES = asList(GBP_USD_RATE, GBP_EUR_RATE);

    ForexRateBookingReq RATE_BOOKING_REQ = ForexRateBookingReq.builder()
            .baseCurrency("GBP")
            .counterCurrency("USD")
            .customerId(1L)
            .tradeAction(TradeAction.BUY)
            .build();

    ForexRateBooking RATE_BOOKING = ForexRateBooking.builder()
            .bookingRef(UUID.randomUUID().toString())
            .baseCurrency(RATE_BOOKING_REQ.getBaseCurrency())
            .counterCurrency(RATE_BOOKING_REQ.getCounterCurrency())
            .customerId(RATE_BOOKING_REQ.getCustomerId())
            .tradeAction(RATE_BOOKING_REQ.getTradeAction())
            .timestamp(LocalDateTime.now())
            .id(1l)
            .expiryTime(LocalDateTime.now().plusMinutes(15))
            .rate(1.25)
            .build();
}
