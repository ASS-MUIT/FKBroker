package us.dit.fhirserver.service.entities.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Objeto del dominio que representa un listado de eventos,
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
public class EventsDTO {

    private List<EventDTO> events;

    public EventsDTO() {
    }

    public EventsDTO(Long idSubscription, Long lastEvent, Long number) {
        events = new ArrayList<EventDTO>();

        for (int i = 0; i < number; i++) {
            EventDTO event = new EventDTO();
            event.setIdSubscription(idSubscription);
            event.setNumber(lastEvent + i + 1);
            events.add(event);
        }
    }

    public List<EventDTO> getEvents() {
        return events;
    }

    public void setEvents(List<EventDTO> events) {
        this.events = events;
    }

}
