package us.dit.fhirserver.service.services.fhir;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.uhn.fhir.context.FhirContext;
import us.dit.fhirserver.service.entities.db.EventDB;
import us.dit.fhirserver.service.entities.db.SubscriptionDB;
import us.dit.fhirserver.service.entities.domain.EventDTO;
import us.dit.fhirserver.service.repositories.EventRepository;
import us.dit.fhirserver.service.repositories.SubscriptionRepository;
import us.dit.fhirserver.service.services.RestClient;
import us.dit.fhirserver.service.services.mapper.EventMapper;

/**
 * Servicio para gestionar las operaciones sobre los eventos.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Service
public class EventService {

    private static final Logger logger = LogManager.getLogger();

    private final FhirContext fhirContext;
    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EventMapper eventMapper;
    private final RestClient restClient;

    /**
     * Constructor que inyecta los repositorios {@link EventRepository} y
     * {@link SubscriptionRepository}, los componentes {@link EventMapper} y
     * {@link FhirContext} y el servicio {@link RestClient}.
     * 
     * @param fhirContext            componente que contiene el contexto FHIR.
     * @param eventRepository        repositorio JPA de la entidad {@link EventDB}.
     * @param subscriptionRepository repositorio JPA de la entidad
     *                               {@link SubscriptionDB}.
     * @param eventMapper            componente que transforma entidades, objetos
     *                               del dominio y recursos FHIR relacionados con
     *                               los eventos.
     * @param restClient             servicio que gestiona las operaciones REST.
     */
    @Autowired
    public EventService(FhirContext fhirContext, EventRepository eventRepository,
            SubscriptionRepository subscriptionRepository, EventMapper eventMapper, RestClient restClient) {
        this.fhirContext = fhirContext;
        this.eventRepository = eventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.eventMapper = eventMapper;
        this.restClient = restClient;
    }

    /**
     * Obtiene todos los eventos de una subscripción.
     * 
     * @param idSubscription identificador de la subscripción.
     * @return el listado con los datos de todos los eventos de la subscripción.
     */
    public List<EventDTO> getEventsDTO(Long idSubscription) {
        List<EventDB> eventDBs = eventRepository.findByIdSubscriptionOrderByIdEvent(idSubscription);

        List<EventDTO> eventDTOs = eventDBs.stream().map(eventMapper::toDTO).collect(Collectors.toList());

        return eventDTOs;
    }

    /**
     * Crea y envía una notificación con los datos de todos los eventos facilitados
     * al endpoint configurado en una subscripción.
     * 
     * @param idSubscription identificador de la subscripción.
     * @param eventDTOs      listado de eventos a enviar.
     */
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

    /**
     * Envía un mensaje de handshake al endpoint configurado en una subscripción
     * para comprobar que la conexión sea correcta, cambiando el estado de la
     * subscripción a ACTIVE o ERROR en función de la respuesta.
     * 
     * @param idSubscription identificador de la subscripción.
     * @return true, en el caso de que la conexión sea satisfactoria, o false, en el
     *         caso contrario.
     */
    @Transactional
    public void sendHandshake(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);
        // Genera el mensaje
        Bundle bundle = eventMapper.toBundleHandshake(subscriptionDB);
        String message = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

        // Notifica al cliente
        if (restClient.sendMessage(subscriptionDB.getEndpoint(), message)) {
            logger.info("Handshake satisfactorio para la subscripción con ID: {}", idSubscription);
            subscriptionDB.setStatus(SubscriptionStatusCodes.ACTIVE.toCode());
        } else {
            logger.info("Handshake erroneo para la subscripción con ID: {}", idSubscription);
            subscriptionDB.setStatus(SubscriptionStatusCodes.ERROR.toCode());
        }

        subscriptionRepository.save(subscriptionDB);
    }

    /**
     * Envía un mensaje de heartbeat al endpoint configurado en una subscripción
     * para comprobar que la conexión sea correcta, cambiando el estado de la
     * subscripción a ACTIVE o ERROR en función de la respuesta.
     * 
     * @param idSubscription identificador de la subscripción.
     * @return true, en el caso de que la conexión sea satisfactoria, o false, en el
     *         caso contrario.
     */
    @Transactional
    public void sendHeartbeat(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);

        // Genera el mensaje de notificación
        Bundle bundle = eventMapper.toBundleHeartbeat(subscriptionDB);
        String message = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

        // Notifica al cliente
        if (restClient.sendMessage(subscriptionDB.getEndpoint(), message)) {
            logger.info("Heartbeat satisfactorio para la subscripción con ID: {}", idSubscription);
            subscriptionDB.setStatus(SubscriptionStatusCodes.ACTIVE.toCode());
        } else {
            logger.info("Heartbeat erroneo para la subscripción con ID: {}", idSubscription);
            subscriptionDB.setStatus(SubscriptionStatusCodes.ERROR.toCode());
        }

        subscriptionRepository.save(subscriptionDB);
    }

}
