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
import java.util.*;

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
                    String query = ex.getRequestURI().getQuery();

                    if ((query != null && query.startsWith("year="))) {
                        String yearStr = query.substring("year=".length());
                        try {
                            int year = Integer.parseInt(yearStr);
                            getMoviesYear(ex, year);
                        } catch (NumberFormatException e) {
                            sendJson(ex, 400, "Некорректный параметр запроса — 'year'");
                        }
                    } else if (parts.length == 2) {
                        get(ex);
                    } else if (parts.length == 3) {
                        getId(ex, parts);
                    }
                    break;
                case "POST":
                    post(ex);
                    break;
                case "DELETE":
                    delete(ex);
                    break;
                default:
                    sendNoContent(405, ex);
            }
        } catch (
                Exception e) {
            System.out.println("Ошибка в handle: " + e.getMessage());
            e.printStackTrace();
            sendJson(ex, 500, "{\"error\":\"Внутренняя ошибка\"}");
        }
    }

    private void get(HttpExchange ex) throws IOException {
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
        } catch (NumberFormatException e) {
            sendJson(ex, 400, "Некорректный ID");
        }
    }

    private void getMoviesYear(HttpExchange ex, int year) throws IOException {
        List<Movie> movies = store.getMoviesYear(year);
        String json = new Gson().toJson(movies);

        sendJson(ex, 200, json);
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

    private void delete(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            String[] parts = path.split("/");
            String id = parts[2];
            Optional<Movie> movie = store.getMovie(Integer.parseInt(id));

            if (movie.isEmpty()) {
                sendNoContent(404, ex);
            } else {
                store.deleteMovie(Integer.parseInt(id));
                sendNoContent(204, ex);
            }
        } catch (NumberFormatException e) {
            sendJson(ex, 400, "Некорректный ID");
        }
    }
}