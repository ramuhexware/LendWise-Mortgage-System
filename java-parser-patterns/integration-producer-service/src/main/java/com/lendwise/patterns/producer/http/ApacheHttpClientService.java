package com.lendwise.patterns.producer.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;

/**
 * HTTP client using Apache HttpClient 5.
 * Parser should detect: HttpClients.createDefault(), CloseableHttpClient,
 * HttpGet, HttpPost, httpClient.execute()
 */
@Service
@Slf4j
public class ApacheHttpClientService {

    private CloseableHttpClient httpClient;

    @PostConstruct
    public void init() {
        // Parser detects: HttpClients.custom() or HttpClients.createDefault()
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(5))
            .setResponseTimeout(Timeout.ofSeconds(30))
            .build();

        this.httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();
    }

    @PreDestroy
    public void destroy() throws IOException {
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * GET request using Apache HttpClient.
     * Parser detects: new HttpGet(), httpClient.execute()
     */
    public String get(String url) throws IOException, ParseException {
        // Parser detects: new HttpGet(url)
        HttpGet httpGet = new HttpGet(url);
        httpGet.addHeader("Accept", "application/json");

        // Parser detects: httpClient.execute()
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            int statusCode = response.getCode();
            log.info("GET {} returned {}", url, statusCode);

            if (statusCode >= 400) {
                throw new IOException("HTTP error: " + statusCode);
            }

            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * POST request with JSON body.
     * Parser detects: new HttpPost(), setEntity(), httpClient.execute()
     */
    public String post(String url, String jsonBody) throws IOException, ParseException {
        // Parser detects: new HttpPost(url)
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Accept", "application/json");

        // Set request body
        StringEntity entity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getCode();
            log.info("POST {} returned {}", url, statusCode);

            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * PUT request.
     * Parser detects: new HttpPut()
     */
    public String put(String url, String jsonBody) throws IOException, ParseException {
        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", "application/json");
        httpPut.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * DELETE request.
     * Parser detects: new HttpDelete()
     */
    public int delete(String url) throws IOException {
        HttpDelete httpDelete = new HttpDelete(url);

        try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            return response.getCode();
        }
    }

    /**
     * POST with form data.
     */
    public String postForm(String url, java.util.Map<String, String> formData) throws IOException, ParseException {
        HttpPost httpPost = new HttpPost(url);

        StringBuilder formBody = new StringBuilder();
        for (var entry : formData.entrySet()) {
            if (formBody.length() > 0) formBody.append("&");
            formBody.append(entry.getKey()).append("=").append(entry.getValue());
        }

        httpPost.setEntity(new StringEntity(formBody.toString(), ContentType.APPLICATION_FORM_URLENCODED));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * GET with custom headers.
     */
    public String getWithHeaders(String url, java.util.Map<String, String> headers) throws IOException, ParseException {
        HttpGet httpGet = new HttpGet(url);

        for (var entry : headers.entrySet()) {
            httpGet.addHeader(entry.getKey(), entry.getValue());
        }

        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            return EntityUtils.toString(response.getEntity());
        }
    }
}
