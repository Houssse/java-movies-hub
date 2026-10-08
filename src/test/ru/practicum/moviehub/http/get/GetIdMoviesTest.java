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

public class GetIdMoviesTest {
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

        Movie movie = new Movie("movies", 1924);
        moviesStore.addMovie(movie);

        id = movie.getId();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovieById_existingId_returnsMovie() throws IOException, InterruptedException {
        HttpResponse<String> resp = getIdMovies(id);

        assertEquals(200, resp.statusCode(), "GET /movies/{id} должен вернуть 200");
        assertEquals("", resp.body());
    }

    @Test
    void getMovieById_missingId_returns404() throws IOException, InterruptedException {
        HttpResponse<String> resp = getIdMovies(-1);

        assertEquals(404, resp.statusCode(), "должен вернуть 404");
        assertEquals("Фильм не найден", resp.body());
    }

    @Test
    void getMovieById_invalidId_returns400() throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/ss"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "должен вернуть 400");
        assertEquals("Некорректный ID", resp.body(), "Ожидалось Некорректный ID");
    }



    private HttpResponse<String> getIdMovies(int id) throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + id))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
