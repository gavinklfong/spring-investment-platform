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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactivefeign.spring.config.EnableReactiveFeignClients;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import space.gavinklfong.invest.InvestApplication;
import space.gavinklfong.invest.forex.clients.config.ForexClientConfig;
import space.gavinklfong.invest.forex.dtos.ForexRate;
import space.gavinklfong.invest.forex.dtos.ForexRateBooking;
import space.gavinklfong.invest.forex.dtos.TradeAction;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static space.gavinklfong.invest.forex.clients.TestConstants.RATE_BOOKING_REQ;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "ForexTradeProvider", port = "8082")
@SpringBootTest(classes = {InvestApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("pact")
@Slf4j
public class ForexClientPactTest {

    @Autowired
    private ForexClient forexClient;

    @Pact(consumer="InvestmentClient")
    public RequestResponsePact getForexRates(PactDslWithProvider builder) {
        return builder
                .given("Get Forex Rates")
                .uponReceiving("Latest Rates Request")
                .path("/rates/latest")
                .method(HttpMethod.GET.name())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(
                        PactDslJsonArray.arrayEachLike()
                                .stringType("baseCurrency", "GBP")
                                .stringType("counterCurrency", "USD")
                                .numberType("buyRate", 1.25)
                                .numberType("sellRate", 1.5)
                                .timestamp("timestamp", "yyyy-MM-dd'T'HH:mm:ss'Z'", Instant.now())
                                .closeObject()
                )
                .toPact();
    }

    @Pact(consumer="InvestmentClient")
    public RequestResponsePact bookRate(PactDslWithProvider builder) {
        return builder
                .given("Book Forex Rate")
                .uponReceiving("Rate Booking Request")
                .path("/rates/book")
                .method(HttpMethod.POST.name())
                .headers(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new PactDslJsonBody()
                        .stringType("baseCurrency", "GBP")
                        .stringType("counterCurrency", "USD")
                        .numberType("baseCurrencyAmount", 1500.25)
                        .stringType("tradeAction", TradeAction.BUY.name())
                        .numberType("customerId", 1L)
                )
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(
                        new PactDslJsonBody()
                                .numberType("id", 100)
                                .timestamp("timestamp", "yyyy-MM-dd'T'HH:mm:ss'Z'", Instant.now())
                                .stringType("baseCurrency", "GBP")
                                .stringType("counterCurrency", "USD")
                                .numberType("rate", 1.25)
                                .stringType("tradeAction", TradeAction.BUY.name())
                                .numberType("baseCurrencyAmount", 1500.25)
                                .stringType("bookingRef", UUID.randomUUID().toString())
                                .timestamp("expiryTime", "yyyy-MM-dd'T'HH:mm:ss'Z'", Instant.now().plus(Duration.ofMinutes(10)))
                )
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getForexRates")
    void getRatesPactTest(MockServer mockServer) throws IOException {
        Flux<ForexRate> rates = forexClient.getLatestRates();
        List<ForexRate> rateList = rates.collectList().block();
        assertTrue(rateList.size() > 0);
        ForexRate rateSample = rateList.get(0);
        assertNotNull(rateSample);
    }

    @Test
    @PactTestFor(pactMethod = "bookRate")
    void bookRatePactTest(MockServer mockServer) throws IOException {
        Mono<ForexRateBooking> bookingMono = forexClient.bookRate(RATE_BOOKING_REQ);
        ForexRateBooking booking = bookingMono.block();
        assertNotNull(booking);
    }
}
