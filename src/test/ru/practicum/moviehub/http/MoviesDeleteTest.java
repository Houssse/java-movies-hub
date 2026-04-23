package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesDeleteTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static final String TEST_MOVIE = "{\"title\":\"Inception\",\"year\":2010}";

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        server = new MoviesServer();
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {

    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void deleteMovieById_withExistingId_returnsNoContent() throws Exception {
        HttpRequest reqCreate = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(TEST_MOVIE, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(2))
                .build();

        client.send(reqCreate, HttpResponse.BodyHandlers.ofString());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(204, response.statusCode());
        assertEquals("[]", server.store.getAll());
    }

    @Test
    void deleteMovieById_withNonExistingId_returnsNotFound() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

        String expecting = "{\"error\":\"Фильм не найден\",\"details\":[\"Фильм с id 1 не существует\"]}";
        String actual = response.body();

        assertEquals(404, response.statusCode());
        assertEquals(expecting, actual);
    }

    @Test
    void deleteMovieById_withNonNumericId_returnsBadRequest() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/blablas"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

        String expecting = "{\"error\":\"Некорректный id\",\"details\":[\"id должен быть числом\"]}";
        String actual = response.body();

        assertEquals(400, response.statusCode());
        assertEquals(expecting, actual);

    }
}
