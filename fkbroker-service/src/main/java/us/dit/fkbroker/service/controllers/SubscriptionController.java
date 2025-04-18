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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import us.dit.fkbroker.service.entities.db.Signal;
import us.dit.fkbroker.service.entities.domain.FhirServerDTO;
import us.dit.fkbroker.service.entities.domain.Filter;
import us.dit.fkbroker.service.entities.domain.SubscriptionDetails;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicDetails;
import us.dit.fkbroker.service.services.fhir.FhirServerService;
import us.dit.fkbroker.service.services.fhir.FhirService;
import us.dit.fkbroker.service.services.kie.SignalService;

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
    private final SignalService signalService;
    private final FhirServerService fhirServerService;

    /**
     * Constructor que inyecta los servicios {@link FhirService},
     * {@link SignalService} y {@link FhirServerService}.
     * 
     * @param fhirService           servicio para gestionar operaciones que se
     *                              realizan sobre elementos FHIR.
     * @param notificationEPService servicio para gestionar las operaciones sobre
     *                              las entidades NotificationEP.
     * @param fhirServerService     servicio para gestionar los servidores FHIR.
     */
    @Autowired
    public SubscriptionController(FhirService fhirService, SignalService signalService,
            FhirServerService fhirServerService) {
        this.fhirService = fhirService;
        this.signalService = signalService;
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
    public String getSubscriptionPage(Model model) {

        List<FhirServerDTO> fhirServers = fhirServerService.getAllFhirServers();

        model.addAttribute("fhirServers", fhirServers);

        return "fhir/subscriptions-manager";
    }

    /**
     * Maneja las solicitudes GET para obtener la página de suscripciones.
     * 
     * @param model        el modelo de Spring para añadir atributos.
     * @param idFhirServer el id del servidor FHIR.
     * @return el nombre de la vista "subscriptions-manager".
     */
    @PostMapping("/server")
    public String getSubscriptionsAndTopics(@RequestParam String server, RedirectAttributes redirectAttributes) {

        List<SubscriptionTopicDetails> topics = fhirService.getSubscriptionTopics(server);
        List<SubscriptionDetails> subscriptions = fhirService.getSubscriptions(server);

        redirectAttributes.addFlashAttribute("subscriptionTopics", topics);
        redirectAttributes.addFlashAttribute("subscriptions", subscriptions);
        redirectAttributes.addFlashAttribute("urlServer", server);

        return "redirect:/subscriptions";
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
    public String createSubscription(@RequestParam String idTopic, @RequestParam String urlServer, Model model) {

        // Obtiene el SubscriptionTopic
        SubscriptionTopicDetails topicDetails = fhirService.getSubscriptionTopic(idTopic, urlServer);

        List<SubscriptionTopicDetails.FilterDetail> filters = topicDetails.getFilters();
        String topicUrl = topicDetails.getUrl();

        // Obtener recurso e interacción del topic
        String resource = topicDetails.getResource();
        String interaction = topicDetails.getInteraction();
        String endpoint;
        logger.info("recurso: " + resource + " interaction: " + interaction);

        // Obtiene el endpoint
        Signal signal = signalService.getSignalByResourceAndInteraction(resource, interaction);
        endpoint = applicationAddress + "notification/" + signal.getId();

        model.addAttribute("endpoint", endpoint);
        model.addAttribute("topicUrl", topicUrl);
        model.addAttribute("filters", filters);
        model.addAttribute("urlServer", urlServer);
        logger.debug("Saliendo de create-suscription con los datos " + model.toString());

        return "fhir/subscription-form";
    }

    /**
     * Maneja las solicitudes POST para eliminar una suscripción.
     * 
     * @param subscriptionId el ID de la suscripción a eliminar.
     * @param idFhirServer   el id del servidor FHIR.
     * @return una redirección a la página principal.
     */
    @PostMapping("/delete-subscription")
    public String deleteSubscription(@RequestParam String subscriptionId, @RequestParam String urlServer) {

        fhirService.deleteSubscription(subscriptionId, urlServer);

        return "redirect:/subscriptions";
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
    public String submitFilters(@RequestParam Map<String, String> requestParams, @RequestParam String urlServer,
            @RequestParam String endpoint, Model model) {

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
        logger.debug("Invoco el método createSubscription de suscriptionService con ", payload, urlServer, endpoint,
                filters, topicUrl);
        fhirService.createSubscription(topicUrl, payload, filters, urlServer, endpoint);

        return "redirect:/subscriptions";
    }
}
