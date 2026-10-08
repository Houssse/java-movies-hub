package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.api.MovieValidator;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            switch (ex.getRequestMethod()) {
                case "GET":
                    String path = ex.getRequestURI().getPath();
                    String[] parts = path.split("/");

                    if (parts.length == 2) {
                        get(ex);
                    } else if (parts.length == 3) {
                        getId(ex, parts);
                    }
                    break;
                case "POST":
                    post(ex);
                    break;
                default:
                    sendNoContent(204, ex);
            }
        } catch (Exception e) {
            System.out.println("Ошибка в handle: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Внутренняя ошибка\"}");
        }
    }

    private void get(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        sendJson(ex, 200, store.getAll());
    }

    private void getId(HttpExchange ex, String[] parts) throws IOException {
        try {
            String id = parts[2];
            Optional<Movie> movie = store.getMovie(Integer.parseInt(id));
            if (movie.isEmpty()) {
                sendJson(ex, 404, "Фильм не найден");
            } else {
                String json = new Gson().toJson(movie.get());
                sendJson(ex, 200, json);
            }
        } catch (NumberFormatException e){
            sendJson(ex, 400, "Некорректный ID");
        }
    }


    private void post(HttpExchange ex) throws IOException {
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
    }
}