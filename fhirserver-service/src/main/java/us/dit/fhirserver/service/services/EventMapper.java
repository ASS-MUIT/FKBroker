package us.dit.fhirserver.service.services;

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

@Component
public class EventMapper {

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    public EventDB toEntity(EventDTO eventDTO) {
        EventDB eventDB = new EventDB();

        eventDB.setId(eventDTO.getId());
        eventDB.setIdEvent(eventDTO.getIdEvent());
        eventDB.setIdResource(eventDTO.getIdResource());
        eventDB.setIdSubscription(eventDTO.getIdSubscription());

        return eventDB;
    }

    public EventDTO toDTO(EventDB eventDB) {
        EventDTO eventDTO = new EventDTO();

        eventDTO.setId(eventDB.getId());
        eventDTO.setIdEvent(eventDB.getIdEvent());
        eventDTO.setIdResource(eventDB.getIdResource());
        eventDTO.setIdSubscription(eventDB.getIdSubscription());

        return eventDTO;
    }

    public SubscriptionStatusNotificationEventComponent toNotificationEvent(EventDB event, String resource) {
        SubscriptionStatusNotificationEventComponent notificationEvent = new SubscriptionStatusNotificationEventComponent();
        notificationEvent.setEventNumber(event.getIdEvent());
        String urlResource = fhirServerUrl + "/" + resource + "/" + event.getIdResource();
        Reference referenceResource = new Reference(urlResource);
        notificationEvent.setFocus(referenceResource);
        return notificationEvent;
    }

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
}
