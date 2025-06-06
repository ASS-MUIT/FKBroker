package us.dit.fkbroker.service.services.fhir;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.hl7.fhir.r5.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.entities.db.Topic;
import us.dit.fkbroker.service.entities.domain.SubscriptionDetails;
import us.dit.fkbroker.service.entities.domain.SubscriptionEntry;
import us.dit.fkbroker.service.entities.domain.SubscriptionForm;
import us.dit.fkbroker.service.repositories.SubscriptionRepository;
import us.dit.fkbroker.service.services.mapper.SubscriptionMapper;

/**
 * Servicio para manejar las operaciones de la entidad {@link SubscriptionData}.
 * 
 * @author josperbel
 * @version 1.0
 * @date Abr 2025
 */
@Service
public class SubscriptionService {

    @Value("${application.address}")
    private String applicationAddress;

    private static final Logger logger = LogManager.getLogger();

    private final FhirService fhirService;
    private final EntityManager entityManager;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    /**
     * Constructor que inyecta el {@link SubscriptionRepository}.
     * 
     * @param subscriptionRepository repositorio JPA de la entidad
     *                               {@link SubscriptionData}.
     * @param entityManager          interfaz usada para interaccionar con el
     *                               contexto de persistencia.
     */
    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, EntityManager entityManager,
            FhirService fhirService, SubscriptionMapper subscriptionMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.entityManager = entityManager;
        this.fhirService = fhirService;
        this.subscriptionMapper = subscriptionMapper;
    }

    /**
     * Obtiene una subscripción de la base de datos por su identificador.
     * 
     * @param idEndpoint identificador del endpoint de la subscripción.
     * @return la subscripción obtenida.
     * @throws RuntimeException si no encuentra la subscripción.
     */
    public SubscriptionData getSubscriptionData(Long idEndpoint) {
        Optional<SubscriptionData> optionalSubscription = subscriptionRepository.findByIdEndpoint(idEndpoint);

        if (optionalSubscription.isPresent()) {
            return optionalSubscription.get();
        } else {
            throw new RuntimeException("Subscription not found with endpoint id: " + idEndpoint);
        }
    }

    /**
     * Obtiene los detalles de una subscripción de un servidor FHIR.
     * 
     * @param server datos del servidor FHIR.
     * @param id     identificador de la subscripción.
     * @return los detalles de la subscripción obtenida.
     */
    public SubscriptionDetails getSubscriptionDetails(FhirServer server, String id) {
        Subscription subscription = fhirService.getSubscription(server.getUrl(), id);
        return subscriptionMapper.toDetails(subscription);
    }

    /**
     * Obtiene todas las subscripciones de un servidor FHIR en la base de datos.
     * 
     * @param idServer identificador del servidor FHIR.
     * @return el listado de subscripciones de la base de datos.
     */
    public List<SubscriptionData> getSubscriptions(Long idServer) {
        return subscriptionRepository.findByServerId(idServer);
    }

    /**
     * Obtiene todas las subscripciones de un servidor FHIR y actualiza la
     * información de base de datos si ha ocurrido algún cambio.
     * 
     * @param server información del servidor FHIR.
     * @return el listado de subscripciones actualizado.
     */
    public List<SubscriptionEntry> getAndUpdateSubscriptions(FhirServer server) {
        List<SubscriptionEntry> subscriptionEntries = new ArrayList<SubscriptionEntry>();

        // Obtiene los datos de las subscripciones del servidor FHIR
        List<Subscription> serverSubscriptions = fhirService.getSubscriptions(server.getUrl());

        // Obtiene los datos de las subscripciones guardadas en la base de datos
        List<SubscriptionData> databaseSubscriptions = subscriptionRepository.findByServerId(server.getId());

        // Convierte la lista en mapa por su identificador para facilitar las búsquedas
        Map<String, Subscription> serverSubscriptionsMap = serverSubscriptions.stream()
                .collect(Collectors.toMap(Subscription::getIdPart, obj -> obj));

        for (SubscriptionData subscriptionData : databaseSubscriptions) {
            if (!serverSubscriptionsMap.containsKey(subscriptionData.getIdSubscription())) {
                // Si no se encuentra la subscripción en el servidor FHIR, se actualiza el
                // estado de la subscripción en base de datos a NULL
                subscriptionData.setStatus(SubscriptionStatusCodes.NULL.toCode());
                subscriptionData.setUpdated(new Date());
                subscriptionData = subscriptionRepository.save(subscriptionData);
            } else {
                // Si se encuentra, obtiene el estado de la subscripción
                Subscription subscription = serverSubscriptionsMap.get(subscriptionData.getIdSubscription());
                String status = subscription.getStatus().toCode();

                // Actualiza el estado de la subscripción de base de datos si hay algún cambio
                if (!status.equals(subscriptionData.getStatus())) {
                    subscriptionData.setStatus(status);
                    subscriptionData.setUpdated(new Date());
                    subscriptionData = subscriptionRepository.save(subscriptionData);
                }
            }

            // Mapea las entradas
            SubscriptionEntry subscriptionEntry = subscriptionMapper.toEntry(subscriptionData);
            subscriptionEntries.add(subscriptionEntry);
        }

        return subscriptionEntries;
    }

    /**
     * Crea una nueva subscripción con los datos pasados en el servidor FHIR y
     * guarda los detalles de la misma en la base de datos.
     * 
     * @param server           información del servidor FHIR donde se debe crear la
     *                         subscripción.
     * @param server           información del Subscription Topic para el que se
     *                         debe crear la subscipción.
     * @param subscriptionForm datos de la subscripción que se desea crear.
     */
    public void createSubscription(FhirServer server, Topic topic, SubscriptionForm subscriptionForm) {
        // Obtiene un identificador único (usado para crear el endpoint)
        Long idEndpoint = ((Number) entityManager.createNativeQuery("SELECT nextval('sub_seq')").getSingleResult())
                .longValue();
        String endpoint = applicationAddress + "notification/" + idEndpoint;

        // Crea la subscripción en el servidor FHIR
        Subscription subscription = subscriptionMapper.toSubscription(subscriptionForm, endpoint);
        Subscription createdSubscription = fhirService.createSubscription(server.getUrl(), subscription);

        // Guarda los datos de la subscripción en la base de datos
        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setIdEndpoint(idEndpoint);
        subscriptionData.setServer(server);
        subscriptionData.setTopic(topic);
        subscriptionData.setIdSubscription(createdSubscription.getIdElement().getIdPart());
        subscriptionData.setEvents((long) 0);
        subscriptionData.setUpdated(new Date());
        subscriptionRepository.save(subscriptionData);
    }

    /**
     * Actualiza los datos de una subscripción en la base de datos.
     * 
     * @param subscriptionData datos de la subscripción a actualizar.
     * @return los datos de la subscripción actualizada.
     */
    public SubscriptionData updateSubscription(SubscriptionData subscriptionData) {
        subscriptionData.setUpdated(new Date());
        return subscriptionRepository.save(subscriptionData);
    }

    /**
     * Elimina una subscripción de un servidor FHIR y de la base de datos.
     * 
     * @param server         datos del servidor FHIR de la subscripción a eliminar.
     * @param idSubscription identificador de la subscripción a eliminar.
     */
    @Transactional
    public void deleteSubscription(FhirServer server, String idSubscription) {
        // Elimina la subscripción del servidor FHIR
        fhirService.deleteSubscription(server.getUrl(), idSubscription);

        // Elimina la subscripción de la base de datos
        subscriptionRepository.deleteByServerAndIdSubscription(server, idSubscription);
    }
}
