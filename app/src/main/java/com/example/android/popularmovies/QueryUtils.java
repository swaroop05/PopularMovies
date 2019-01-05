package com.example.android.popularmovies;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by meets on 3/7/2018.
 */

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getName();
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private static final int SUCCESS_RESPONSE_CODE = 200;
    private static final String KEY_RESULTS = "results";
    public static final String KEY_MOVIE_TITLE = "title";
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w342/";
    public static final String KEY_IMAGE_URL = "poster_path";
    public static final String KEY_RELEASE_DATE = "release_date";
    public static final String KEY_VOTE_AVERAGE = "vote_average";
    public static final String KEY_PLOT = "overview";
    public static final String KEY_MOVIE_ID = "id";
    public static final String KEY_TOTAL_RESULTS = "total_results";
    public static final String KEY_AUTHOR_OF_REVIEW = "author";
    public static final String KEY_CONTENT_OF_REVIEW = "content";
    public static final String KEY_NAME = "name";
    public static final String KEY_FIELD = "key";

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(READ_TIMEOUT /* milliseconds */);
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT /* milliseconds */);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == SUCCESS_RESPONSE_CODE) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Response code was not " + SUCCESS_RESPONSE_CODE + ". It was " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException was thrown in makeHttpRequest method", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            //InputStreamReader handles the translation process from the raw data to human readable characters.
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            //InputStreamReader only allows to read single character at a time. This can be avoided if we wrap InputStreamReader around BufferedReader.
            //BufferedReader will accept reading in character and will read and save larger chunk of data. So when programe requires to read another character
            //BufferedReader will have it stored already which helps us to avoid reading each character from InputStreamReader.
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Extracts the Movies information from JSON response received from Http request
     *
     * @param jsonResponse
     * @return ArrayList<Movies>
     */
    private static ArrayList<Movies> extractMoviesInfo(String jsonResponse) {
        // Create an empty ArrayList that we can start adding moviesInfo to
        ArrayList<Movies> moviesInfo = new ArrayList<>();
        try {
            if (jsonResponse != null) {
                JSONObject reader = new JSONObject(jsonResponse);
                JSONArray resultsArray = reader.optJSONArray(KEY_RESULTS);
                if (resultsArray != null) {
                    int arrayCount = resultsArray.length();
                    for (int i = 0; i < arrayCount; i++) {
                        JSONObject individualMovieDetails = resultsArray.getJSONObject(i);
                        String movieId = individualMovieDetails.optString(KEY_MOVIE_ID);
                        String movieTitle = individualMovieDetails.optString(KEY_MOVIE_TITLE);
                        String imageUrl = IMAGE_BASE_URL + individualMovieDetails.optString(KEY_IMAGE_URL);
                        String releaseDate = individualMovieDetails.optString(KEY_RELEASE_DATE);
                        String voteAverage = individualMovieDetails.optString(KEY_VOTE_AVERAGE);
                        String plot = individualMovieDetails.optString(KEY_PLOT);
                        moviesInfo.add(new Movies(movieId, imageUrl, movieTitle, releaseDate, voteAverage, plot));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the movies JSON results", e);
        }
        return moviesInfo;
    }

    /**
     * Extracts the Review information from JSON response received from Http request
     *
     * @param jsonResponse
     * @return ArrayList<Reviews>
     */
    private static ArrayList<Reviews> extractReviewsInfo(String jsonResponse) {
        // Create an empty ArrayList that we can start adding reviewsInfo to
        ArrayList<Reviews> reviewsInfo = new ArrayList<>();
        try {
            if (jsonResponse != null) {
                JSONObject reader = new JSONObject(jsonResponse);
                JSONArray resultsArray = reader.optJSONArray(KEY_RESULTS);


                if (resultsArray != null) {
                    int arrayCount = resultsArray.length();
                    for (int i = 0; i < arrayCount; i++) {
                        JSONObject individualReviewDetails = resultsArray.getJSONObject(i);
                        String authorOfReview = individualReviewDetails.optString(KEY_AUTHOR_OF_REVIEW);
                        String contentOfReview = individualReviewDetails.optString(KEY_CONTENT_OF_REVIEW);
                        reviewsInfo.add(new Reviews(authorOfReview, contentOfReview));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the reviews JSON results", e);
        }
        return reviewsInfo;
    }

    /**
     * Extracts the Trailers information from JSON response received from Http request
     *
     * @param jsonResponse
     * @return ArrayList<Reviews>
     */
    private static ArrayList<Trailers> extractTrailersInfo(String jsonResponse) {
        // Create an empty ArrayList that we can start adding reviewsInfo to
        ArrayList<Trailers> trailersInfo = new ArrayList<>();
        try {
            if (jsonResponse != null) {
                JSONObject reader = new JSONObject(jsonResponse);
                JSONArray resultsArray = reader.optJSONArray(KEY_RESULTS);


                if (resultsArray != null) {
                    int arrayCount = resultsArray.length();
                    for (int i = 0; i < arrayCount; i++) {
                        JSONObject individualTrailerDetails = resultsArray.getJSONObject(i);
                        String trailerName = individualTrailerDetails.optString(KEY_NAME);
                        String trailerKey = individualTrailerDetails.optString(KEY_FIELD);
                        trailersInfo.add(new Trailers(trailerName, trailerKey));
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the trailers JSON results", e);
        }
        return trailersInfo;
    }

    /**
     * Core method used to fetch moviesInfo as List which users all helper methods to make URL object
     * Http request, read from Input stream, Parse JSON
     */
    public static List<Movies> fetchMoviesInfos(String stringUrl) {

        // Create URL object
        URL url = createUrl(stringUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = QueryUtils.makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making HttpRequest", e);
        }
        return extractMoviesInfo(jsonResponse);
    }


    /**
     * Core method used to fetch reviewsInfo as List which users all helper methods to make URL object
     * Http request, read from Input stream, Parse JSON
     */
    public static List<Reviews> fetchReviewInfos(String stringUrl) {

        // Create URL object
        URL url = createUrl(stringUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = QueryUtils.makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making HttpRequest", e);
        }
        return extractReviewsInfo(jsonResponse);
    }

    /**
     * Core method used to fetch trailersInfo as List which users all helper methods to make URL object
     * Http request, read from Input stream, Parse JSON
     */
    public static List<Trailers> fetchTrailerInfos(String stringUrl) {

        // Create URL object
        URL url = createUrl(stringUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = "";
        try {
            jsonResponse = QueryUtils.makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making HttpRequest", e);
        }
        return extractTrailersInfo(jsonResponse);
    }


}
