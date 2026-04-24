package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesPostTest {
    private static final String BASE = "http://localhost:8080";
    private static final String TEST_MOVIE = "{\"title\": \"Inception\", \"year\": 2010}";
    private static MoviesServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(new MoviesStore(), 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        server.clearStore();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void postMovie_withValidMovie_returnsCreatedStatus() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(TEST_MOVIE))
                .timeout(Duration.ofSeconds(2))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, resp.statusCode());
        assertEquals("application/json; charset=UTF-8",
                resp.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    void postMovie_addsMovieToStorage() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(TEST_MOVIE))
                .timeout(Duration.ofSeconds(2))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());

        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();
        HttpResponse<String> getResp = client.send(getReq, HttpResponse.BodyHandlers.ofString());

        String expecting = "[{\"title\":\"Inception\",\"year\":2010,\"id\":1}]";
        assertEquals(expecting, getResp.body().trim());
    }

    @Test
    void postMovie_withEmptyTitle_returnsBadRequest() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"\", \"year\": 2010}"))
                .timeout(Duration.ofSeconds(2))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        String expecting = "{\"error\":\"Ошибка валидации\",\"details\":[\"название не должно быть пустым\"]}";
        assertEquals(422, resp.statusCode());
        assertEquals(expecting, resp.body().trim());
    }

    @Test
    void postMovie_withTooLongTitle_returnsBadRequest() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"" +
                        "a".repeat(101) + "\", \"year\": 2010}"))
                .timeout(Duration.ofSeconds(2))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        String expecting = "{\"error\":\"Ошибка валидации\",\"details\"" +
                ":[\"название не должно быть больше 100 символов\"]}";
        assertEquals(422, resp.statusCode());
        assertEquals(expecting, resp.body().trim());
    }

    @Test
    void postMovie_withInvalidYear_returnsBadRequest() throws Exception {
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Inception\", \"year\": 2123}"))
                .timeout(Duration.ofSeconds(2))
                .build();

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\": \"Inception\", \"year\": 1881}"))
                .timeout(Duration.ofSeconds(2))
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> resp2 = client.send(req2, HttpResponse.BodyHandlers.ofString());

        assertEquals(422, resp1.statusCode());
        assertTrue(resp1.body().contains("год должен быть между 1888 и"));
        assertEquals(422, resp2.statusCode());
        assertTrue(resp2.body().contains("год должен быть между 1888 и"));
    }
}