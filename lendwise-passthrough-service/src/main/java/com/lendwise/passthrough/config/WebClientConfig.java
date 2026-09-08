package com.lendwise.passthrough.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient configuration for Gold Field service communication.
 * Configures connection pooling, timeouts, and retry policies.
 */
@Configuration
public class WebClientConfig {

    @Value("${webclient.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${webclient.read-timeout:30000}")
    private int readTimeout;

    @Value("${webclient.write-timeout:30000}")
    private int writeTimeout;

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json");
    }

    @Bean
    public WebClient borrowerServiceClient(WebClient.Builder builder,
            @Value("${services.borrower-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient kycServiceClient(WebClient.Builder builder,
            @Value("${services.kyc-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient documentServiceClient(WebClient.Builder builder,
            @Value("${services.document-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient creditServiceClient(WebClient.Builder builder,
            @Value("${services.credit-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient underwritingServiceClient(WebClient.Builder builder,
            @Value("${services.underwriting-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient complianceServiceClient(WebClient.Builder builder,
            @Value("${services.compliance-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient pricingServiceClient(WebClient.Builder builder,
            @Value("${services.pricing-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient ratelockServiceClient(WebClient.Builder builder,
            @Value("${services.ratelock-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient amortizationServiceClient(WebClient.Builder builder,
            @Value("${services.amortization-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient closingServiceClient(WebClient.Builder builder,
            @Value("${services.closing-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient esignServiceClient(WebClient.Builder builder,
            @Value("${services.esign-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient fundingServiceClient(WebClient.Builder builder,
            @Value("${services.funding-service.url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}
