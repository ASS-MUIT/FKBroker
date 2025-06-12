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

import us.dit.fhirserver.service.entities.db.EventDB;
import us.dit.fhirserver.service.entities.db.SubscriptionDB;
import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;
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
     * {@link SubscriptionTopicDB} en una entidad {@link SubscriptionDB}
     * 
     * @param subscription        recurso FHIR {@link Subscription}.
     * @param SubscriptionTopicDB entidad {@link SubscriptionTopicDB}.
     * @return la entidad {@link SubscriptionDB}.
     */
    public SubscriptionDB toEntity(Subscription subscription, SubscriptionTopicDB SubscriptionTopicDB) {
        SubscriptionDB subscriptionDB = new SubscriptionDB();

        subscriptionDB.setEndpoint(subscription.getEndpoint());
        subscriptionDB.setHeartbeatPeriod(subscription.getHeartbeatPeriod());
        subscriptionDB.setStatus(SubscriptionStatusCodes.REQUESTED.toCode());
        subscriptionDB.setLastEvent(0);
        subscriptionDB.setTopic(SubscriptionTopicDB);

        return subscriptionDB;
    }

    /**
     * Transforma una entidad {@link SubscriptionDB} en un objeto del dominio
     * {@link SubscriptionDTO}.
     * 
     * @param eventDB entidad {@link SubscriptionDB}.
     * @return el objeto del dominio {@link SubscriptionDTO}.
     */
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

    /**
     * Transforma una entidad {@link SubscriptionDB} en un recurso FHIR
     * {@link Subscription}.
     * 
     * @param eventDB entidad {@link SubscriptionDB}.
     * @return el recurso FHIR {@link Subscription}.
     */
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

    /**
     * Transforma un listado de entidades {@link SubscriptionDB} en un recurso FHIR
     * {@link Bundle}.
     * 
     * @param subscriptionDBs listado de entidades {@link SubscriptionDB}.
     * @return el recurso FHIR {@link Bundle}.
     */
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

    /**
     * Transforma una entidad {@link SubscriptionDB} en un recurso FHIR
     * {@link Bundle} del tipo QUERYSTATUS.
     * 
     * @param subscriptionDB entidad {@link SubscriptionDB}.
     * @return el recurso FHIR {@link Bundle}.
     */
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

    /**
     * Transforma una entidad {@link SubscriptionDB} y un listado de entidades
     * {@link EventDB} en un recurso FHIR {@link Bundle} del tipo QUERYEVENT.
     * 
     * @param subscriptionDB entidad {@link SubscriptionDB}.
     * @param events         listado de entidades {@link EventDB}.
     * @return el recurso FHIR {@link Bundle}.
     */
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
}
