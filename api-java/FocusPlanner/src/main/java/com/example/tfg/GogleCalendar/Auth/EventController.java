//package com.example.tfg.GogleCalendar.Auth;
//
//import com.example.tfg.GogleCalendar.Auth.Permisos.GoogleCalendarService;
//import com.google.api.services.calendar.model.Event;
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//
//public class EventController {
//    private final GoogleCalendarService googleCalendarService;
//
//    public EventController(GoogleCalendarService googleCalendarService) {
//        this.googleCalendarService = googleCalendarService;
//    }
//
//    public void addEventToGoogleCalendar(String summary, String description, String startDateTime, String endDateTime) throws GeneralSecurityException, IOException {
//        // Llamada al método createEvent para agregar el evento a Google Calendar
////        Event createdEvent = googleCalendarService.createEvent(summary, description, startDateTime, endDateTime);
//
//        // Aquí puedes hacer algo con el evento creado, por ejemplo, mostrar un mensaje de éxito
////        System.out.println("Evento creado: " + createdEvent.getHtmlLink());
//    }
//}
