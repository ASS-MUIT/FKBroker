package us.dit.fhirserver.service.services.fhir;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import us.dit.fhirserver.service.entities.db.EventDB;
import us.dit.fhirserver.service.entities.db.SubscriptionDB;
import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;
import us.dit.fhirserver.service.entities.domain.SubscriptionDTO;
import us.dit.fhirserver.service.repositories.EventRepository;
import us.dit.fhirserver.service.repositories.SubscriptionRepository;
import us.dit.fhirserver.service.repositories.SubscriptionTopicRepository;
import us.dit.fhirserver.service.services.RestClient;
import us.dit.fhirserver.service.services.mapper.SubscriptionMapper;

/**
 * Servicio para gestionar las operaciones sobre las subscripciones FHIR.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Service
public class SubscriptionService {

    private static final Logger logger = LogManager.getLogger();

    private final FhirContext fhirContext;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionTopicRepository subscriptionTopicRepository;
    private final EventRepository eventRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionSchedulerManager subscriptionschedulerManager;
    private final EventService eventService;

    /**
     * Constructor que inyecta los repositorios {@link SubscriptionRepository},
     * {@link SubscriptionTopicRepository} y {@link EventRepository}, los
     * componentes {@link SubscriptionMapper}, {@link SubscriptionSchedulerManager}
     * y {@link FhirContext} y el servicio {@link RestClient}.
     * 
     * @param fhirContext                  componente que contiene el contexto FHIR.
     * @param subscriptionRepository       repositorio JPA de la entidad
     *                                     {@link SubscriptionDB}.
     * @param subscriptionTopicRepository  repositorio JPA de la entidad
     *                                     {@link SubscriptionTopicDB}.
     * @param eventRepository              repositorio JPA de la entidad
     *                                     {@link EventDB}.
     * @param SubscriptionMapper           componente que transforma entidades,
     *                                     objetos del dominio y recursos FHIR
     *                                     relacionados con las subscripciones.
     * @param subscriptionschedulerManager componente que programa las tareas
     *                                     periodicas realizadas sobre
     *                                     subscripciones.
     * @param restClient                   servicio que gestiona las operaciones
     *                                     REST.
     */
    @Autowired
    public SubscriptionService(FhirContext fhirContext, SubscriptionRepository subscriptionRepository,
            SubscriptionTopicRepository subscriptionTopicRepository, EventRepository eventRepository,
            SubscriptionMapper subscriptionMapper, SubscriptionSchedulerManager subscriptionschedulerManager,
            EventService eventService) {
        this.fhirContext = fhirContext;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionTopicRepository = subscriptionTopicRepository;
        this.eventRepository = eventRepository;
        this.subscriptionMapper = subscriptionMapper;
        this.subscriptionschedulerManager = subscriptionschedulerManager;
        this.eventService = eventService;
    }

    /**
     * Crea una nueva subscripción con estado REQUESTED en la base de datos del
     * servidor en función de la información recibida.
     * 
     * También comprueba asíncronamente, mediante un mensaje de handshake, que la
     * conexión con el endpoint configurado en la subscripción sea correcto,
     * cambiando el estado de la subscripción a ACTIVE o ERROR.
     * 
     * Por último, si todo es correcto, configura una tarea períodica para enviar
     * los heartbeats en el periodo establecido.
     * 
     * @param message mensaje recibido con la información de la subscripción.
     * @return información de la subscripción que se acaba de crear.
     */
    public String saveSubscription(String message) {
        Subscription subscription = fhirContext.newJsonParser().parseResource(Subscription.class, message);

        String[] parts = subscription.getTopic().split("/");
        Long idSubscriptionTopic = Long.valueOf(parts[parts.length - 1]);
        SubscriptionTopicDB subscriptionTopicDB = subscriptionTopicRepository.getById(idSubscriptionTopic);

        SubscriptionDB subscriptionDB = subscriptionMapper.toEntity(subscription, subscriptionTopicDB);
        subscriptionDB = subscriptionRepository.save(subscriptionDB);

        Long idSub = subscriptionDB.getId();
        Integer heartbeatPeriod = subscriptionDB.getHeartbeatPeriod();

        // Programa una tarea para comprobar la conexión con el endpoint e inicia una
        // tarea programada para enviar los heartbeats
        subscriptionschedulerManager.programaTarea(() -> eventService.sendHandshake(idSub), 30);
        subscriptionschedulerManager.iniciarTarea(idSub, heartbeatPeriod, () -> eventService.sendHeartbeat(idSub));

        subscription = subscriptionMapper.toSubscription(subscriptionDB);
        return fhirContext.newJsonParser().encodeResourceToString(subscription);
    }

    /**
     * Obtiene una subscripción de la base de datos del servidor, creando el recurso
     * FHIR {@link Subscription} correspondiente.
     * 
     * @param idSubscription identificador de la subscripción a obtener.
     * @return información de la subscripción.
     */
    public String getSubscription(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);

        Subscription subscription = subscriptionMapper.toSubscription(subscriptionDB);

        return fhirContext.newJsonParser().encodeResourceToString(subscription);
    }

    /**
     * Obtiene todas las subscripciones de la base de datos del servidor, creando el
     * recurso FHIR {@link Bundle} correspondiente.
     * 
     * @return información de todas las subscripciones.
     */
    public String getSubscriptions() {
        List<SubscriptionDB> subscriptionDBs = subscriptionRepository.findAll();

        Bundle bundle = subscriptionMapper.toBundleSubscription(subscriptionDBs);

        return fhirContext.newJsonParser().encodeResourceToString(bundle);
    }

    /**
     * Elimina una subscripción de la base de datos del servidor.
     * 
     * @param idSubscription identificador de la subscripción a eliminar.
     * @return información de la subscripción eliminada.
     */
    public String deleteSubscription(Long idSubscription) {
        subscriptionRepository.deleteById(idSubscription);
        subscriptionschedulerManager.cancelarTarea(idSubscription);

        OperationOutcome response = new OperationOutcome();

        return fhirContext.newJsonParser().encodeResourceToString(response);
    }

    /**
     * Actualiza el estado de una subscripción de la base de datos del servidor.
     * 
     * También comprueba asíncronamente, mediante un mensaje de handshake, que la
     * conexión con el endpoint configurado en la subscripción sea correcto,
     * cambiando el estado de la subscripción a ACTIVE o ERROR.
     * 
     * Por último, si todo es correcto, configura una tarea períodica para enviar
     * los heartbeats en el periodo establecido.
     * 
     * @param idSubscription identificador de la subscripción a actualizar.
     * @return información de la subscripción actualizada.
     */
    public String updateSubscription(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);
        subscriptionDB.setStatus(SubscriptionStatusCodes.REQUESTED.toCode());
        subscriptionDB = subscriptionRepository.save(subscriptionDB);

        Long idSub = subscriptionDB.getId();
        Integer heartbeatPeriod = subscriptionDB.getHeartbeatPeriod();

        // Programa una tarea para comprobar la conexión con el endpoint e inicia una
        // tarea programada para enviar los heartbeats
        subscriptionschedulerManager.programaTarea(() -> eventService.sendHandshake(idSub), 30);
        subscriptionschedulerManager.iniciarTarea(idSub, heartbeatPeriod, () -> eventService.sendHeartbeat(idSub));

        Subscription subscription = subscriptionMapper.toSubscription(subscriptionDB);

        return fhirContext.newJsonParser().encodeResourceToString(subscription);
    }

    /**
     * Obtiene el estado de una subscripción de la base de datos del servidor,
     * creando el recurso FHIR {@link Bundle} con la información correspondiente.
     * 
     * @param idSubscription identificador de la subscripción.
     * @return información del estado de la subscripción.
     */
    public String getStatus(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);

        Bundle bundle = subscriptionMapper.toBundleStatus(subscriptionDB);

        return fhirContext.newJsonParser().encodeResourceToString(bundle);
    }

    /**
     * Obtiene un listado de eventos de una subscripción de la base de datos del
     * servidor, creando el recurso FHIR {@link Bundle} con la información
     * correspondiente.
     * 
     * @param idSubscription    identificador de la subscripción.
     * @param eventsSinceNumber número de evento inferior del listado a obtener.
     * @param eventsUntilNumber número de evento superior del listado a obtener.
     * @return información de los eventos de la subscripción solicitado.
     */
    public String getEvents(Long idSubscription, Long eventsSinceNumber, Long eventsUntilNumber) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);
        List<EventDB> events = eventRepository.findByIdSubscriptionAndNumberBetweenOrderByNumber(
                subscriptionDB.getId(), eventsSinceNumber, eventsUntilNumber);

        Bundle bundle = subscriptionMapper.toBundleEvents(subscriptionDB, events);

        return fhirContext.newJsonParser().encodeResourceToString(bundle);
    }

    /**
     * Obtiene todas las subscripciones de la base de datos del servidor, creando un
     * listado de objetos de dominio {@link SubscriptionDTO} con la información
     * correspondiente.
     * 
     * @return un listado de objetos de dominio con la información correspondiente.
     */
    public List<SubscriptionDTO> getSubscriptionsDTO() {
        List<SubscriptionDB> subscriptionDBs = subscriptionRepository.findAll();

        List<SubscriptionDTO> subscriptionDTOs = subscriptionDBs.stream().map(subscriptionMapper::toDTO)
                .collect(Collectors.toList());

        return subscriptionDTOs;
    }

    /**
     * Obtiene una subscripción de la base de datos del servidor, creando un objeto
     * de dominio {@link SubscriptionDTO} con la información correspondiente.
     * 
     * @param idSubscription identificador de la subscripción.
     * @return información de la subscripción solicitada.
     */
    public SubscriptionDTO getSubscriptionDTO(Long idSubscription) {
        SubscriptionDB subscriptionDB = subscriptionRepository.getById(idSubscription);

        return subscriptionMapper.toDTO(subscriptionDB);
    }
}
