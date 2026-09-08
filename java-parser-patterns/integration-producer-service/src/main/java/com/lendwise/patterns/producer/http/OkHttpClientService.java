package com.lendwise.patterns.producer.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client using OkHttp.
 * Parser should detect: OkHttpClient, Request.Builder(), client.newCall().execute()
 */
@Service
@Slf4j
public class OkHttpClientService {

    private OkHttpClient okHttpClient;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @PostConstruct
    public void init() {
        // Parser detects: new OkHttpClient.Builder()
        this.okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
    }

    /**
     * GET request using OkHttp.
     * Parser detects: new Request.Builder(), client.newCall(), call.execute()
     */
    public String get(String url) throws IOException {
        // Parser detects: new Request.Builder().url().build()
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")
            .build();

        // Parser detects: okHttpClient.newCall(request).execute()
        try (Response response = okHttpClient.newCall(request).execute()) {
            log.info("OkHttp GET {} returned {}", url, response.code());

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }

            ResponseBody body = response.body();
            return body != null ? body.string() : null;
        }
    }

    /**
     * POST request with JSON body.
     * Parser detects: RequestBody.create(), Request.Builder().post()
     */
    public String post(String url, String jsonBody) throws IOException {
        // Parser detects: RequestBody.create()
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            log.info("OkHttp POST {} returned {}", url, response.code());

            ResponseBody responseBody = response.body();
            return responseBody != null ? responseBody.string() : null;
        }
    }

    /**
     * PUT request.
     */
    public String put(String url, String jsonBody) throws IOException {
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
            .url(url)
            .put(body)
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            return responseBody != null ? responseBody.string() : null;
        }
    }

    /**
     * DELETE request.
     */
    public int delete(String url) throws IOException {
        Request request = new Request.Builder()
            .url(url)
            .delete()
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.code();
        }
    }

    /**
     * Asynchronous GET request.
     * Parser detects: client.newCall().enqueue()
     */
    public void getAsync(String url, Callback callback) {
        Request request = new Request.Builder()
            .url(url)
            .build();

        // Parser detects: okHttpClient.newCall(request).enqueue()
        okHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * POST with form body.
     */
    public String postForm(String url, java.util.Map<String, String> formData) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (var entry : formData.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder()
            .url(url)
            .post(formBuilder.build())
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            return body != null ? body.string() : null;
        }
    }

    /**
     * Request with custom headers.
     */
    public String getWithHeaders(String url, java.util.Map<String, String> headers) throws IOException {
        Request.Builder builder = new Request.Builder().url(url);

        for (var entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        try (Response response = okHttpClient.newCall(builder.build()).execute()) {
            ResponseBody body = response.body();
            return body != null ? body.string() : null;
        }
    }
}
