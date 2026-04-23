package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MoviesHandler extends BaseHttpHandler{

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
            byte[] bytes = "[]".getBytes(StandardCharsets.UTF_8);
            sendJson(ex, 201, "[]");
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
