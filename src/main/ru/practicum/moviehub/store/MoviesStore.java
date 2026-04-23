package ru.practicum.moviehub.store;

import com.google.gson.Gson;
import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Map;

public class MoviesStore {
    private final Gson gson = new Gson();
    private final Map<Integer, Movie> movies = new HashMap<>();
    private int nextId = 1;

    public void add(String json) {
        Movie movie = gson.fromJson(json, Movie.class);
        movie.setId(nextId++);
        movies.put(movie.getId(), movie);
    }

    public String getAll() {
        return gson.toJson(movies.values());
    }

    public void clear() {
        movies.clear();
        nextId = 1;
    }

}
