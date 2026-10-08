package ru.practicum.moviehub.http.delete;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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

public class DeleteMoviesTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore moviesStore = new MoviesStore();
    private static HttpRequest request;
    private static int id;


    @BeforeAll
    static void beforeAll() throws IOException {
        server = new MoviesServer(moviesStore, 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @BeforeEach
    void beforeEach() {
        Movie movie = new Movie("movies", 1924);
        moviesStore.addMovie(movie);

        id = movie.getId();
    }

    @Test
    void deleteMovie_existingId_returns204() throws IOException, InterruptedException {
        HttpResponse<String> response = DeleteIdMovies(id);

        assertEquals(204, response.statusCode(), "Должен вернуть 204");
    }

    @Test
    void deleteMovie_notExistingId_returns404() throws IOException, InterruptedException {
        HttpResponse<String> response = DeleteIdMovies(id + 1);

        assertEquals(404, response.statusCode(), "Должен вернуть 404");
    }

    @Test
    void deleteMovie_invalidId_returns400() throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/ss"))
                .DELETE()
                .build();

        HttpResponse<String> response = client .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Должен вернуть 400");
        assertEquals("Некорректный ID", response.body(), "Ожидалось Некорректный ID");
    }

    private HttpResponse<String> DeleteIdMovies(int id) throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .DELETE()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
