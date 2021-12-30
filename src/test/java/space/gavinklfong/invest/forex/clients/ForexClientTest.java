package space.gavinklfong.invest.forex.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import space.gavinklfong.invest.InvestApplication;
import space.gavinklfong.invest.forex.dtos.ForexRate;
import space.gavinklfong.invest.forex.dtos.ForexRateBooking;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.Arrays.asList;
import static space.gavinklfong.invest.forex.clients.TestConstants.*;

@SpringBootTest(classes = {InvestApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Slf4j
public class ForexClientTest {

    public static WireMockServer wireMockRule = new WireMockServer(options().dynamicPort());

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("app.forex.service.url", wireMockRule::baseUrl);
    }

    @BeforeAll
    public static void beforeAll() {
        wireMockRule.start();
    }

    @AfterAll
    public static void afterAll() {
        wireMockRule.stop();
    }

    @AfterEach
    public void afterEach() {
        wireMockRule.resetAll();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ForexClient forexClient;

    @Test
    void givenLatestRates_whenGetLatestRates_thenReturnResult() throws JsonProcessingException {
        // Given
        wireMockRule.stubFor(get(urlEqualTo("/rates/latest"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(FOREX_RATES))
                )
        );

        // When
        Flux<ForexRate> rates = forexClient.getLatestRates();

        // Then
        StepVerifier.create(rates)
                .expectNext(FOREX_RATES.get(0))
                .expectNext(FOREX_RATES.get(1))
                .expectComplete();
    }

    @Test
    void givenRateAvailableForBooking_whenSubmitBookingReq_thenReturnBooking() throws JsonProcessingException {
        // Given
        wireMockRule.stubFor(post(urlEqualTo("/rates/book"))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(RATE_BOOKING_REQ)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(RATE_BOOKING))
                )
        );

        // When
        Mono<ForexRateBooking> rateBooking = forexClient.bookRate(RATE_BOOKING_REQ);

        // Then
        StepVerifier.create(rateBooking)
                .expectNext(RATE_BOOKING)
                .verifyComplete();
    }

    @Test
    void givenAllAttemptTimeout_whenSubmitBookingReq_thenReturnError() throws JsonProcessingException {
        // Given
        wireMockRule.stubFor(post(urlEqualTo("/rates/book"))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(RATE_BOOKING_REQ)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(RATE_BOOKING))
                        .withFixedDelay(5000)
                ));

        // When
        Mono<ForexRateBooking> rateBooking = forexClient.bookRate(RATE_BOOKING_REQ);

        // Then
        StepVerifier.create(rateBooking)
                .expectError();
    }

    @Test
    void given1stAnd2ndAttempTimeoutAnd3rdAttemptSuccess_whenSubmitBookingReq_thenReturnRateBooking() throws JsonProcessingException {
        // Given
        setupStubForRateBooking("retry", STARTED, "1st request received", 5000);
        setupStubForRateBooking("retry", "1st request received", "2nd request received", 5000);
        setupStubForRateBooking("retry", "2nd request received", "3rd request received", 1000);

        // When
        Mono<ForexRateBooking> rateBooking = forexClient.bookRate(RATE_BOOKING_REQ);

        // Then
        StepVerifier.create(rateBooking)
                .expectNext(RATE_BOOKING)
                .verifyComplete();
    }

    private void setupStubForRateBooking(String scenario, String currentState, String nextState, Integer fixedDelay) throws JsonProcessingException {
        wireMockRule.stubFor(post(urlEqualTo("/rates/book"))
                .inScenario(scenario)
                .whenScenarioStateIs(currentState)
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(RATE_BOOKING_REQ)))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(RATE_BOOKING))
                        .withFixedDelay(fixedDelay)
                )
                .willSetStateTo(nextState));
    }

}
