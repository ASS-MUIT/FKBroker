package us.dit.fhirserver.service.services.mapper;

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

import us.dit.fhirserver.service.entities.db.Event;
import us.dit.fhirserver.service.entities.db.Subs;
import us.dit.fhirserver.service.entities.db.Topic;
import us.dit.fhirserver.service.entities.domain.SubscriptionDTO;

/**
 * Componente que transforma entidades, objetos del dominio y recursos FHIR
 * relacionados con las subscripciones.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Component
public class SubscriptionMapper {

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    private final EventMapper eventMapper;

    /**
     * Constructor que inyecta el componente {@link EventMapper}.
     * 
     * @param eventMapper componente que transforma entidades, objetos del dominio y
     *                    recursos FHIR relacionados con los eventos.
     */
    @Autowired
    public SubscriptionMapper(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    /**
     * Transforma un objeto un recurso FHIR {@link Subscription} y una entidad
     * {@link Topic} en una entidad {@link Subs}
     * 
     * @param subscription recurso FHIR {@link Subscription}.
     * @param topic        entidad {@link Topic}.
     * @return la entidad {@link Subs}.
     */
    public Subs toEntity(Subscription subscription, Topic topic) {
        Subs subs = new Subs();

        subs.setEndpoint(subscription.getEndpoint());
        subs.setHeartbeatPeriod(subscription.getHeartbeatPeriod());
        subs.setStatus(SubscriptionStatusCodes.REQUESTED.toCode());
        subs.setLastEvent(0);
        subs.setTopic(topic);

        return subs;
    }

    /**
     * Transforma una entidad {@link Subs} en un objeto del dominio
     * {@link SubscriptionDTO}.
     * 
     * @param subs entidad {@link Subs}.
     * @return el objeto del dominio {@link SubscriptionDTO}.
     */
    public SubscriptionDTO toDTO(Subs subs) {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();

        subscriptionDTO.setId(subs.getId());
        subscriptionDTO.setEndpoint(subs.getEndpoint());
        subscriptionDTO.setHeartbeatPeriod(subs.getHeartbeatPeriod());
        subscriptionDTO.setIdTopic(subs.getTopic().getId());
        subscriptionDTO.setLastEvent(subs.getLastEvent());
        subscriptionDTO.setStatus(subs.getStatus());

        return subscriptionDTO;
    }

    /**
     * Transforma una entidad {@link Subs} en un recurso FHIR {@link Subscription}.
     * 
     * @param subs entidad {@link Subs}.
     * @return el recurso FHIR {@link Subscription}.
     */
    public Subscription toSubscription(Subs subs) {
        Subscription subscription = new Subscription();

        // Valores guardados en Base de datos
        subscription.setId(subs.getId().toString());
        subscription.setStatus(SubscriptionStatusCodes.fromCode(subs.getStatus()));
        subscription.setEndpoint(subs.getEndpoint());
        subscription.setHeartbeatPeriod(subs.getHeartbeatPeriod());
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subs.getTopic().getId().toString();
        subscription.setTopic(urlTopic);

        // Valores simulados
        Coding coding = new Coding();
        coding.setCode("rest-hook");
        subscription.setChannelType(coding);
        subscription.setContent(SubscriptionPayloadContent.IDONLY);
        subscription.setContentType("application/fhir+json");

        return subscription;
    }

    /**
     * Transforma un listado de entidades {@link Subs} en un recurso FHIR
     * {@link Bundle}.
     * 
     * @param subscriptions listado de entidades {@link Subs}.
     * @return el recurso FHIR {@link Bundle}.
     */
    public Bundle toBundleSubscription(List<Subs> subscriptions) {
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SEARCHSET);

        for (Subs subs : subscriptions) {
            Subscription subscription = toSubscription(subs);

            BundleEntryComponent entry = new BundleEntryComponent();
            entry.setResource(subscription);
            bundle.addEntry(entry);
        }

        return bundle;
    }

    /**
     * Transforma una entidad {@link Subs} en un recurso FHIR {@link Bundle} del
     * tipo QUERYSTATUS.
     * 
     * @param subs entidad {@link Subs}.
     * @return el recurso FHIR {@link Bundle}.
     */
    public Bundle toBundleStatus(Subs subs) {
        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subs.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.QUERYSTATUS);
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
     * Transforma una entidad {@link Subs} y un listado de entidades {@link Event}
     * en un recurso FHIR {@link Bundle} del tipo QUERYEVENT.
     * 
     * @param subs   entidad {@link Subs}.
     * @param events listado de entidades {@link Event}.
     * @return el recurso FHIR {@link Bundle}.
     */
    public Bundle toBundleEvents(Subs subs, List<Event> events) {
        Topic subscriptionTopicDB = subs.getTopic();

        Bundle bundle = new Bundle();
        bundle.setType(BundleType.SUBSCRIPTIONNOTIFICATION);

        SubscriptionStatus subscriptionStatus = new SubscriptionStatus();
        subscriptionStatus.setStatus(SubscriptionStatusCodes.fromCode(subs.getStatus()));
        subscriptionStatus.setType(SubscriptionNotificationType.QUERYEVENT);
        subscriptionStatus.setEventsSinceSubscriptionStart(subs.getLastEvent());
        String urlSubscription = fhirServerUrl + "/Subscription/" + subs.getId().toString();
        Reference reference = new Reference(urlSubscription);
        subscriptionStatus.setSubscription(reference);
        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subscriptionTopicDB.getId().toString();
        subscriptionStatus.setTopic(urlTopic);

        for (Event event : events) {
            SubscriptionStatusNotificationEventComponent notificationEvent = eventMapper.toNotificationEvent(event,
                    subscriptionTopicDB.getResource());
            subscriptionStatus.addNotificationEvent(notificationEvent);
        }

        BundleEntryComponent entry = new BundleEntryComponent();
        entry.setResource(subscriptionStatus);
        bundle.addEntry(entry);

        return bundle;
    }
}
