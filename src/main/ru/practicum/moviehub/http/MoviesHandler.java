package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MoviesHandler extends BaseHttpHandler{
    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            byte[] bytes = "[]".getBytes(StandardCharsets.UTF_8);
            sendJson(ex, 200, "[]");
            try (OutputStream os = ex.getResponseBody()){
                os.write(bytes);
            }
        } else if (method.equalsIgnoreCase("POST")) {
            String body = new String(ex.getRequestBody().readAllBytes());
            store.add(body);

            byte[] bytes = "[]".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "application/json");
            sendJson(ex, 201, "[]");
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
