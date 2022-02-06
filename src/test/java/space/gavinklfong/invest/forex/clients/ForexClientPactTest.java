package space.gavinklfong.invest.forex.clients;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import space.gavinklfong.invest.InvestApplication;
import space.gavinklfong.invest.forex.dtos.ForexRate;
import space.gavinklfong.invest.forex.dtos.ForexRateBooking;
import space.gavinklfong.invest.forex.dtos.ForexTradeDeal;
import space.gavinklfong.invest.forex.dtos.TradeAction;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static space.gavinklfong.invest.forex.clients.ForexClientPactTest.TestConstants.*;
import static space.gavinklfong.invest.forex.clients.TestConstants.RATE_BOOKING_REQ;
import static space.gavinklfong.invest.forex.clients.TestConstants.TRADE_DEAL_REQ;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "ForexTradeProvider", port = "8082", pactVersion = PactSpecVersion.V3)
@SpringBootTest(classes = {InvestApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("pact")
@Slf4j
public class ForexClientPactTest {

    @Autowired
    private ForexClient forexClient;

    @Pact(consumer = "InvestmentClient")
    public RequestResponsePact getForexRatesPact(PactDslWithProvider builder) {
        return builder
                .given("Get Forex Rates")
                .uponReceiving("Latest Rates Request")
                .path("/rates/latest")
                .method(HttpMethod.GET.name())
                .matchHeader("X-API-KEY", API_KEY_REGEX)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(PactDslJsonArray.arrayEachLike()
                        .stringMatcher("baseCurrency", CURRENCY_REGEX, "GBP")
                        .stringMatcher("counterCurrency", CURRENCY_REGEX, "USD")
                        .numberType("buyRate", 1.25)
                        .numberType("sellRate", 1.5)
                        .datetime("timestamp", TIMESTAMP_FORMAT, Instant.now())
                        .closeObject()
                )
                .toPact();
    }

    @Pact(consumer = "InvestmentClient")
    public RequestResponsePact getForexRatePact(PactDslWithProvider builder) {
        return builder
                .given("Get Forex Rate")
                .uponReceiving("Latest Rate Request")
                .matchPath(format("/rates/latest/%s/%s", CURRENCY_REGEX, CURRENCY_REGEX))
                .method(HttpMethod.GET.name())
                .matchHeader("X-API-KEY", API_KEY_REGEX)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(new PactDslJsonBody()
                        .stringMatcher("baseCurrency", CURRENCY_REGEX, "GBP")
                        .stringMatcher("counterCurrency", CURRENCY_REGEX, "USD")
                        .numberType("buyRate", 1.25)
                        .numberType("sellRate", 1.5)
                        .datetime("timestamp", TIMESTAMP_FORMAT, Instant.now())
                )
                .toPact();
    }

    @Pact(consumer = "InvestmentClient")
    public RequestResponsePact bookRatePact(PactDslWithProvider builder) {
        return builder
                .given("Book Forex Rate")
                .uponReceiving("Rate Booking Request")
                .path("/rates/book")
                .method(HttpMethod.POST.name())
                .matchHeader("X-API-KEY", API_KEY_REGEX)
                .headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new PactDslJsonBody()
                        .stringMatcher("baseCurrency", CURRENCY_REGEX, "GBP")
                        .stringMatcher("counterCurrency", CURRENCY_REGEX, "USD")
                        .numberType("baseCurrencyAmount", 1500.25)
                        .stringType("tradeAction", TradeAction.BUY.name())
                        .numberType("customerId", 1L)
                )
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(new PactDslJsonBody()
                        .numberType("id", 100)
                        .datetime("timestamp", TIMESTAMP_FORMAT, Instant.now())
                        .stringMatcher("baseCurrency", CURRENCY_REGEX, "GBP")
                        .stringMatcher("counterCurrency", CURRENCY_REGEX, "USD")
                        .numberType("rate", 1.25)
                        .stringType("tradeAction", TradeAction.BUY.name())
                        .numberType("baseCurrencyAmount", 1500.25)
                        .uuid("bookingRef", UUID.randomUUID())
                        .datetime("timestamp", TIMESTAMP_FORMAT, Instant.now().plus(Duration.ofMinutes(10)))
                        .numberType("customerId", 1L)
                )
                .toPact();
    }

    @Pact(consumer = "InvestmentClient")
    public RequestResponsePact submitDealPact(PactDslWithProvider builder) {
        return builder
                .given("Submit Forex Trade Deal")
                .uponReceiving("Trade Deal Request")
                .path("/deals")
                .method(HttpMethod.POST.name())
                .matchHeader("X-API-KEY", API_KEY_REGEX)
                .headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new PactDslJsonBody()
                        .stringMatcher("baseCurrency", CURRENCY_REGEX, "GBP")
                        .stringMatcher("counterCurrency", CURRENCY_REGEX, "USD")
                        .numberType("baseCurrencyAmount", 1500.25)
                        .numberType("rate", 1.25)
                        .stringType("tradeAction", TradeAction.BUY.name())
                        .numberType("customerId", 1L)
                        .uuid("rateBookingRef", UUID.randomUUID())
                )
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(new PactDslJsonBody()
                        .numberType("id", 100)
                        .datetime("timestamp", TIMESTAMP_FORMAT, Instant.now())
                        .stringMatcher("baseCurrency", CURRENCY_REGEX, "GBP")
                        .stringMatcher("counterCurrency", CURRENCY_REGEX, "USD")
                        .numberType("rate", 1.25)
                        .stringType("tradeAction", TradeAction.BUY.name())
                        .numberType("baseCurrencyAmount", 1500.25)
                        .numberType("customerId", 1L)
                        .uuid("dealRef", UUID.randomUUID())
                )
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getForexRatesPact")
    void getRatesPactTest(MockServer mockServer) throws IOException {
        Flux<ForexRate> rateFlux = forexClient.getLatestRates();
        List<ForexRate> rates = rateFlux.collectList().block();
        assertTrue(rates.size() > 0);
        ForexRate rateSample = rates.get(0);
        assertNotNull(rateSample);
    }

    @Test
    @PactTestFor(pactMethod = "getForexRatePact")
    void getRatePactTest(MockServer mockServer) throws IOException {
        Mono<ForexRate> rateMono = forexClient.getLatestRate("GBP", "USD");
        ForexRate rate = rateMono.block();
        assertNotNull(rate);
    }

    @Test
    @PactTestFor(pactMethod = "bookRatePact")
    void bookRatePactTest(MockServer mockServer) {
        Mono<ForexRateBooking> bookingMono = forexClient.bookRate(RATE_BOOKING_REQ);
        ForexRateBooking booking = bookingMono.block();
        assertNotNull(booking);
    }

    @Test
    @PactTestFor(pactMethod = "submitDealPact")
    void submitDealPactTest(MockServer mockServer) {
        Mono<ForexTradeDeal> dealMono = forexClient.submitDeal(TRADE_DEAL_REQ);
        ForexTradeDeal deal = dealMono.block();
        assertNotNull(deal);
    }

    interface TestConstants {
        String CURRENCY_REGEX = "([A-Z]){3}";
        String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
        String API_KEY_REGEX = "[A-Za-z0-9-]+";
    }
}
