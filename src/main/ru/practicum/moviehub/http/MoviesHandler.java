package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.MovieValidator;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if (method.equalsIgnoreCase("GET")) {
                sendJson(ex, 200, store.getAll());
            } else if (method.equalsIgnoreCase("POST")) {
                if (MovieValidator.isValidateHandle(ex)) {
                    String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    if (MovieValidator.isValidateBody(body)) {
                        JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                        Movie movie = new Gson().fromJson(json, Movie.class);
                        String answer = store.addMovie(movie);

                        sendJson(ex, 201, answer);
                    } else {
                        sendJson(ex, 422, MovieValidator.errorsMessages(body));
                    }
                } else {
                    sendNoContent(415, ex);
                }
            } else {
                sendNoContent(204, ex);
            }
        } catch (Exception e) {
            System.out.println("Ошибка в handle: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Внутренняя ошибка\"}");
        }
    }
}