package com.java.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.java.chat.GeminiClient.StructuredGenerateResult;

import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import tools.jackson.databind.ObjectMapper;

class GeminiClientFallbackTest {

    @Test
    void chat_usesSecondKey_whenFirstKeyGets429() {
        try (SequenceHttpClient fakeHttp = new SequenceHttpClient()) {
            fakeHttp.enqueue(429, "{\"error\":{\"message\":\"quota exceeded\"}}");
            fakeHttp.enqueue(200, "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"fallback-ok\"}]}}]}");
            GeminiClient client = new GeminiClient(new ObjectMapper());
            client.setHttpClientForTest(fakeHttp);
            ReflectionTestUtils.setField(client, "apiKey", "first-key");
            ReflectionTestUtils.setField(client, "apiKey2", "second-key");

            String out = client.chat(List.of(new ChatMessage("user", "테스트")));
            assertEquals("fallback-ok", out);
            assertEquals(2, fakeHttp.callCount());
        }
    }

    @Test
    void generateStructuredResult_quotaExceeded_whenAllKeysReturn429() {
        try (SequenceHttpClient fakeHttp = new SequenceHttpClient()) {
            fakeHttp.enqueue(429, "{\"error\":{\"message\":\"quota exceeded\"}}");
            fakeHttp.enqueue(429, "{\"error\":{\"message\":\"quota exceeded\"}}");
            GeminiClient client = new GeminiClient(new ObjectMapper());
            client.setHttpClientForTest(fakeHttp);
            ReflectionTestUtils.setField(client, "apiKey", "first-key");
            ReflectionTestUtils.setField(client, "apiKey2", "second-key");

            StructuredGenerateResult r = client.generateStructuredResult("s", "u", 0.2, true, 384);
            assertEquals(StructuredGenerateResult.Kind.QUOTA_EXCEEDED, r.kind());
            assertEquals("", r.rawText());
            assertEquals(2, fakeHttp.callCount());
        }
    }

    @Test
    void generateStructuredResult_ok_whenSecondKeyReturns200() {
        try (SequenceHttpClient fakeHttp = new SequenceHttpClient()) {
            fakeHttp.enqueue(429, "{\"error\":{\"message\":\"quota exceeded\"}}");
            fakeHttp.enqueue(200, "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"{\\\"a\\\":1}\"}]}}]}");
            GeminiClient client = new GeminiClient(new ObjectMapper());
            client.setHttpClientForTest(fakeHttp);
            ReflectionTestUtils.setField(client, "apiKey", "first-key");
            ReflectionTestUtils.setField(client, "apiKey2", "second-key");

            StructuredGenerateResult r = client.generateStructuredResult("s", "u", 0.2, true, 384);
            assertEquals(StructuredGenerateResult.Kind.OK, r.kind());
            assertEquals("{\"a\":1}", r.rawText());
        }
    }

    private static final class SequenceHttpClient extends HttpClient {
        private final Deque<SimpleResponse> responses = new ArrayDeque<>();
        private int calls;

        SequenceHttpClient enqueue(int status, String body) {
            responses.addLast(new SimpleResponse(status, body));
            return this;
        }

        int callCount() {
            return calls;
        }

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return Redirect.NEVER;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return new SSLParameters();
        }

        @Override
        public Optional<java.net.Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            calls++;
            SimpleResponse next = responses.removeFirst();
            @SuppressWarnings("unchecked")
            HttpResponse<T> casted = (HttpResponse<T>) next;
            return casted;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException("Not needed for test");
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException("Not needed for test");
        }
    }

    private static final class SimpleResponse implements HttpResponse<String> {
        private final int statusCode;
        private final String body;

        private SimpleResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public HttpRequest request() {
            return HttpRequest.newBuilder().uri(URI.create("https://example.com")).build();
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (k, v) -> true);
        }

        @Override
        public URI uri() {
            return URI.create("https://example.com");
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

    }
}
