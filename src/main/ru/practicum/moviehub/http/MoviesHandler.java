package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;
    private final Gson gson = new Gson();

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            try {
                sendJson(ex, 200, store.getAll());
            } catch (Exception e) {

            } finally {
                ex.close();
            }
        } else if (method.equalsIgnoreCase("POST")) {
            try {
                String contentType = ex.getRequestHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.contains("application/json")) {
                    sendError(ex, 415, "Неправильный Content-Type",
                            List.of("Ожидается application/json"));
                    return;
                }

                Movie movie;
                try {
                    String body = new String(ex.getRequestBody().readAllBytes());
                    movie = gson.fromJson(body, Movie.class);
                } catch (Exception e) {
                    sendError(ex, 400, "Некорректный JSON", List.of(e.getMessage()));
                    return;
                }

                if (!validate(movie, ex)) {
                    return;
                }

                store.add(movie);
                sendJson(ex, 201, gson.toJson(movie));
            } finally {
                ex.close();
            }
        }
    }

    private boolean validate(Movie movie, HttpExchange ex) throws IOException {
        List<String> errors = new ArrayList<>();

        if (movie.getTitle() == null || movie.getTitle().isEmpty()) {
            errors.add("название не должно быть пустым");
        }
        if (movie.getTitle() != null && movie.getTitle().length() > 100) {
            errors.add("название не должно быть больше 100 символов");
        }
        if (movie.getYear() < 1888 || movie.getYear() > 2026) {
            errors.add("год должен быть между 1888 и 2026");
        }

        if (!errors.isEmpty()) {
            sendError(ex, 422, "Ошибка валидации", errors);
            return false;
        }
        return true;
    }

    private void sendError(HttpExchange ex, int status, String error, List<String> details) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(error, details);
        sendJson(ex, status, gson.toJson(errorResponse));
    }
}
