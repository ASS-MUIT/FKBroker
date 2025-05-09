package us.dit.fhirserver.service.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.fhir.context.FhirContext;
import us.dit.fhirserver.service.entities.db.EventDB;
import us.dit.fhirserver.service.entities.db.SubscriptionDB;
import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;
import us.dit.fhirserver.service.entities.domain.SubscriptionDTO;
import us.dit.fhirserver.service.repositories.EventRepository;
import us.dit.fhirserver.service.repositories.SubscriptionRepository;
import us.dit.fhirserver.service.repositories.SubscriptionTopicRepository;

@Service
public class SubscriptionService {

    private final FhirContext fhirContext;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionTopicRepository subscriptionTopicRepository;
    private final EventRepository eventRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionSchedulerManager subscriptionschedulerManager;
    private final RestClient restClient;

    @Autowired
    public SubscriptionService(FhirContext fhirContext, SubscriptionRepository subscriptionRepository,
            SubscriptionTopicRepository subscriptionTopicRepository, EventRepository eventRepository,
            SubscriptionMapper subscriptionMapper, SubscriptionSchedulerManager subscriptionschedulerManager,
            RestClient restClient) {
        this.fhirContext = fhirContext;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionTopicRepository = subscriptionTopicRepository;
        this.eventRepository = eventRepository;
        this.subscriptionMapper = subscriptionMapper;
        this.subscriptionschedulerManager = subscriptionschedulerManager;
        this.restClient = restClient;
    }

    public String saveSubscription(String message) {
        Subscription subscription = fhirContext.newJsonParser().parseResource(Subscription.class, message);

        String[] parts = subscription.getTopic().split("/");
        Long idSubscriptionTopic = Long.valueOf(parts[parts.length - 1]);
        SubscriptionTopicDB subscriptionTopicDB = subscriptionTopicRepository.getById(idSubscriptionTopic);

        SubscriptionDB subscriptionDB = subscriptionMapper.toEntity(subscription, subscriptionTopicDB);
        subscriptionDB = subscriptionRepository.save(subscriptionDB);

        Long idSub = subscriptionDB.getId();
        Integer heartbeat = subscriptionDB.getHeartbeatPeriod();

        // Comprueba la conexión con el endpoint y configura los heartbeats
        CompletableFuture.runAsync(() -> {
            // Comprueba la conexión con el endpoint
            sendHandshake(idSub);

            // Configura los heartbeats
            subscriptionschedulerManager.iniciarTarea(idSub, heartbeat * 1000, () -> sendHeartbeat(idSub));
        });

        subscription = subscriptionMapper.toSubscription(subscriptionDB);
        return fhirContext.newJsonParser().encodeResourceToString(subscription);
    }

    public String getSubscription(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);

        Subscription subscription = subscriptionMapper.toSubscription(subscriptionDB);

        return fhirContext.newJsonParser().encodeResourceToString(subscription);
    }

    public String getSubscriptions() {
        List<SubscriptionDB> subscriptionDBs = subscriptionRepository.findAll();

        Bundle bundle = subscriptionMapper.toBundleSubscription(subscriptionDBs);

        return fhirContext.newJsonParser().encodeResourceToString(bundle);
    }

    public String deleteSubscription(Long idSubscription) {
        subscriptionRepository.deleteById(idSubscription);
        subscriptionschedulerManager.cancelarTarea(idSubscription);

        OperationOutcome response = new OperationOutcome();

        return fhirContext.newJsonParser().encodeResourceToString(response);
    }

    public String getStatus(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);

        Bundle bundle = subscriptionMapper.toBundleStatus(subscriptionDB);

        return fhirContext.newJsonParser().encodeResourceToString(bundle);
    }

    public String getEvents(Long idSubscription, Long eventsSinceNumber, Long eventsUntilNumber) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);
        List<EventDB> events = eventRepository.findByIdSubscriptionAndIdEventBetweenOrderByIdEvent(
                subscriptionDB.getId(), eventsSinceNumber, eventsUntilNumber);

        Bundle bundle = subscriptionMapper.toBundleEvents(subscriptionDB, events);

        return fhirContext.newJsonParser().encodeResourceToString(bundle);
    }

    public List<SubscriptionDTO> getSubscriptionsDTO() {
        List<SubscriptionDB> subscriptionDBs = subscriptionRepository.findAll();

        List<SubscriptionDTO> subscriptionDTOs = subscriptionDBs.stream().map(subscriptionMapper::toDTO)
                .collect(Collectors.toList());

        return subscriptionDTOs;
    }

    public SubscriptionDTO getSubscriptionDTO(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);

        return subscriptionMapper.toDTO(subscriptionDB);
    }

    public Boolean sendHandshake(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);

        // Genera el mensaje
        Bundle bundle = subscriptionMapper.toBundleHandshake(subscriptionDB);
        String message = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

        // Notifica al cliente
        return restClient.sendMessage(subscriptionDB.getEndpoint(), message);
    }

    @Transactional
    public void sendHeartbeat(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);
        
        // Genera el mensaje de notificación
        Bundle bundle = subscriptionMapper.toBundleHeartbeat(subscriptionDB);
        String message = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);
        
        // Notifica al cliente
        if (restClient.sendMessage(subscriptionDB.getEndpoint(), message)) {
            subscriptionDB.setStatus(SubscriptionStatusCodes.ACTIVE.toCode());
        } else {
            subscriptionDB.setStatus(SubscriptionStatusCodes.ERROR.toCode());
        }

        subscriptionRepository.save(subscriptionDB);
    }
}
