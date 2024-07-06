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

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.json.JSONArray;
import org.json.JSONObject;
///import org.json.JSONArray;
//import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;

/**
 * Esta clase desarrolla las distintas operaciones que se realizan sobre elementos FHIR.
 * @author juanmabrazo98
 * @version 1.0
 * @date jul 2024
 * 
 */
@Service
public class FhirClient {
	 private static final Logger logger = LogManager.getLogger();

    @Value("${fhir.server.username}")
    private String username;

    @Value("${fhir.server.password}")
    private String password;

    @Value("${application.address}")
    private String applicationAddress;

    private final RestTemplate restTemplate;

    /**
     * Constructor que inyecta RestTemplate.
     * 
     * @param restTemplate el RestTemplate para realizar solicitudes HTTP.
     */
    public FhirClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Crea los encabezados HTTP necesarios para la autenticación.
     * 
     * @return los encabezados HTTP.
     */
    private HttpHeaders createHeaders() {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.valueOf("application/fhir+json"));
        return headers;
    }

    /**
     * Obtiene una lista de SubscriptionTopics desde un servidor FHIR.
     * 
     * @param fhirUrl la URL del servidor FHIR.
     * @return una lista de SubscriptionTopics.
     */
    public List<SubscriptionTopicDetails> getSubscriptionTopics(String fhirUrl) {
        try {
            String url = fhirUrl + "/SubscriptionTopic";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String json = response.getBody();

            FhirContext ctx = FhirContext.forR5();
            IParser parser = ctx.newJsonParser();
            Bundle bundle = parser.parseResource(Bundle.class, json);

            if (bundle == null || bundle.getEntry().isEmpty()) {
                return Collections.emptyList();
            }

            List<SubscriptionTopicDetails> topicDetails = new ArrayList<>();
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                SubscriptionTopic topic = (SubscriptionTopic) entry.getResource();
                String name = topic.getTitle();
                String id = topic.getIdElement().getIdPart();
                String topicUrl = topic.getUrl();

                List<SubscriptionTopicDetails.FilterDetail> filters = new ArrayList<>();
                for (SubscriptionTopic.SubscriptionTopicCanFilterByComponent filterComponent : topic.getCanFilterBy()) {
                    String description = filterComponent.getDescription();
                    String filterParameter = filterComponent.getFilterParameter();

                    // Obtener comparadores y modificadores
                    List<String> comparators = new ArrayList<>();
                    for (org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.Enumerations.SearchComparator> comparatorEnum : filterComponent.getComparator()) {
                        comparators.add(comparatorEnum.getCode());
                    }

                    List<String> modifiers = new ArrayList<>();
                    for (org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.Enumerations.SearchModifierCode> modifierEnum : filterComponent.getModifier()) {
                        modifiers.add(modifierEnum.getCode());
                    }

                    filters.add(new SubscriptionTopicDetails.FilterDetail(description, filterParameter, comparators, modifiers));
                }

                topicDetails.add(new SubscriptionTopicDetails(name, id, topicUrl, filters));
            }

            return topicDetails;
        } catch (ResourceAccessException e) {
            System.err.println("Error accessing FHIR server: " + e.getMessage());
            return Collections.emptyList();
        } catch (HttpClientErrorException e) {
            System.err.println("Client error: " + e.getMessage());
            System.err.println("Response body: " + e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene una lista de suscripciones desde un servidor FHIR.
     * 
     * @param fhirUrl la URL del servidor FHIR.
     * @return una lista de suscripciones.
     */
    public List<SubscriptionDetails> getSubscriptions(String fhirUrl) {
        try {
            System.out.println("1. obteniendo subscripciones");
            String url = fhirUrl + "/Subscription";
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.valueOf("application/fhir+json"));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String json = response.getBody();

            FhirContext ctx = FhirContext.forR5();
            IParser parser = ctx.newJsonParser();
            Bundle bundle = parser.parseResource(Bundle.class, json);

            if (bundle == null || bundle.getEntry().isEmpty()) {
                return Collections.emptyList();
            }
            List<SubscriptionDetails> subscriptionDetails = new ArrayList<>();
            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                Subscription subscription = (Subscription) entry.getResource();
                String endpoint = subscription.getEndpoint();
                if (endpoint.startsWith(applicationAddress)) {
                    String topicTitle = subscription.getTopic();
                    String id = subscription.getIdElement().getIdPart();

                    List<SubscriptionDetails.FilterDetail> filters = new ArrayList<>();
                    for (Subscription.SubscriptionFilterByComponent filter : subscription.getFilterBy()) {
                        String filterParameter = filter.getFilterParameter();
                        String comparator = (filter.getComparator() != null) ? filter.getComparator().toCode() : null;
                        String modifier = (filter.getModifier() != null) ? filter.getModifier().toCode() : null;
                        String value = filter.getValue();
                        filters.add(new SubscriptionDetails.FilterDetail(filterParameter, comparator, modifier, value));
                    }
                    subscriptionDetails.add(new SubscriptionDetails(endpoint, topicTitle, id, filters));
                }
                
            }
            return subscriptionDetails; 

        } catch (ResourceAccessException e) {
            System.err.println("Error accessing FHIR server: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene el título de un SubscriptionTopic a partir de su referencia.
     * 
     * @param referencia la referencia del SubscriptionTopic.
     * @return el título del SubscriptionTopic.
     */
    public String getTopicTitle(String referencia) {
        try {
            String url = referencia;
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String json = response.getBody();

            FhirContext ctx = FhirContext.forR5();
            IParser parser = ctx.newJsonParser();
            SubscriptionTopic topic = parser.parseResource(SubscriptionTopic.class, json);

            return topic.getTitle();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.GONE) {
                System.err.println("Resource was deleted: " + e.getMessage());
            } else {
                System.err.println("Error accessing FHIR server: " + e.getMessage());
            }
            return "Unknown";
        } catch (ResourceAccessException e) {
            System.err.println("Error accessing FHIR server: " + e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Obtiene el recurso asociado a un SubscriptionTopic a partir de su referencia.
     * 
     * @param referencia la referencia del SubscriptionTopic.
     * @return el recurso asociado al SubscriptionTopic.
     */
    public String getTopicResource(String referencia) {
        try {
            String url = referencia;
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String json = response.getBody();

            JSONObject jsonResponse = new JSONObject(json);

            if (jsonResponse.has("resourceTrigger")) {
                JSONObject resourceTrigger = jsonResponse.getJSONArray("resourceTrigger").getJSONObject(0);
                if (resourceTrigger.has("resource")) {
                    return resourceTrigger.getString("resource");
                } else {
                    throw new RuntimeException("No resource found in the resourceTrigger");
                }
            } else {
                throw new RuntimeException("No resourceTrigger found in the SubscriptionTopic");
            }
        } catch (Exception e) {
            System.err.println("Error accessing FHIR server: " + e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Obtiene la interacción asociada a un SubscriptionTopic a partir de su referencia.
     * 
     * @param referencia la referencia del SubscriptionTopic.
     * @return la interacción asociada al SubscriptionTopic.
     */
    public String getTopicInteraction(String referencia) {
        try {
            String url = referencia;
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String json = response.getBody();

            JSONObject jsonResponse = new JSONObject(json);

            if (jsonResponse.has("resourceTrigger")) {
                JSONObject resourceTrigger = jsonResponse.getJSONArray("resourceTrigger").getJSONObject(0);
                if (resourceTrigger.has("supportedInteraction")) {
                    return resourceTrigger.getJSONArray("supportedInteraction").getString(0);
                } else {
                    throw new RuntimeException("No supportedInteraction found in the resourceTrigger");
                }
            } else {
                throw new RuntimeException("No resourceTrigger found in the SubscriptionTopic");
            }
        } catch (Exception e) {
            System.err.println("Error accessing FHIR server: " + e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Obtiene una lista de IDs de SubscriptionTopics desde un servidor FHIR.
     * 
     * @param fhirUrl la URL del servidor FHIR.
     * @return una lista de IDs de SubscriptionTopics.
     */
    public List<String> getSubscriptionTopicIds(String fhirUrl) {
        try {
            String url = fhirUrl + "/SubscriptionTopic";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String json = response.getBody();

            FhirContext ctx = FhirContext.forR5();
            IParser parser = ctx.newJsonParser();
            Bundle bundle = parser.parseResource(Bundle.class, json);

            if (bundle == null || bundle.getEntry().isEmpty()) {
                return Collections.emptyList();
            }

            return bundle.getEntry().stream()
                    .map(entry -> (SubscriptionTopic) entry.getResource())
                    .map(SubscriptionTopic::getIdElement)
                    .map(id -> id.getIdPart())
                    .collect(Collectors.toList());
        } catch (ResourceAccessException e) {
            System.err.println("Error accessing FHIR server: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Elimina una suscripción en el servidor FHIR.
     * 
     * @param subscriptionId el ID de la suscripción a eliminar.
     */
    public void deleteSubscription(String subscriptionId, String fhirUrl) {
        try {
            String url = fhirUrl + "/Subscription/" + subscriptionId;
            System.out.println("3. eliminando" + url);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
            System.out.println("Subscription deleted successfully.");
        } catch (ResourceAccessException e) {
            System.err.println("Error accessing FHIR server: " + e.getMessage());
        }
    }

    /**
     * Obtiene los detalles de los filtros de un SubscriptionTopic desde un servidor FHIR.
     * 
     * @param topicUrl la URL del SubscriptionTopic.
     * @param fhirUrl la URL del servidor FHIR.
     * @return una lista de detalles de los filtros.
     */
    public List<SubscriptionTopicDetails.FilterDetail> getFilters(String topicUrl, String fhirUrl) {
        try {
            String url = fhirUrl + "/SubscriptionTopic?url=" + topicUrl;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String json = response.getBody();

            FhirContext ctx = FhirContext.forR5();
            Bundle bundle = ctx.newJsonParser().parseResource(Bundle.class, json);

            if (bundle == null || bundle.getEntry().isEmpty()) {
                return new ArrayList<>();
            }

            SubscriptionTopic topic = (SubscriptionTopic) bundle.getEntryFirstRep().getResource();
            List<SubscriptionTopicDetails.FilterDetail> filters = new ArrayList<>();
            for (SubscriptionTopic.SubscriptionTopicCanFilterByComponent filterComponent : topic.getCanFilterBy()) {
                String description = filterComponent.getDescription();
                String filterParameter = filterComponent.getFilterParameter();

                // Obtener comparadores y modificadores
                List<String> comparators = new ArrayList<>();
                for (org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.Enumerations.SearchComparator> comparatorEnum : filterComponent.getComparator()) {
                    comparators.add(comparatorEnum.getCode());
                }

                List<String> modifiers = new ArrayList<>();
                for (org.hl7.fhir.r5.model.Enumeration<org.hl7.fhir.r5.model.Enumerations.SearchModifierCode> modifierEnum : filterComponent.getModifier()) {
                    modifiers.add(modifierEnum.getCode());
                }

                filters.add(new SubscriptionTopicDetails.FilterDetail(description, filterParameter, comparators, modifiers));
            }

            return filters;
        } catch (Exception e) {
            System.err.println("Error fetching filters: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Crea una nueva suscripción en el servidor FHIR.
     * 
     * @param topicUrl la URL del SubscriptionTopic.
     * @param payload el payload de la suscripción.
     * @param filters la lista de filtros de la suscripción.
     * @param fhirUrl la URL del servidor FHIR.
     * @param endpoint el endpoint de la suscripción.
     */
    public void createSubscription(String topicUrl, String payload, List<Filter> filters, String fhirUrl, String endpoint) {
    	logger.debug("Entro en createSubscription del fhirClient");
        try {
            String url = fhirUrl + "/Subscription";
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject subscriptionJson = new JSONObject();
            subscriptionJson.put("resourceType", "Subscription");
            subscriptionJson.put("status", "active");
            subscriptionJson.put("topic", topicUrl);

            JSONObject channelType = new JSONObject();
            channelType.put("code", "rest-hook");

            subscriptionJson.put("channelType", channelType);
            subscriptionJson.put("endpoint", endpoint);
            subscriptionJson.put("heartbeatPeriod", 60);
            subscriptionJson.put("timeout", 300);
            subscriptionJson.put("content", payload);
            subscriptionJson.put("contentType", "application/fhir+json");

            JSONArray filterByArray = new JSONArray();
            for (Filter filter : filters) {
                if (!"NULL".equals(filter.getValue())) {
                    JSONObject filterBy = new JSONObject();
                    filterBy.put("filterParameter", filter.getParameter());
                    if (filter.getComparator() != null || !filter.getComparator().isEmpty()) {
                        filterBy.put("comparator", filter.getComparator());
                    }
                    if (filter.getModifier() != null || !filter.getModifier().isEmpty()) {
                        filterBy.put("modifier", filter.getModifier());
                    }
                    filterBy.put("value", filter.getValue());
                    filterByArray.put(filterBy);
                }
            }

            if (filterByArray.length()!=0) {
                subscriptionJson.put("filterBy", filterByArray);
            }

            HttpEntity<String> entity = new HttpEntity<>(subscriptionJson.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("Subscription created successfully.");
            } else {
                System.err.println("Failed to create subscription. Status code: " + response.getStatusCode());
            }
        } catch (ResourceAccessException e) {
            System.err.println("Error accessing FHIR server: " + e.getMessage());
        } catch (HttpClientErrorException e) {
            System.err.println("Client error: " + e.getMessage());
            System.err.println("Response body: " + e.getResponseBodyAsString());
        } catch(Exception e) {
        	 System.err.println("capturada excepcion: " + e.getMessage());
        }
    }

    /**
     * Obtiene la URL completa del recurso asociado a una notificación a partir de su JSON.
     * 
     * @param json el JSON que contiene la notificación.
     * @return la URL completa del recurso de notificación.
     */
    public String getNotificationResourceId(String json) {
        System.out.println(json);
        String id = null;

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray entries = jsonObject.getJSONArray("entry");

            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.getJSONObject(i);

                if (entry.has("request")) {
                    String fullUrl = entry.getString("fullUrl");
                    id = fullUrl;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return id;
    }
}
