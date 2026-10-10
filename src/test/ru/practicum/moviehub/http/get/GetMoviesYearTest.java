package ru.practicum.moviehub.http.get;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetMoviesYearTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore moviesStore = new MoviesStore();
    private static HttpRequest request;

    @BeforeAll
    static void beforeAll() throws IOException {
        server = new MoviesServer(moviesStore, 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        Movie movie = new Movie("movies", 1924);
        moviesStore.addMovie(movie);
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMoviesByYear_existingYear_returnsFilteredMovies() throws IOException, InterruptedException {
        HttpResponse<String> response = gerYear(1924);

        assertEquals(200, response.statusCode(), "Должен вернуть 200");
        assertEquals("[{\"id\":1,\"title\":\"movies\",\"year\":1924}]", response.body());
    }

    @Test
    void getMoviesByYear_missingYear_returnsEmptyArray() throws IOException, InterruptedException {
        HttpResponse<String> response = gerYear(1925);

        assertEquals(200, response.statusCode(), "Должен вернуть 200");
        assertEquals("[]", response.body(), "Должен вернуть пустой массив");
    }

    @Test
    void getMoviesByYear_invalidYear_returns400() throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=" + "year"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
        assertEquals("Некорректный параметр запроса — 'year'", resp.body());
    }

    private HttpResponse gerYear(int year) throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=" + year))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
