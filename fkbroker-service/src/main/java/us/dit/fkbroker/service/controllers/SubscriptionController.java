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
package us.dit.fkbroker.service.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import us.dit.fkbroker.service.entities.db.NotificationEP;
import us.dit.fkbroker.service.entities.domain.FhirServerDTO;
import us.dit.fkbroker.service.entities.domain.Filter;
import us.dit.fkbroker.service.entities.domain.SubscriptionDetails;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicDetails;
import us.dit.fkbroker.service.services.fhir.FhirServerService;
import us.dit.fkbroker.service.services.fhir.FhirService;
import us.dit.fkbroker.service.services.kie.NotificationEPService;

/**
 * Controlador que gestiona las llamadas a los métodos necesarios al navegar por
 * la interfaz web.
 * 
 * @author juanmabrazo98
 * @author josperbel - Nueva ubicación de entidades, utilización de nuevos
 *         servicios {@link FhirService} y {@link FhirServerService} y
 *         optimización del método createSubscription evitando varias llamadas
 *         para obtener un SubscriptionTopic
 * @version 1.2
 * @date Mar 2025
 */
@Controller
public class SubscriptionController {

    private static final Logger logger = LogManager.getLogger();

    @Value("${application.address}")
    private String applicationAddress;

    private final FhirService fhirService;
    private final NotificationEPService notificationEPService;
    private final FhirServerService fhirServerService;

    /**
     * Constructor que inyecta los servicios {@link FhirService},
     * {@link NotificationEPService} y {@link FhirServerService}.
     * 
     * @param fhirService           servicio para gestionar operaciones que se
     *                              realizan sobre elementos FHIR.
     * @param notificationEPService servicio para gestionar las operaciones sobre
     *                              las entidades NotificationEP.
     * @param fhirServerService     servicio para gestionar los servidores FHIR.
     */
    @Autowired
    public SubscriptionController(FhirService fhirService, NotificationEPService notificationEPService,
            FhirServerService fhirServerService) {
        this.fhirService = fhirService;
        this.notificationEPService = notificationEPService;
        this.fhirServerService = fhirServerService;
    }

    /**
     * Maneja las solicitudes GET para obtener la página principal.
     * 
     * @param model el modelo de Spring para añadir atributos.
     * @return el nombre de la vista "index".
     */
    @GetMapping("/")
    public String getHomePage(Model model) {
        return "index";
    }

    /**
     * Maneja las solicitudes GET para obtener la página de suscripciones.
     * 
     * @param model        el modelo de Spring para añadir atributos.
     * @param idFhirServer el id del servidor FHIR.
     * @return el nombre de la vista "subscriptions-manager".
     */
    @GetMapping("/subscriptions")
    public String getSubscriptionPage(Model model, @RequestParam(required = false) Long idFhirServer) {

        if (idFhirServer == null) {
            List<FhirServerDTO> fhirServers = fhirServerService.getAllFhirServers();

            model.addAttribute("fhirServers", fhirServers);

            return "subscriptions-select-server";
        } else {
            Optional<FhirServerDTO> optionalFhirServer = fhirServerService.getFhirServer(idFhirServer);

            if (optionalFhirServer.isPresent()) {
                FhirServerDTO fhirServer = optionalFhirServer.get();

                List<SubscriptionTopicDetails> topics = fhirService.getSubscriptionTopics(fhirServer.getUrl());
                List<SubscriptionDetails> subscriptions = fhirService.getSubscriptions(fhirServer.getUrl());

                model.addAttribute("subscriptionTopics", topics);
                model.addAttribute("subscriptions", subscriptions);
                model.addAttribute("idFhirServer", idFhirServer);
            }

            return "subscriptions-manager";
        }
    }

