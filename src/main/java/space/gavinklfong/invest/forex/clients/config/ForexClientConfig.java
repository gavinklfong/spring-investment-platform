package space.gavinklfong.invest.forex.clients.config;

import feign.Feign;
import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import reactivefeign.client.ReactiveHttpRequestInterceptor;
import reactivefeign.client.ReactiveHttpRequestInterceptors;
import reactivefeign.client.ReactiveHttpResponse;
import reactivefeign.client.log.DefaultReactiveLogger;
import reactivefeign.client.log.ReactiveLoggerListener;
import reactivefeign.client.statushandler.ReactiveStatusHandler;
import reactivefeign.client.statushandler.ReactiveStatusHandlers;
import reactivefeign.retry.BasicReactiveRetryPolicy;
import reactivefeign.retry.ReactiveRetryPolicy;
import reactivefeign.retry.SimpleReactiveRetryPolicy;
import reactivefeign.spring.config.ReactiveRetryPolicies;
import reactivefeign.utils.HttpStatus;
import space.gavinklfong.invest.forex.clients.ForexClient;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.function.BiFunction;

@Configuration
public class ForexClientConfig {

    @Value("${app.forex.service.api-key}")
    private String apiKey;

    private static final String API_KEY_HEADER = "API_KEY";

    @Bean
    public ReactiveHttpRequestInterceptor apiKeyIntercepter() {
        return ReactiveHttpRequestInterceptors.addHeader(API_KEY_HEADER, apiKey);
    }

    @Bean
    public ReactiveLoggerListener loggerListener() {
        return new DefaultReactiveLogger(Clock.systemUTC(), LoggerFactory.getLogger(ForexClient.class.getName()));
    }

    @Bean
    public ReactiveRetryPolicy reactiveRetryPolicy() {
        return BasicReactiveRetryPolicy.retryWithBackoff(3, 2000);
    }

    @Bean
    public ReactiveStatusHandler reactiveStatusHandler() {
        return ReactiveStatusHandlers.throwOnStatus(
                (status) -> (status == 500),
                errorFunction());
    }

    private BiFunction<String, ReactiveHttpResponse, Throwable> errorFunction() {
        return (methodKey, response) -> {
            return new RetryableException(response.status(), "", null, Date.from(Instant.EPOCH), null);
        };
    }
}
