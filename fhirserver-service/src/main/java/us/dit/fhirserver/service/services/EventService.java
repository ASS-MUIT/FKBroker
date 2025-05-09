package us.dit.fhirserver.service.services;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import us.dit.fhirserver.service.entities.db.EventDB;
import us.dit.fhirserver.service.entities.db.SubscriptionDB;
import us.dit.fhirserver.service.entities.domain.EventDTO;
import us.dit.fhirserver.service.repositories.EventRepository;
import us.dit.fhirserver.service.repositories.SubscriptionRepository;

@Service
public class EventService {

    private final FhirContext fhirContext;
    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EventMapper eventMapper;
    private final RestClient restClient;

    @Autowired
    public EventService(FhirContext fhirContext, EventRepository eventRepository,
            SubscriptionRepository subscriptionRepository, EventMapper eventMapper, RestClient restClient) {
        this.fhirContext = fhirContext;
        this.eventRepository = eventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.eventMapper = eventMapper;
        this.restClient = restClient;
    }

    public List<EventDTO> getEventsDTO(Long idSubscription) {
        List<EventDB> eventDBs = eventRepository.findByIdSubscriptionOrderByIdEvent(idSubscription);

        List<EventDTO> eventDTOs = eventDBs.stream().map(eventMapper::toDTO).collect(Collectors.toList());

        return eventDTOs;
    }

    public void sendEvents(Long idSubscription, List<EventDTO> eventDTOs) {
        // Obtiene la información
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);
        subscriptionDB.setLastEvent(subscriptionDB.getLastEvent() + eventDTOs.size());
        List<EventDB> eventDBs = eventDTOs.stream().map(eventMapper::toEntity).collect(Collectors.toList());

        // Genera el mensaje de notificación
        Bundle bundle = eventMapper.toBundleNotification(subscriptionDB, eventDBs);
        String message = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

        // Notifica al cliente
        Boolean send = restClient.sendMessage(subscriptionDB.getEndpoint(), message);

        if (send) {
            subscriptionDB.setStatus(SubscriptionStatusCodes.ACTIVE.toCode());
        } else {
            subscriptionDB.setStatus(SubscriptionStatusCodes.ERROR.toCode());
        }

        // Guarda la información en BBDD
        subscriptionRepository.save(subscriptionDB);
        eventRepository.saveAll(eventDBs);
    }

}
