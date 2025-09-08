package us.dit.fhirserver.service.services.mapper;

import java.util.List;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.Bundle.BundleType;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionNotificationType;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionStatusNotificationEventComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import us.dit.fhirserver.service.entities.db.Event;
import us.dit.fhirserver.service.entities.db.Subs;
import us.dit.fhirserver.service.entities.db.Topic;
import us.dit.fhirserver.service.entities.domain.EventDTO;

/**
 * Componente que transforma entidades, objetos del dominio y recursos FHIR
 * relacionados con los eventos.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Component
public class EventMapper {

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    /**
     * Transforma un objeto del dominio {@link EventDTO} en una entidad
     * {@link Event}
     * 
     * @param idSbuscription objeto del dominio {@link EventDTO}.
     * @param eventDTO       objeto del dominio {@link EventDTO}.
     * @return la entidad {@link Event}.
     */
    public Event toEntity(Long idSbuscription, EventDTO eventDTO) {
        Event event = new Event();

        event.setNumber(eventDTO.getNumber());
        event.setIdResource(eventDTO.getIdResource());
        event.setIdSubscription(idSbuscription);

        return event;
    }

    /**
     * Transforma una entidad {@link Event} en un objeto del dominio
     * {@link EventDTO}.
     * 
     * @param event entidad {@link Event}.
     * @return el objeto del dominio {@link EventDTO}.
     */
    public EventDTO toDTO(Event event) {
        EventDTO eventDTO = new EventDTO();

        eventDTO.setNumber(event.getNumber());
        eventDTO.setIdResource(event.getIdResource());

        return eventDTO;
    }

    /**
     * Crea un recurso FHIR {@link SubscriptionStatusNotificationEventComponent} a
     * partir de la información de una entidad {@link Event} y el recurso.
     * 
     * @param eventDB  entidad {@link Event}.
     * @param resource nombre del recurso notificado.
     * @return el recurso FHIR {@link SubscriptionStatusNotificationEventComponent}.
     */
    public SubscriptionStatusNotificationEventComponent toNotificationEvent(Event event, String resource) {
        SubscriptionStatusNotificationEventComponent notificationEvent = new SubscriptionStatusNotificationEventComponent();
        notificationEvent.setEventNumber(event.getNumber());
        String urlResource = fhirServerUrl + "/" + resource + "/" + event.getIdResource();
        Reference referenceResource = new Reference(urlResource);
        notificationEvent.setFocus(referenceResource);
        return notificationEvent;
    }

    /**
     * Crea un recurso FHIR {@link Bundle} a partir de la información de una
     * subscripción y un listado de eventos.
     * 
     * @param subs   entidad {@link Subs}.
     * @param events listado de entidades {@link Event}.
     * @return el recurso FHIR {@link Bundle}.
     */
    public Bundle toBundleNotification(Subs subs, List<Event> events) {
        Topic topic = subs.getTopic();

        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subs.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.EVENTNOTIFICATION);
        subscriptionStatus.setEventsSinceSubscriptionStart(subs.getLastEvent());
        String urlSubscription = fhirServerUrl + "/Subscription/" + subs.getId().toString();
        Reference reference = new Reference(urlSubscription);
        subscriptionStatus.setSubscription(reference);
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + topic.getId().toString();
        subscriptionStatus.setTopic(urlTopic);

        for (Event event : events) {
            SubscriptionStatusNotificationEventComponent notificationEvent = toNotificationEvent(event,
                    topic.getResource());
            subscriptionStatus.addNotificationEvent(notificationEvent);
        }

        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setResource(subscriptionStatus);
        bundle.addEntry(entry);

        return bundle;
    }

    /**
     * Transforma una entidad {@link Subs} en un recurso FHIR {@link Bundle} del
     * tipo HEARTBEAT.
     * 
     * @param subs entidad {@link Subs}.
     * @return el recurso FHIR {@link Bundle}.
     */
    public Bundle toBundleHeartbeat(Subs subs) {
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subs.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.HEARTBEAT);
        subscriptionStatus.setEventsSinceSubscriptionStart(subs.getLastEvent());
        String urlSubscription = fhirServerUrl + "/Subscription/" + subs.getId().toString();
        Reference reference = new Reference(urlSubscription);
        subscriptionStatus.setSubscription(reference);
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subs.getTopic().getId().toString();
        subscriptionStatus.setTopic(urlTopic);

        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setResource(subscriptionStatus);
        bundle.addEntry(entry);

        return bundle;
    }

    /**
     * Transforma una entidad {@link Subs} en un recurso FHIR {@link Bundle} del
     * tipo HANDSHAKE.
     * 
     * @param subs entidad {@link Subs}.
     * @return el recurso FHIR {@link Bundle}.
     */
    public Bundle toBundleHandshake(Subs subs) {
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subs.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.HANDSHAKE);
        subscriptionStatus.setEventsSinceSubscriptionStart(subs.getLastEvent());
        String urlSubscription = fhirServerUrl + "/Subscription/" + subs.getId().toString();
        Reference reference = new Reference(urlSubscription);
        subscriptionStatus.setSubscription(reference);
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subs.getTopic().getId().toString();
        subscriptionStatus.setTopic(urlTopic);

        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setResource(subscriptionStatus);
        bundle.addEntry(entry);

        return bundle;
    }
}
