package com.example.interfaces.api;

import com.example.application.event.EventService;
import com.example.domain.event.Event;
import com.example.interfaces.dto.EventDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDto.Response> createEvent(@Valid @RequestBody EventDto.Request request) {
        Event event = request.toEntity();
        Event savedEvent = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EventDto.Response.fromEntity(savedEvent));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto.Response> getEvent(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(event -> ResponseEntity.ok(EventDto.Response.fromEntity(event)))
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다. ID: " + id));
    }

    @GetMapping
    public ResponseEntity<List<EventDto.Response>> getAllEvents() {
        List<EventDto.Response> events = eventService.getAllEvents().stream()
                .map(EventDto.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDto.Response> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventDto.Request request) {
        Event event = request.toEntity();
        Event updatedEvent = eventService.updateEvent(id, event);
        return ResponseEntity.ok(EventDto.Response.fromEntity(updatedEvent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<EventDto.Response>> getActiveEvents() {
        List<EventDto.Response> events = eventService.getActiveEvents().stream()
                .map(EventDto.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/registrable")
    public ResponseEntity<List<EventDto.Response>> getRegistrableEvents() {
        List<EventDto.Response> events = eventService.getRegistrableEvents().stream()
                .map(EventDto.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventDto.Response>> getUpcomingEvents() {
        List<EventDto.Response> events = eventService.getUpcomingEvents(LocalDateTime.now()).stream()
                .map(EventDto.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/past")
    public ResponseEntity<List<EventDto.Response>> getPastEvents() {
        List<EventDto.Response> events = eventService.getPastEvents(LocalDateTime.now()).stream()
                .map(EventDto.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventDto.Response>> searchEvents(@RequestParam String keyword) {
        List<EventDto.Response> events = eventService.searchEvents(keyword).stream()
                .map(EventDto.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<EventDto.Response>> getEventsByLocation(@PathVariable String location) {
        List<EventDto.Response> events = eventService.getEventsByLocation(location).stream()
                .map(EventDto.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @GetMapping("/organizer/{organizer}")
    public ResponseEntity<List<EventDto.Response>> getEventsByOrganizer(@PathVariable String organizer) {
        List<EventDto.Response> events = eventService.getEventsByOrganizer(organizer).stream()
                .map(EventDto.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<EventDto.Response> registerParticipant(@PathVariable Long id) {
        try {
            Event event = eventService.registerParticipant(id);
            return ResponseEntity.ok(EventDto.Response.fromEntity(event));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<EventDto.Response> cancelRegistration(@PathVariable Long id) {
        Event event = eventService.cancelRegistration(id);
        return ResponseEntity.ok(EventDto.Response.fromEntity(event));
    }
}
