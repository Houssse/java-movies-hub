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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesGetTest {
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

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(TEST_MOVIE, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(2))
                .build();

        client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeEach
    void beforeEach() {

    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));


        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_whenMoviesExist_returnsArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "[{\"title\":\"Inception\",\"year\":2010,\"id\":1}]";
        String actual = resp.body().trim();

        assertEquals(expected, actual, "Должен вернуть массив с фильмом");
    }

    @Test
    void getMovieById_withExistingId_returnsMovie() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "{\"title\":\"Inception\",\"year\":2010,\"id\":1}";
        String actual = resp.body().trim();

        assertEquals(200, resp.statusCode());
        assertEquals(expected, actual);

    }

    @Test
    void getMovieById_withNonExistingId_returnsNotFound() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/4"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "{\"error\":\"Фильм не найден\",\"details\":[\"Фильм с id 4 не существует\"]}";
        String actual = resp.body().trim();
        assertEquals(404, resp.statusCode());
        assertEquals(expected, actual);
    }

    @Test
    void getMovieById_withNonNumericId_returnsBadRequest() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/blabla"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "{\"error\":\"Некорректный id\",\"details\":[\"id должен быть числом\"]}";
        String actual = resp.body().trim();
        assertEquals(404, resp.statusCode());
        assertEquals(expected, actual);
    }
}
