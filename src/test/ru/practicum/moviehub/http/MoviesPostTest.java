package ru.practicum.moviehub.http;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesPostTest {    private static final String BASE = "http://localhost:8080";
    private static final String TEST_MOVIE = "{\"title\": \"Inception\", \"year\": 2010}";
    private static MoviesServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer();
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        server.store.clear();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void  postMovie_withValidMovie_returnsCreatedStatus() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(TEST_MOVIE))
                .timeout(Duration.ofSeconds(2))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, resp.statusCode(), "Должен вернуть 201");

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

        String expecting = "[{\"title\":\"Inception\",\"year\":2010,\"id\":1}]";
        String actual = server.store.getAll();

        assertEquals(expecting, actual, "Ожидалось сохранение фильма");
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

        assertEquals(422, resp.statusCode(), "Ожидался код ошибки 422");
        assertEquals("{\"error\":\"Ошибка валидации\",\"details\":[\"название не должно быть пустым\"]}"
                , resp.body());
    }
}
