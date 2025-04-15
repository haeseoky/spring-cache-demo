package com.example.application.event;

import com.example.domain.event.Event;
import com.example.domain.event.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    @Transactional
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event updateEvent(Long id, Event eventDetails) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다. ID: " + id));

        // 이벤트 정보 업데이트
        event.setTitle(eventDetails.getTitle());
        event.setDescription(eventDetails.getDescription());
        event.setStartDate(eventDetails.getStartDate());
        event.setEndDate(eventDetails.getEndDate());
        event.setLocation(eventDetails.getLocation());
        event.setOrganizer(eventDetails.getOrganizer());
        event.setMaxParticipants(eventDetails.getMaxParticipants());
        event.setIsActive(eventDetails.getIsActive());

        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EntityNotFoundException("이벤트를 찾을 수 없습니다. ID: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getActiveEvents() {
        return eventRepository.findByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getRegistrableEvents() {
        return eventRepository.findRegistrableEvents();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getUpcomingEvents(LocalDateTime fromDate) {
        return eventRepository.findByStartDateAfterOrderByStartDateAsc(fromDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getPastEvents(LocalDateTime toDate) {
        return eventRepository.findByEndDateBeforeOrderByEndDateDesc(toDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> searchEvents(String keyword) {
        return eventRepository.findByTitleContainingOrDescriptionContainingOrderByStartDateAsc(keyword, keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByLocation(String location) {
        return eventRepository.findByLocationContainingOrderByStartDateAsc(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsByOrganizer(String organizer) {
        return eventRepository.findByOrganizerContainingOrderByStartDateAsc(organizer);
    }

    @Override
    @Transactional
    public Event registerParticipant(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다. ID: " + id));

        if (!event.isRegistrationAvailable()) {
            throw new IllegalStateException("이 이벤트는 현재 등록이 불가능합니다.");
        }

        event.incrementParticipants();
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event cancelRegistration(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("이벤트를 찾을 수 없습니다. ID: " + id));

        event.decrementParticipants();
        return eventRepository.save(event);
    }
}
