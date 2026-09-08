package com.lendwise.patterns.producer.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HTTP client using legacy HttpURLConnection.
 * Parser should detect: URL.openConnection(), HttpURLConnection,
 * setRequestMethod(), getInputStream(), getResponseCode()
 */
@Service
@Slf4j
public class UrlConnectionService {

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;

    /**
     * GET request using HttpURLConnection.
     * Parser detects: URL.openConnection(), setRequestMethod(), getInputStream()
     */
    public String get(String urlString) throws IOException {
        // Parser detects: new URL(urlString)
        URL url = new URL(urlString);

        // Parser detects: url.openConnection() cast to HttpURLConnection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            // Parser detects: connection.setRequestMethod()
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("Accept", "application/json");

            // Parser detects: connection.getResponseCode()
            int responseCode = connection.getResponseCode();
            log.info("URLConnection GET {} returned {}", urlString, responseCode);

            if (responseCode >= 400) {
                throw new IOException("HTTP error: " + responseCode);
            }

            // Parser detects: connection.getInputStream()
            return readInputStream(connection.getInputStream());

        } finally {
            connection.disconnect();
        }
    }

    /**
     * POST request with JSON body.
     * Parser detects: setDoOutput(), getOutputStream()
     */
    public String post(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // Parser detects: connection.setDoOutput(true)
            connection.setDoOutput(true);

            // Parser detects: connection.getOutputStream()
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            log.info("URLConnection POST {} returned {}", urlString, responseCode);

            if (responseCode >= 400) {
                String errorResponse = readInputStream(connection.getErrorStream());
                throw new IOException("HTTP error " + responseCode + ": " + errorResponse);
            }

            return readInputStream(connection.getInputStream());

        } finally {
            connection.disconnect();
        }
    }

    /**
     * PUT request.
     */
    public String put(String urlString, String jsonBody) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            return readInputStream(connection.getInputStream());

        } finally {
            connection.disconnect();
        }
    }

    /**
     * DELETE request.
     */
    public int delete(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("DELETE");
            return connection.getResponseCode();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * GET with custom headers.
     */
    public String getWithHeaders(String urlString, java.util.Map<String, String> headers) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");

            for (var entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            return readInputStream(connection.getInputStream());

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Check if URL is reachable.
     */
    public boolean isReachable(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode >= 200 && responseCode < 400;
        } catch (IOException e) {
            return false;
        }
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
