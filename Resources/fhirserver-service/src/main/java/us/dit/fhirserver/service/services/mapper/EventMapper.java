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

import us.dit.fhirserver.service.entities.db.EventDB;
import us.dit.fhirserver.service.entities.db.SubscriptionDB;
import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;
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
     * {@link EventDB}
     * 
     * @param eventDTO objeto del dominio {@link EventDTO}.
     * @return la entidad {@link EventDB}.
     */
    public EventDB toEntity(EventDTO eventDTO) {
        EventDB eventDB = new EventDB();

        eventDB.setId(eventDTO.getId());
        eventDB.setNumber(eventDTO.getNumber());
        eventDB.setIdResource(eventDTO.getIdResource());
        eventDB.setIdSubscription(eventDTO.getIdSubscription());

        return eventDB;
    }

    /**
     * Transforma una entidad {@link EventDB} en un objeto del dominio
     * {@link EventDTO}.
     * 
     * @param eventDB entidad {@link EventDB}.
     * @return el objeto del dominio {@link EventDTO}.
     */
    public EventDTO toDTO(EventDB eventDB) {
        EventDTO eventDTO = new EventDTO();

        eventDTO.setId(eventDB.getId());
        eventDTO.setNumber(eventDB.getNumber());
        eventDTO.setIdResource(eventDB.getIdResource());
        eventDTO.setIdSubscription(eventDB.getIdSubscription());

        return eventDTO;
    }

    /**
     * Crea un recurso FHIR {@link SubscriptionStatusNotificationEventComponent} a
     * partir de la información de una entidad {@link EventDB} y el recurso.
     * 
     * @param eventDB  entidad {@link EventDB}.
     * @param resource nombre del recurso notificado.
     * @return el recurso FHIR {@link SubscriptionStatusNotificationEventComponent}.
     */
    public SubscriptionStatusNotificationEventComponent toNotificationEvent(EventDB event, String resource) {
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
     * @param subscriptionDB entidad {@link SubscriptionDB}.
     * @param events         listado de entidades {@link EventDB}.
     * @return el recurso FHIR {@link Bundle}.
     */
    public Bundle toBundleNotification(SubscriptionDB subscriptionDB, List<EventDB> events) {
        SubscriptionTopicDB subscriptionTopicDB = subscriptionDB.getTopic();

        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subscriptionDB.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.EVENTNOTIFICATION);
        subscriptionStatus.setEventsSinceSubscriptionStart(subscriptionDB.getLastEvent());
        String urlSubscription = fhirServerUrl + "/Subscription/" + subscriptionDB.getId().toString();
        Reference reference = new Reference(urlSubscription);
        subscriptionStatus.setSubscription(reference);
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subscriptionTopicDB.getId().toString();
        subscriptionStatus.setTopic(urlTopic);

        for (EventDB event : events) {
            SubscriptionStatusNotificationEventComponent notificationEvent = toNotificationEvent(event,
                    subscriptionTopicDB.getResource());
            subscriptionStatus.addNotificationEvent(notificationEvent);
        }

        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setResource(subscriptionStatus);
        bundle.addEntry(entry);

        return bundle;
    }

    /**
     * Transforma una entidad {@link SubscriptionDB} en un recurso FHIR
     * {@link Bundle} del tipo HEARTBEAT.
     * 
     * @param subscriptionDB entidad {@link SubscriptionDB}.
     * @return el recurso FHIR {@link Bundle}.
     */
    public Bundle toBundleHeartbeat(SubscriptionDB subscriptionDB) {
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subscriptionDB.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.HEARTBEAT);
        subscriptionStatus.setEventsSinceSubscriptionStart(subscriptionDB.getLastEvent());
        String urlSubscription = fhirServerUrl + "/Subscription/" + subscriptionDB.getId().toString();
        Reference reference = new Reference(urlSubscription);
        subscriptionStatus.setSubscription(reference);
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subscriptionDB.getTopic().getId().toString();
        subscriptionStatus.setTopic(urlTopic);

        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setResource(subscriptionStatus);
        bundle.addEntry(entry);

        return bundle;
    }

    /**
     * Transforma una entidad {@link SubscriptionDB} en un recurso FHIR
     * {@link Bundle} del tipo HANDSHAKE.
     * 
     * @param subscriptionDB entidad {@link SubscriptionDB}.
     * @return el recurso FHIR {@link Bundle}.
     */
    public Bundle toBundleHandshake(SubscriptionDB subscriptionDB) {
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subscriptionDB.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.HANDSHAKE);
        subscriptionStatus.setEventsSinceSubscriptionStart(subscriptionDB.getLastEvent());
        String urlSubscription = fhirServerUrl + "/Subscription/" + subscriptionDB.getId().toString();
        Reference reference = new Reference(urlSubscription);
        subscriptionStatus.setSubscription(reference);
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subscriptionDB.getTopic().getId().toString();
        subscriptionStatus.setTopic(urlTopic);

        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setResource(subscriptionStatus);
        bundle.addEntry(entry);

        return bundle;
    }
}
