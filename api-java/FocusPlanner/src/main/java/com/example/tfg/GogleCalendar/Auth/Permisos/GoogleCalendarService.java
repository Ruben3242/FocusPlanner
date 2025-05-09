package com.example.tfg.GogleCalendar.Auth.Permisos;

import com.example.tfg.model.Task;
import com.example.tfg.model.User;
import com.example.tfg.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GoogleCalendarService {

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.secret}")
    private String googleClientSecret;

    private final UserRepository userRepository;

    public GoogleCalendarService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public Calendar getCalendarClient(User user) throws IOException, GeneralSecurityException, InterruptedException {
        refreshAccessTokenIfExpired(user);
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance())
                .setClientSecrets(googleClientId, googleClientSecret)
                .build()
                .setAccessToken(user.getGoogleAccessToken())
                .setRefreshToken(user.getGoogleRefreshToken());

        return new Calendar.Builder(
                credential.getTransport(),
                credential.getJsonFactory(),
                credential
        ).setApplicationName("Focus Planner").build();
    }

    public String createCalendarEvent(Task task, User user) throws IOException, GeneralSecurityException, InterruptedException {
        refreshAccessTokenIfExpired(user);
        Calendar calendar = getCalendarClient(user);

        Event event = new Event()
                .setSummary(task.getTitle())
                .setDescription(task.getDescription());

        Date startDate = Date.from(task.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant());

        EventDateTime start = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startDate));
        event.setStart(start);
        event.setEnd(start); // opcionalmente, puedes añadir duración real

        Event createdEvent = calendar.events().insert("primary", event).execute();
        return createdEvent.getId();
    }

    public void deleteCalendarEvent(String eventId, User user) throws IOException, GeneralSecurityException, InterruptedException {
        refreshAccessTokenIfExpired(user);
        Calendar calendar = getCalendarClient(user);
        calendar.events().delete("primary", eventId).execute();
    }

    public void updateCalendarEvent(Task task, User user) throws IOException, GeneralSecurityException, InterruptedException {
        refreshAccessTokenIfExpired(user);
        Calendar calendar = getCalendarClient(user);
        Event event = calendar.events().get("primary", task.getGoogleCalendarEventId()).execute();

        event.setSummary(task.getTitle());
        event.setDescription(task.getDescription());
        Date startDate = Date.from(task.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        EventDateTime start = new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startDate));
        event.setStart(start);
        event.setEnd(start);

        calendar.events().update("primary", event.getId(), event).execute();
    }
    public void refreshAccessTokenIfExpired(User user) throws IOException, InterruptedException {
        if (user.getGoogleAccessTokenExpiry() == null || user.getGoogleAccessTokenExpiry().isBefore(Instant.now())) {
            if (user.getGoogleRefreshToken() == null) {
                throw new RuntimeException("No refresh token available for user: " + user.getEmail());
            }

            String refreshToken = user.getGoogleRefreshToken();
            String clientId = googleClientId;
            String clientSecret = googleClientSecret;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            "client_id=" + clientId +
                                    "&client_secret=" + clientSecret +
                                    "&refresh_token=" + refreshToken +
                                    "&grant_type=refresh_token"
                    ))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(response.body());

                String newAccessToken = jsonNode.get("access_token").asText();
                int expiresIn = jsonNode.get("expires_in").asInt();

                user.setGoogleAccessToken(newAccessToken);
                user.setGoogleAccessTokenExpiry(Instant.now().plusSeconds(expiresIn));

                userRepository.save(user); // guarda el nuevo token
            } else {
                throw new RuntimeException("Failed to refresh token: " + response.body());
            }
        }
    }

}
