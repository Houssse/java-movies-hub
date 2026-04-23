package ru.practicum.moviehub.store;

import com.google.gson.Gson;
import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new HashMap<>();
    private final Gson gson = new Gson();
    private int nextId = 1;

    public void add(Movie movie) {
        movie.setId(nextId++);
        movies.put(movie.getId(), movie);
    }

    public String getAll() {
        return gson.toJson(new ArrayList<>(movies.values()));
    }

    public void clearAll() {
        movies.clear();
        nextId = 1;
    }

    public void delete(int id) {
        movies.remove(id);
    }

    public Movie getById(int id) {
        return movies.get(id);
    }

    public List<Movie> getByYear(int year) {
        return movies.values().stream()
                .filter(m -> m.getYear() == year)
                .collect(Collectors.toList());
    }

}
