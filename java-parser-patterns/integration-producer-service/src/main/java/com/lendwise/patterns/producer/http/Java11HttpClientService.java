package com.lendwise.patterns.producer.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client using Java 11+ HttpClient API.
 * Parser should detect: HttpClient.newHttpClient(), HttpRequest.newBuilder(),
 * client.send(), client.sendAsync()
 */
@Service
@Slf4j
public class Java11HttpClientService {

    private final HttpClient httpClient;

    public Java11HttpClientService() {
        // Parser detects: HttpClient.newBuilder()
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    /**
     * Synchronous GET request.
     * Parser detects: HttpRequest.newBuilder(), client.send()
     */
    public String getSync(String url) throws Exception {
        // Parser detects: HttpRequest.newBuilder().uri().GET().build()
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(30))
            .build();

        // Parser detects: httpClient.send()
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("GET {} returned status {}", url, response.statusCode());
        return response.body();
    }

    /**
     * Synchronous POST request with JSON body.
     * Parser detects: HttpRequest.newBuilder().POST()
     */
    public String postSync(String url, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("HTTP error: " + response.statusCode());
        }

        return response.body();
    }

    /**
     * Asynchronous GET request.
     * Parser detects: httpClient.sendAsync()
     */
    public CompletableFuture<String> getAsync(String url) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();

        // Parser detects: httpClient.sendAsync()
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .exceptionally(ex -> {
                log.error("Async GET failed: {}", url, ex);
                return null;
            });
    }

    /**
     * Asynchronous POST request.
     */
    public CompletableFuture<HttpResponse<String>> postAsync(String url, String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * PUT request.
     */
    public String putSync(String url, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    /**
     * DELETE request.
     */
    public int deleteSync(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .DELETE()
            .build();

        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        return response.statusCode();
    }

    /**
     * Request with custom method.
     */
    public String customMethod(String url, String method, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .method(method, body != null
                ? HttpRequest.BodyPublishers.ofString(body)
                : HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
