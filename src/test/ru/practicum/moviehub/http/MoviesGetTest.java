package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesGetTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static final String TEST_MOVIE = "{\"title\":\"Inception\",\"year\":2010}";

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
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
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
        assertEquals("application/json; charset=UTF-8",
                resp.headers().firstValue("Content-Type").orElse(""));
        assertEquals("[]", resp.body().trim());
    }

    @Test
    void getMovies_whenMoviesExist_returnsArray() throws Exception {
        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(TEST_MOVIE, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(2))
                .build();
        client.send(postReq, HttpResponse.BodyHandlers.ofString());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "[{\"title\":\"Inception\",\"year\":2010,\"id\":1}]";
        assertEquals(expected, resp.body().trim());
        assertEquals("application/json; charset=UTF-8",
                resp.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    void getMovieById_withExistingId_returnsMovie() throws Exception {
        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(TEST_MOVIE, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(2))
                .build();
        client.send(postReq, HttpResponse.BodyHandlers.ofString());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "{\"title\":\"Inception\",\"year\":2010,\"id\":1}";
        assertEquals(200, resp.statusCode());
        assertEquals(expected, resp.body().trim());
        assertEquals("application/json; charset=UTF-8",
                resp.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    void getMovieById_withNonExistingId_returnsNotFound() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/4"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "{\"error\":\"Фильм не найден\",\"details\":[\"Фильм с id 4 не существует\"]}";
        assertEquals(404, resp.statusCode());
        assertEquals(expected, resp.body().trim());
    }

    @Test
    void getMovieById_withNonNumericId_returnsBadRequest() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/blabla"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "{\"error\":\"Некорректный id\",\"details\":[\"id должен быть числом\"]}";
        assertEquals(400, resp.statusCode());
        assertEquals(expected, resp.body().trim());
    }

    @Test
    void getMoviesByYear_withExistingYear_returnsFilteredList() throws Exception {
        // Добавляем фильм
        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .headers("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(TEST_MOVIE, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(2))
                .build();
        client.send(postReq, HttpResponse.BodyHandlers.ofString());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2010"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "[{\"title\":\"Inception\",\"year\":2010,\"id\":1}]";
        assertEquals(200, resp.statusCode());
        assertEquals(expected, resp.body().trim());
        assertEquals("application/json; charset=UTF-8",
                resp.headers().firstValue("Content-Type").orElse(""));
    }

    @Test
    void getMoviesByYear_whenNoMoviesForYear_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=1999"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
        assertEquals("application/json; charset=UTF-8",
                resp.headers().firstValue("Content-Type").orElse(""));
        assertEquals("[]", resp.body().trim());
    }

    @Test
    void getMoviesByYear_withNonNumericYear_returnsBadRequest() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=blabla"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String expected = "{\"error\":\"Некорректный year\",\"details\":[\"year должен быть числом\"]}";
        assertEquals(400, resp.statusCode());
        assertEquals(expected, resp.body().trim());
    }
}