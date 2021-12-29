package space.gavinklfong.invest.forex.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
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
import reactor.test.StepVerifier;
import space.gavinklfong.invest.InvestApplication;
import space.gavinklfong.invest.forex.dtos.ForexRate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static java.util.Arrays.asList;

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

        final List<ForexRate> FOREX_RATES = asList(
                ForexRate.builder().baseCurrency("GBP")
                        .counterCurrency("USD")
                        .buyRate(1.1)
                        .sellRate(1.2)
                        .timestamp(LocalDateTime.now())
                        .build());

        // Given
        wireMockRule.stubFor(get(urlEqualTo("/rates/latest"))
                .willReturn(aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(FOREX_RATES)))
        );

        // When
        Flux<ForexRate> rates = forexClient.getLatestRates();

        // Then
        StepVerifier.create(rates)
                .expectNext(FOREX_RATES.get(0))
                .expectComplete();
    }

}
