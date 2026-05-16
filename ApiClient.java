package com.bloodbank.ui.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Static HTTP client for all backend API calls.
 * Stores the JWT token and attaches it to every request.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    private static String authToken = null;

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) ->
                            LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
            .registerTypeAdapter(LocalTime.class,
                    (JsonDeserializer<LocalTime>) (json, type, ctx) ->
                            LocalTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalTime.class,
                    (JsonSerializer<LocalTime>) (src, type, ctx) ->
                            new JsonPrimitive(src.toString()))
            .create();

    // ── Token Management ──────────────────────────────────────────────────────

    public static void setToken(String token) { authToken = token; }
    public static void clearToken() { authToken = null; }
    public static Gson getGson() { return GSON; }

    // ── Request Builder ───────────────────────────────────────────────────────

    private static Request.Builder buildRequest(String url) {
        Request.Builder builder = new Request.Builder().url(BASE_URL + url);
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }
        return builder;
    }

    // ── HTTP Methods ──────────────────────────────────────────────────────────

    /** GET request — returns raw JSON string */
    public static String get(String endpoint) throws IOException {
        Request request = buildRequest(endpoint).get().build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + body);
            return body;
        }
    }

    /** POST with a Java object (serialized to JSON by Gson) */
    public static String post(String endpoint, Object payload) throws IOException {
        String json = GSON.toJson(payload);
        RequestBody body = RequestBody.create(json, JSON_TYPE);
        Request request = buildRequest(endpoint).post(body).build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + responseBody);
            return responseBody;
        }
    }

    /** POST with a raw JSON string (for hand-crafted payloads) */
    public static String postRaw(String endpoint, String jsonPayload) throws IOException {
        String json = jsonPayload != null ? jsonPayload : "{}";
        RequestBody body = RequestBody.create(json, JSON_TYPE);
        Request request = buildRequest(endpoint).post(body).build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + responseBody);
            return responseBody;
        }
    }

    /** PUT with a Java object */
    public static String put(String endpoint, Object payload) throws IOException {
        String json = payload != null ? GSON.toJson(payload) : "{}";
        RequestBody body = RequestBody.create(json, JSON_TYPE);
        Request request = buildRequest(endpoint).put(body).build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + responseBody);
            return responseBody;
        }
    }

    /** PUT with a raw JSON string */
    public static String putRaw(String endpoint, String jsonPayload) throws IOException {
        String json = jsonPayload != null ? jsonPayload : "{}";
        RequestBody body = RequestBody.create(json, JSON_TYPE);
        Request request = buildRequest(endpoint).put(body).build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + responseBody);
            return responseBody;
        }
    }

    /** DELETE request */
    public static String delete(String endpoint) throws IOException {
        Request request = buildRequest(endpoint).delete().build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + ": " + responseBody);
            return responseBody;
        }
    }

    // ── Deserialization Helpers ───────────────────────────────────────────────

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    /** Deserialize a JSON array into a List<T> */
    public static <T> List<T> listFromJson(String json, Class<T> elementClass) {
        Type listType = TypeToken.getParameterized(List.class, elementClass).getType();
        return GSON.fromJson(json, listType);
    }
}
