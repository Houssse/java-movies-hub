package ru.practicum.moviehub.store;

import com.google.gson.Gson;
import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new HashMap<>();
    private int nextId = 1;

    public String addMovie(Movie movie) {
        movie.setId(nextId++);
        movies.put(movie.getId(), movie);
        return new Gson().toJson(movie);
    }

    public String getAll() {
        return new Gson().toJson(movies.values());
    }

    public Optional<Movie> getMovie(int id) {
        return Optional.ofNullable(movies.get(id));
    }

    public void clear() {
        movies.clear();
        nextId = 1;
    }
}