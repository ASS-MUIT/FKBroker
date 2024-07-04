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
**/
package us.dit.fkbroker.service.services.fhir;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio para manejar las operaciones relacionadas con SubscriptionTopics y Subscriptions.
/**
 * Esta clase desarrolla las distintas operaciones que se realizan sobre elementos FHIR.
 * @author juanmabrazo98
 * @version 1.0
 * @date jul 2024
 * 
 */
@Service
public class SubscriptionService {

    private final FhirClient fhirClient;

    /**
     * Constructor que inyecta el FhirClient.
     * 
     * @param fhirClient el cliente FHIR para realizar operaciones FHIR.
     */
    @Autowired
    public SubscriptionService(FhirClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    /**
     * Obtiene una lista de SubscriptionTopics desde un servidor FHIR.
     * 
     * @param url la URL del servidor FHIR.
     * @return una lista de SubscriptionTopics.
     */
    public List<SubscriptionTopicDetails> getSubscriptionTopics(String url) {
        return fhirClient.getSubscriptionTopics(url);
    }

    /**
     * Obtiene una lista de Subscriptions desde un servidor FHIR.
     * 
     * @param url la URL del servidor FHIR.
     * @return una lista de Subscriptions.
     */
    public List<SubscriptionDetails> getSubscriptions(String url) {
        return fhirClient.getSubscriptions(url);
    }

    /**
     * Obtiene una lista de IDs de SubscriptionTopics desde un servidor FHIR.
     * 
     * @param url la URL del servidor FHIR.
     * @return una lista de SubscriptionTopics.
     */
    public List<String> getSubscriptionTopicIds(String url) {
        return fhirClient.getSubscriptionTopicIds(url);
    }

    /**
     * Obtiene los filtros para un SubscriptionTopic dado desde un servidor FHIR.
     * 
     * @param topicUrl la URL del SubscriptionTopic.
     * @param fhirUrl la URL del servidor FHIR.
     * @return una lista de detalles de los filtros del SubscriptionTopic.
     */
    public List<SubscriptionTopicDetails.FilterDetail> getFilters(String topicUrl, String fhirUrl) {
        return fhirClient.getFilters(topicUrl, fhirUrl);
    }

    /**
     * Elimina una Subscription en el servidor FHIR.
     * 
     * @param subscriptionId el ID de la Subscription a eliminar.
     */
    public void deleteSubscription(String subscriptionId, String url) {
        fhirClient.deleteSubscription(subscriptionId, url);
    }

    /**
     * Obtiene el recurso asociado a un SubscriptionTopic a partir de su URL.
     * 
     * @param topicUrl la URL del SubscriptionTopic.
     * @return el recurso asociado al SubscriptionTopic.
     */
    public String getTopicResource(String topicUrl) {
        return fhirClient.getTopicResource(topicUrl);
    }

    /**
     * Obtiene la interacción asociada a un SubscriptionTopic a partir de su URL.
     * 
     * @param topicUrl la URL del SubscriptionTopic.
     * @return la interacción asociada al SubscriptionTopic.
     */
    public String getTopicInteraction(String topicUrl) {
        return fhirClient.getTopicInteraction(topicUrl);
    }

    /**
     * Crea una nueva Subscription en el servidor FHIR.
     * 
     * @param topicUrl la URL del SubscriptionTopic.
     * @param payload el payload de la Subscription.
     * @param filters la lista de filtros de la Subscription.
     * @param fhirUrl la URL del servidor FHIR.
     * @param endpoint el endpoint de la Subscription.
     */
    public void createSubscription(String topicUrl, String payload, List<Filter> filters, String fhirUrl, String endpoint) {
        fhirClient.createSubscription(topicUrl, payload, filters, fhirUrl, endpoint);
    }
}
