package com.example.tfg.GogleCalendar.Auth.Permisos;

import com.google.api.services.calendar.model.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
    private final GoogleCalendarService googleCalendarService;

    public CalendarController(GoogleCalendarService googleCalendarService) {
        this.googleCalendarService = googleCalendarService;
    }

    @GetMapping("/events")
    public List<Event> getEvents() throws GeneralSecurityException, IOException {
//        return googleCalendarService.getUpcomingEvents();
        return List.of();
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(@RequestBody Map<String, String> request) {
//        try {
//            Event event = googleCalendarService.createEvent(
//                    request.get("summary"),
//                    request.get("description"),
//                    request.get("startDateTime"),
//                    request.get("endDateTime")
//            );
//            return ResponseEntity.ok(event);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
        return null;
    }
}
