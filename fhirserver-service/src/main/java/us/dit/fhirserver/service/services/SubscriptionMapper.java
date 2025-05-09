package us.dit.fhirserver.service.services;

import java.util.List;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.Bundle.BundleType;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.hl7.fhir.r5.model.Reference;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.Subscription.SubscriptionPayloadContent;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionNotificationType;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionStatusNotificationEventComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import us.dit.fhirserver.service.entities.db.EventDB;
import us.dit.fhirserver.service.entities.db.SubscriptionDB;
import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;
import us.dit.fhirserver.service.entities.domain.SubscriptionDTO;

@Component
public class SubscriptionMapper {

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    private final EventMapper eventMapper;

    @Autowired
    public SubscriptionMapper(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    public SubscriptionDB toEntity(Subscription subscription, SubscriptionTopicDB SubscriptionTopicDB) {
        SubscriptionDB subscriptionDB = new SubscriptionDB();

        subscriptionDB.setEndpoint(subscription.getEndpoint());
        subscriptionDB.setHeartbeatPeriod(subscription.getHeartbeatPeriod());
        subscriptionDB.setStatus(SubscriptionStatusCodes.REQUESTED.toCode());
        subscriptionDB.setLastEvent(0);
        subscriptionDB.setTopic(SubscriptionTopicDB);

        return subscriptionDB;
    }

    public Subscription toSubscription(SubscriptionDB subscriptionDB) {
        Subscription subscription = new Subscription();

        // Valores guardados en Base de datos
        subscription.setId(subscriptionDB.getId().toString());
        subscription.setStatus(SubscriptionStatusCodes.fromCode(subscriptionDB.getStatus()));
        subscription.setEndpoint(subscriptionDB.getEndpoint());
        subscription.setHeartbeatPeriod(subscriptionDB.getHeartbeatPeriod());
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subscriptionDB.getTopic().getId().toString();
        subscription.setTopic(urlTopic);

        // Valores simulados
        Coding coding = new Coding();
        coding.setCode("rest-hook");
        subscription.setChannelType(coding);
        subscription.setContent(SubscriptionPayloadContent.IDONLY);
        subscription.setContentType("application/fhir+json");

        return subscription;
    }

    public Bundle toBundleSubscription(List<SubscriptionDB> subscriptionDBs) {
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SEARCHSET);

        for (SubscriptionDB subscriptionDB : subscriptionDBs) {
            Subscription subscription = toSubscription(subscriptionDB);

            BundleEntryComponent entry = new BundleEntryComponent();
            entry.setResource(subscription);
            bundle.addEntry(entry);
        }

        return bundle;
    }

    public Bundle toBundleStatus(SubscriptionDB subscriptionDB) {
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subscriptionDB.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.QUERYSTATUS);
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

    public Bundle toBundleEvents(SubscriptionDB subscriptionDB, List<EventDB> events) {
        SubscriptionTopicDB subscriptionTopicDB = subscriptionDB.getTopic();

        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subscriptionDB.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.QUERYEVENT);
        subscriptionStatus.setEventsSinceSubscriptionStart(subscriptionDB.getLastEvent());
        String urlSubscription = fhirServerUrl + "/Subscription/" + subscriptionDB.getId().toString();
        Reference reference = new Reference(urlSubscription);
        subscriptionStatus.setSubscription(reference);
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subscriptionTopicDB.getId().toString();
        subscriptionStatus.setTopic(urlTopic);

        for (EventDB event : events) {
            SubscriptionStatusNotificationEventComponent notificationEvent = eventMapper.toNotificationEvent(event,
                    subscriptionTopicDB.getResource());
            subscriptionStatus.addNotificationEvent(notificationEvent);
        }

        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setResource(subscriptionStatus);
        bundle.addEntry(entry);

        return bundle;
    }

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
    
    public SubscriptionDTO toDTO(SubscriptionDB subscriptionDB) {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        
        subscriptionDTO.setId(subscriptionDB.getId());
        subscriptionDTO.setEndpoint(subscriptionDB.getEndpoint());
        subscriptionDTO.setHeartbeatPeriod(subscriptionDB.getHeartbeatPeriod());
        subscriptionDTO.setIdTopic(subscriptionDB.getTopic().getId());
        subscriptionDTO.setLastEvent(subscriptionDB.getLastEvent());
        subscriptionDTO.setStatus(subscriptionDB.getStatus());
        
        return subscriptionDTO;
    }
}
