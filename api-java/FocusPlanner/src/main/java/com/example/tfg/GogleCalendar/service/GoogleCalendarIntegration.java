package com.example.tfg.GogleCalendar.service;

import com.example.tfg.model.Task;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleCalendarIntegration {

    private static final String APPLICATION_NAME = "FocusPlanner";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String SERVICE_ACCOUNT_FILE = "src/main/resources/credentials.json"; // Ruta del JSON
    private static final String CALENDAR_ID = "focusplanner.welcome@gmail.com"; // Usar el correo correcto del calendario


    public Calendar getCalendarService(String accessToken) throws GeneralSecurityException, IOException {
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    // 📌 Crear un evento en Google Calendar cuando se añade una nueva tarea
    public String addEvent(Task task, String accessToken) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(accessToken);

        Event event = new Event()
                .setSummary(task.getTitle())
                .setDescription(task.getDescription())
                .setStart(new EventDateTime()
                        .setDateTime(new DateTime(task.getDueDate().toString() + "T09:00:00-00:00"))
                        .setTimeZone("UTC"))
                .setEnd(new EventDateTime()
                        .setDateTime(new DateTime(task.getDueDate().toString() + "T10:00:00-00:00"))
                        .setTimeZone("UTC"));

        event = service.events().insert("primary", event).execute(); // Usamos "primary" para el calendario del usuario
        return event.getId();
    }


    // 📌 Actualizar un evento en Google Calendar al modificar una tarea
    public void updateEvent(String eventId, Task task, String accessToken) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(accessToken);
        Event event = service.events().get("primary", eventId).execute();

        event.setSummary(task.getTitle());
        event.setDescription(task.getDescription());
        event.setStart(new EventDateTime()
                .setDateTime(new DateTime(task.getDueDate().toString() + "T09:00:00-00:00"))
                .setTimeZone("UTC"));
        event.setEnd(new EventDateTime()
                .setDateTime(new DateTime(task.getDueDate().toString() + "T10:00:00-00:00"))
                .setTimeZone("UTC"));

        service.events().update("primary", eventId, event).execute();
    }

    // 📌 Eliminar un evento en Google Calendar cuando se borra una tarea
    public void deleteEvent(String eventId, String accessToken) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(accessToken);
        service.events().delete("primary", eventId).execute();
    }

    // 📌 Obtener la lista de eventos en Google Calendar
    public void listUpcomingEvents(String accessToken) throws IOException, GeneralSecurityException {
        Calendar service = getCalendarService(accessToken);
        DateTime now = new DateTime(System.currentTimeMillis());

        var events = service.events().list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        if (events.getItems().isEmpty()) {
            System.out.println("No hay eventos próximos.");
        } else {
            events.getItems().forEach(event ->
                    System.out.println(event.getSummary() + " - " + event.getStart().getDateTime()));
        }
    }
}
