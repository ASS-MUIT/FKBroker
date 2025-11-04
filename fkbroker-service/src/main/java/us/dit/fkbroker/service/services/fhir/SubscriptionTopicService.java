/**
*  This file is part of FKBroker - Broker sending signals to KIEServers from FHIR notifications.
*  Copyright (C) 2024  Universidad de Sevilla/Departamento de Ingeniería Telemática
*
*  FKBroker is free software: you can redistribute it and/or
*  modify it under the terms of the GNU General Public License as published
*  by the Free Software Foundation, either version 3 of the License, or (at
*  your option) any later version.
*
*  FKBroker is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
*  Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with FKBroker. If not, see <https://www.gnu.org/licenses/>.
*
*  This software uses third-party dependencies, including libraries licensed under Apache 2.0.
*  See the project documentation for more details on dependency licenses.
**/
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
    private final SubscriptionTopicRepository subscriptionTopicRepository;
    private final SubscriptionTopicMapper subscriptionTopicMapper;

    /**
     * Constructor que inyecta los servicio {@link FhirService}, el repositorio 
     * {@link SubscriptionTopicRepository} y el componente {@link SubscriptionTopicMapper}.
     * 
     * @param fhirService                 servicio que realiza operaciones sobre
     *                                    servidores FHIR.
     * @param subscriptionTopicRepository repositorio JPA de la entidad
     *                                    {@link Topic}.
     * @param subscriptionTopicMapper     componente que transforma Subscription
     *                                    Topics.
     */
    @Autowired
    public SubscriptionTopicService(FhirService fhirService,
            SubscriptionTopicRepository subscriptionTopicRepository, SubscriptionTopicMapper subscriptionTopicMapper) {
        this.fhirService = fhirService;
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
     * Obtiene los detalles de un Subscription Topic de un servidor FHIR.
     * 
     * @param fhirUrl URL del servidor FHIR.
     * @param id      identificador del Subscription Topic a obtener.
     * @return los detalles del Subscription Topic.
     */
    public String getSubscriptionTopicString(String fhirUrl, String id) {
        // Obtiene el SubscriptionTopic del servidor FHIR
        SubscriptionTopic subscriptionTopic = fhirService.getSubscriptionTopic(fhirUrl, id);

        // Mapea el SubscriptionTopic
        return subscriptionTopicMapper.toString(subscriptionTopic);
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
            // Guarda el SubscriptionTopic en la base de datos
            Topic topic = new Topic();
            topic.setIdTopic(subscriptionTopic.getIdPart());
            topic.setServer(server);
            
            // Genera automáticamente el nombre del topic Kafka
            String kafkaTopicName = generateKafkaTopicName(subscriptionTopic.getIdPart());
            topic.setKafkaTopicName(kafkaTopicName);
            
            subscriptionTopicRepository.save(topic);
        }

        // Elimina de la base de datos los SubscriptionTopics eliminados
        subscriptionTopicRepository.deleteAll(topicsToDelete);

        // Mapea los SubscriptionTopic
        return serverTopics.stream().map(subscriptionTopicMapper::toEntry).collect(Collectors.toList());
    }

    /**
     * Elimina todos los temas de subscripción de la base de datos de un servidor.
     * 
     * @param server información del servidor FHIR.
     */
    public void deleteSubscriptionTopics(FhirServer server) {
        subscriptionTopicRepository.deleteAllByServer(server);
    }

    /**
     * Genera un nombre de topic Kafka basado en el ID del SubscriptionTopic FHIR
     * 
     * @param fhirTopicId ID del SubscriptionTopic FHIR
     * @return nombre normalizado para topic Kafka
     */
    private String generateKafkaTopicName(String fhirTopicId) {
        if (fhirTopicId == null || fhirTopicId.isEmpty()) {
            return "fhir-default";
        }
        
        // Si es una URL, extraer la última parte
        if (fhirTopicId.contains("/")) {
            String[] parts = fhirTopicId.split("/");
            fhirTopicId = parts[parts.length - 1];
        }
        
        // Normalizar: minúsculas, reemplazar caracteres no válidos con guiones
        String normalized = fhirTopicId.toLowerCase()
                                       .replaceAll("[^a-z0-9-]", "-")
                                       .replaceAll("-+", "-")  // Múltiples guiones → uno solo
                                       .replaceAll("^-|-$", ""); // Eliminar guiones al inicio/final
        
        return "fhir-" + normalized;
    }

}
