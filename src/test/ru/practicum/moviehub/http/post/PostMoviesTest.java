package ru.practicum.moviehub.http.post;

import org.junit.jupiter.api.*;
import ru.practicum.moviehub.http.MoviesServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostMoviesTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore moviesStore = new MoviesStore();

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
        moviesStore.clear();
    }

    @Test
    void postMovie_valid_returns201() throws IOException, InterruptedException {
        String jsonBody = "{\"title\":\"Inception\",\"year\":2010}";

        HttpResponse<String> resp = postMovie(jsonBody);

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        assertEquals("[{\"id\":1,\"title\":\"Inception\",\"year\":2010}]", moviesStore.getAll());
    }

    @Test
    void postMovie_emptyTitle_returns422() throws IOException, InterruptedException {
        String jsonBody = "{\"title\":\"\",\"year\":2010}";

        HttpResponse<String> resp = postMovie(jsonBody);

        assertEquals(422, resp.statusCode(), "Должен вернуть 422");
        assertEquals("{\"error\":\"Ошибка валидации\"," +
                        "\"details\":[\"название не должно быть пустым\"]}", resp.body(),
                "Должен вернуть \"error\":\"Ошибка валидации\",\"" +
                        "details\":[\"название не должно быть пустым\"");
    }

    @Test
    void postMovie_emptyYear_returns422() throws IOException, InterruptedException {
        int validYear = LocalDate.now().getYear() + 1;
        String jsonBody = "{\"title\":\"Inception\"}";

        HttpResponse<String> resp = postMovie(jsonBody);

        assertEquals(422, resp.statusCode(), "Должен вернуть 422");
        assertEquals("{\"error\":\"Ошибка валидации\"," +
                        "\"details\":[\"год должен быть между 1888 и " + validYear + "\"]}", resp.body(),
                "Должен вернуть {\"error\":\"Ошибка валидации\"," +
                        "\"details\":[\"год должен быть между 1888 и " + validYear + "\"]}");
    }

    @Test
    void postMovie_tooLongTitle_returns422() throws IOException, InterruptedException {
        String longTitle = "a".repeat(101);
        String jsonBody = "{\"title\":\"" + longTitle + "\",\"year\":2010}";

        HttpResponse<String> resp = postMovie(jsonBody);

        assertEquals(422, resp.statusCode(), "Должен вернуть 422");
        assertEquals("{\"error\":\"Ошибка валидации\"," +
                        "\"details\":[\"название не может быть больше 100 символов\"]}", resp.body(),
                "Должен вернуть {\"error\":\"Ошибка валидации\"," +
                        "\"details\":[\"название не может быть больше 100 символов\"]}");
    }

    @Test
    void postMovie_movieYearLessThanValid() throws IOException, InterruptedException {
        int validYear = LocalDate.now().getYear() + 1;
        String jsonBody = "{\"title\":\"Inception\",\"year\":1885}";

        HttpResponse<String> resp = postMovie(jsonBody);

        assertEquals(422, resp.statusCode(), "Должен вернуть 422");
        assertEquals("{\"error\":\"Ошибка валидации\"," +
                        "\"details\":[\"год должен быть между 1888 и " + validYear + "\"]}", resp.body(),
                "Должен вернуть {\"error\":\"Ошибка валидации\"," +
                        "\"details\":[\"год должен быть между 1888 и " + validYear + "\"]}");
    }

    @Test
    void postMovie_movieYearGreaterThanValid() throws IOException, InterruptedException {
        int invalidYear = LocalDate.now().getYear() + 2;
        int validYear = LocalDate.now().getYear() + 1;
        String jsonBody = "{\"title\":\"Inception\",\"year\":" + invalidYear + "}";

        HttpResponse<String> resp = postMovie(jsonBody);

        assertEquals(422, resp.statusCode(), "Должен вернуть 422");
        assertEquals("{\"error\":\"Ошибка валидации\"," +
                        "\"details\":[\"год должен быть между 1888 и " + validYear + "\"]}", resp.body(),
                "Должен вернуть {\"error\":\"Ошибка валидации\"," +
                        "\"details\":[\"год должен быть между 1888 и " + validYear + "\"]}");
    }

    @Test
    void postMovie_tooInvalidHandle_returns415() throws IOException, InterruptedException {
        String jsonBody = "{\"title\":\"Inception\",\"year\":2010}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/txt; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(2))
                .build();

        HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(415, resp.statusCode(), "Должен вернуть 415");

    }

    private HttpResponse<String> postMovie(String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(2))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
