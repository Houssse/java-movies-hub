package ru.practicum.moviehub.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

public class MovieValidator {

    public static boolean isValidateHandle(HttpExchange ex) {
        String contentType = ex.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.contains("application/json")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidateBody(String body) throws IOException {
        Gson gson = new Gson();
        Movie movie = gson.fromJson(body, Movie.class);

        if (movie.getTitle() == null || movie.getTitle().isEmpty() || movie.getTitle().length() > 100) {
            return false;
        }

        if (movie.getYear() < 1888 || movie.getYear() > LocalDate.now().getYear() + 1) {
            return false;
        }

        return true;
    }

    public static String errorsMessages(String body) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse("Ошибка валидации", new ArrayList<>());
        Gson gson = new Gson();
        Movie movie = gson.fromJson(body, Movie.class);
        JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();

        if (movie.getTitle() == null || movie.getTitle().isEmpty()) {
            errorResponse.addDetails("название не должно быть пустым");
        } else if (movie.getTitle().length() > 100) {
            errorResponse.addDetails("название не может быть больше 100 символов");
        }

        if (!jsonObject.has("year") || jsonObject.get("year").isJsonNull()) {
            errorResponse.addDetails("год не может быть пустым");
        } else if (movie.getYear() < 1888 || movie.getYear() > LocalDate.now().getYear() + 1) {
            errorResponse.addDetails("год должен быть между 1888 и " + (LocalDate.now().getYear() + 1));
        }

        return gson.toJson(errorResponse);
    }
}
