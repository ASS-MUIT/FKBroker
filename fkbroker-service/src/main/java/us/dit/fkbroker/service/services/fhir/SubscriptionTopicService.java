package us.dit.fkbroker.service.services.fhir;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.Topic;
import us.dit.fkbroker.service.entities.db.Trigger;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicDetails;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicEntry;
import us.dit.fkbroker.service.repositories.SubscriptionTopicRepository;
import us.dit.fkbroker.service.services.mapper.SubscriptionTopicMapper;

/**
 * Servicio para manejar las operaciones de la entidad
 * {@link SubscriptionTopic}.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Service
public class SubscriptionTopicService {

    private final FhirService fhirService;
    private final TriggerService triggerService;
    private final SubscriptionTopicRepository subscriptionTopicRepository;
    private final SubscriptionTopicMapper subscriptionTopicMapper;

    /**
     * Constructor que inyecta los servicio {@link FhirService} y
     * {@link TriggerService}, el repositorio {@link SubscriptionTopicRepository} y
     * el componente {@link SubscriptionTopicMapper}.
     * 
     * @param fhirService                 servicio que realiza operaciones sobre
     *                                    servidores FHIR.
     * @param triggerService              servicio utilizado para gestionar los
     *                                    triggers.
     * @param subscriptionTopicRepository repositorio JPA de la entidad
     *                                    {@link Topic}.
     * @param subscriptionTopicMapper     componente que transforma Subscription
     *                                    Topics.
     */
    @Autowired
    public SubscriptionTopicService(FhirService fhirService, TriggerService triggerService,
            SubscriptionTopicRepository subscriptionTopicRepository, SubscriptionTopicMapper subscriptionTopicMapper) {
        this.fhirService = fhirService;
        this.triggerService = triggerService;
        this.subscriptionTopicRepository = subscriptionTopicRepository;
        this.subscriptionTopicMapper = subscriptionTopicMapper;
    }

    /**
     * Obtiene un topic de la base de datos.
     * 
     * @param server información del servidor FHIR..
     * @param id     identificador del Subscription Topic a obtener.
     * @return la entidad Topic de la base de datos.
     */
    public Topic getSubscriptionTopic(FhirServer server, String id) {
        return subscriptionTopicRepository.findByIdTopicAndServer(id, server);
    }

    /**
     * Obtiene los detalles de un Subscription Topic de un servidor FHIR.
     * 
     * @param fhirUrl URL del servidor FHIR.
     * @param id      identificador del Subscription Topic a obtener.
     * @return los detalles del Subscription Topic.
     */
    public SubscriptionTopicDetails getSubscriptionTopicDetails(String fhirUrl, String id) {
        // Obtiene el SubscriptionTopic del servidor FHIR
        SubscriptionTopic subscriptionTopic = fhirService.getSubscriptionTopic(fhirUrl, id);

        // Mapea el SubscriptionTopic
        return subscriptionTopicMapper.toDetails(subscriptionTopic);
    }

    /**
     * Obtiene los datos de los Subscription Topic de un servidor FHIR y actualiza
     * la base de datos con dicha información si es necesario.
     * 
     * @param server datos del servidor FHIR.
     * @return el listado de Subscription Topics disponibles en el servidor.
     */
    public List<SubscriptionTopicEntry> getAndUpdateSubscriptionTopics(FhirServer server) {
        // Obtiene los SubscriptionTopic del servidor FHIR
        List<SubscriptionTopic> serverTopics = fhirService.getSubscriptionTopics(server.getUrl());

        // Obtiene los SubscriptionTopic que están guardados en base de datos
        List<Topic> databaseTopics = subscriptionTopicRepository.findByServer(server);

        // Convierte las listas en mapas por su identificador
        Map<String, SubscriptionTopic> serverTopicsMap = serverTopics.stream()
                .collect(Collectors.toMap(SubscriptionTopic::getIdPart, obj -> obj));
        Map<String, Topic> databaseTopicsMap = databaseTopics.stream()
                .collect(Collectors.toMap(Topic::getIdTopic, obj -> obj));

        // Obtiene los sets con los identificadores
        Set<String> serverIds = serverTopicsMap.keySet();
        Set<String> databaseIds = databaseTopicsMap.keySet();

        // Topics que están en el servidor pero no en la base de datos
        Set<String> idsToCreate = new HashSet<>(serverIds);
        idsToCreate.removeAll(databaseIds);
        List<SubscriptionTopic> topicsToCreate = idsToCreate.stream().map(serverTopicsMap::get)
                .collect(Collectors.toList());

        // Topics que están en la base de datos pero no en el servidor
        Set<String> idsToDelete = new HashSet<>(databaseIds);
        idsToDelete.removeAll(serverIds);
        List<Topic> topicsToDelete = idsToDelete.stream().map(databaseTopicsMap::get).collect(Collectors.toList());

        // Guarda los nuevos SubscriptionTopic en la base de datos
        for (SubscriptionTopic subscriptionTopic : topicsToCreate) {
            // Para esta implementación se está suponiendo que los Topics tendrán
            // configurado un único ResourceTrigger con una única SupportedInteraction
            String resource = subscriptionTopic.getResourceTriggerFirstRep().getResource();
            String interaction = subscriptionTopic.getResourceTriggerFirstRep().getSupportedInteraction().get(0)
                    .asStringValue();

            // Comprueba si ya existe este Trigger en base de datos, sino lo crea
            Trigger trigger = triggerService.getTrigger(resource, interaction);

            // Guarda el SubscriptionTopic en la base de datos
            Topic topic = new Topic();
            topic.setIdTopic(subscriptionTopic.getIdPart());
            topic.setServer(server);
            topic.setTrigger(trigger);
            subscriptionTopicRepository.save(topic);
        }

        // Elimina de la base de datos los SubscriptionTopics eliminados
        subscriptionTopicRepository.deleteAll(topicsToDelete);

        // Mapea los SubscriptionTopic
        return serverTopics.stream().map(subscriptionTopicMapper::toEntry).collect(Collectors.toList());
    }

}
