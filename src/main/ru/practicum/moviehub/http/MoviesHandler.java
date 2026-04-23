package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
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
            sendJson(ex, 200, "[]");

        } else if (method.equalsIgnoreCase("POST")) {
            String body = new String(ex.getRequestBody().readAllBytes());
            store.add(body);

            sendJson(ex, 201, "[]");
        }
    }
}