    /**
     * Maneja las solicitudes POST para crear una nueva suscripción.
     * 
     * @param topicUrl     la URL del tema de la suscripción.
     * @param payload      el payload de la suscripción.
     * @param idFhirServer el id del servidor FHIR.
     * @param model        el modelo de Spring para añadir atributos.
     * @return el nombre de la vista "subscription-form".
     */
    @PostMapping("/create-subscription")
    public String createSubscription(@RequestParam String idTopic, @RequestParam String payload,
            @RequestParam Long idFhirServer, Model model) {

        Optional<FhirServerDTO> optionalFhirServer = fhirServerService.getFhirServer(idFhirServer);

        if (optionalFhirServer.isPresent()) {
            FhirServerDTO fhirServer = optionalFhirServer.get();

            // Obtiene el SubscriptionTopic
            SubscriptionTopicDetails topicDetails = fhirService.getSubscriptionTopic(idTopic, fhirServer.getUrl());

            List<SubscriptionTopicDetails.FilterDetail> filters = topicDetails.getFilters();
            String topicUrl = topicDetails.getUrl();

            // Obtener recurso e interacción del topic
            String resource = topicDetails.getResource();
            String interaction = topicDetails.getInteraction();
            String endpoint;
            logger.info("recurso: " + resource + " interaction: " + interaction);

            // Comparar si existe ya
            Optional<NotificationEP> optionalNotificationEP = notificationEPService
                    .findNotificationEPByResourceAndInteraction(resource, interaction);
            if (optionalNotificationEP.isPresent()) {
                // Si existe, obtener el id y este será nuestro endpoint
                NotificationEP existingNotificationEP = optionalNotificationEP.get();
                endpoint = applicationAddress + "notification/" + existingNotificationEP.getId();
                logger.debug("Usando endpoint ya existente " + endpoint);
            } else {
                // Si no existe, se crea un endpoint nuevo
                String signalName = interaction + "-" + resource;
                NotificationEP newNotificationEP = new NotificationEP();
                newNotificationEP.setResource(resource);
                newNotificationEP.setInteraction(interaction);
                newNotificationEP.setSignalName(signalName);
                NotificationEP savedNotificationEP = notificationEPService.saveNotificationEP(newNotificationEP);
                endpoint = applicationAddress + "notification/" + savedNotificationEP.getId();
                logger.debug("Creado nuevo endpoint " + endpoint);
            }

            model.addAttribute("endpoint", endpoint);
            model.addAttribute("topicUrl", topicUrl);
            model.addAttribute("payload", payload);
            model.addAttribute("filters", filters);
            model.addAttribute("idFhirServer", idFhirServer);
            logger.debug("Saliendo de create-suscription con los datos " + model.toString());
        }

        return "subscription-form";
    }

    /**
     * Maneja las solicitudes POST para eliminar una suscripción.
     * 
     * @param subscriptionId el ID de la suscripción a eliminar.
     * @param idFhirServer   el id del servidor FHIR.
     * @return una redirección a la página principal.
     */
    @PostMapping("/delete-subscription")
    public String deleteSubscription(@RequestParam String subscriptionId, @RequestParam Long idFhirServer) {
        Optional<FhirServerDTO> optionalFhirServer = fhirServerService.getFhirServer(idFhirServer);

        if (optionalFhirServer.isPresent()) {
            FhirServerDTO fhirServer = optionalFhirServer.get();

            fhirService.deleteSubscription(subscriptionId, fhirServer.getUrl());
        }

        return "redirect:/subscriptions?idFhirServer=" + idFhirServer;
    }

    /**
     * Maneja las solicitudes POST para enviar los filtros de una suscripción.
     * 
     * @param requestParams los parámetros de la solicitud que contienen los
     *                      filtros.
     * @param idFhirServer  el id del servidor FHIR.
     * @param endpoint      el endpoint de la suscripción.
     * @param model         el modelo de Spring para añadir atributos.
     * @return una redirección a la página de suscripciones.
     */
    @PostMapping("/submit-filters")
    public String submitFilters(@RequestParam Map<String, String> requestParams, @RequestParam Long idFhirServer,
            @RequestParam String endpoint, Model model) {
        Optional<FhirServerDTO> optionalFhirServer = fhirServerService.getFhirServer(idFhirServer);

        if (optionalFhirServer.isPresent()) {
            FhirServerDTO fhirServer = optionalFhirServer.get();

            List<Filter> filters = new ArrayList<>();
            String topicUrl = requestParams.get("topicUrl");
            String payload = requestParams.get("payload");
            logger.debug("Entando en submit-filters con topicURL " + topicUrl + " y payload " + payload);

            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key.startsWith("filters[") && value != null && !value.isEmpty()) {
                    logger.debug("Se han encontrado filtros");
                    String parameter = key.substring(8, key.length() - 1);
                    String comparatorKey = "comparators[" + parameter + "]";
                    String modifierKey = "modifiers[" + parameter + "]";

                    String comparator = requestParams.get(comparatorKey);
                    String modifier = requestParams.get(modifierKey);

                    Filter filter = new Filter(parameter, value, comparator, modifier);
                    filters.add(filter);
                }
            }
            logger.debug("Invoco el método createSubscription de suscriptionService con ", payload, idFhirServer,
                    endpoint, filters, topicUrl);
            fhirService.createSubscription(topicUrl, payload, filters, fhirServer.getUrl(), endpoint);
        }

        return "redirect:/subscriptions?idFhirServer=" + idFhirServer;
    }
}
