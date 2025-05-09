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

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r5.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.entities.domain.SubscriptionDetails;
import us.dit.fkbroker.service.entities.domain.SubscriptionForm;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicDetails;
import us.dit.fkbroker.service.services.fhir.FhirServerService;
import us.dit.fkbroker.service.services.fhir.FhirService;
import us.dit.fkbroker.service.services.fhir.SubscriptionService;

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
@RequestMapping("/fhir/servers/{idServer}/subscriptions")
public class SubscriptionController {

    private final FhirService fhirService;
    private final SubscriptionService subscriptionService;
    private final FhirServerService fhirServerService;

    /**
     * Constructor que inyecta los servicios {@link FhirService},
     * {@link SubscriptionService} y {@link FhirServerService}.
     * 
     * @param fhirService         servicio para gestionar operaciones que se
     *                            realizan sobre elementos FHIR.
     * @param subscriptionService servicio para gestionar las operaciones sobre las
     *                            entidades {@link Subscription}.
     * @param fhirServerService   servicio para gestionar los servidores FHIR.
     */
    @Autowired
    public SubscriptionController(FhirService fhirService, SubscriptionService subscriptionService,
            FhirServerService fhirServerService) {
        this.fhirService = fhirService;
        this.subscriptionService = subscriptionService;
        this.fhirServerService = fhirServerService;
    }

    /**
     * Maneja las solicitudes GET para obtener la página de suscripciones.
     * 
     * @param model        el modelo de Spring para añadir atributos.
     * @param idFhirServer el id del servidor FHIR.
     * @return el nombre de la vista "subscriptions-manager".
     */
    @GetMapping
    public String getSubscriptionsAndTopics(@PathVariable Long idServer, Model model) {

        Optional<FhirServer> optionalServer = fhirServerService.getFhirServer(idServer);

        if (optionalServer.isPresent()) {
            FhirServer server = optionalServer.get();
            String urlServer = server.getUrl();

            List<SubscriptionTopicDetails> topics = fhirService.getSubscriptionTopics(urlServer);
            List<SubscriptionDetails> subscriptions = fhirService.getSubscriptions(urlServer);

            model.addAttribute("fhirServer", server);
            model.addAttribute("subscriptionTopics", topics);
            model.addAttribute("subscriptions", subscriptions);
        } else {
            // TODO lanzar error
        }

        return "fhir/subscriptions-manager";
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
    @PostMapping("/form")
    public String subscriptionForm(@PathVariable Long idServer, @RequestParam String idTopic, Model model) {

        Optional<FhirServer> optionalServer = fhirServerService.getFhirServer(idServer);

        if (optionalServer.isPresent()) {
            String urlServer = optionalServer.get().getUrl();

            SubscriptionTopicDetails topicDetails = fhirService.getSubscriptionTopic(idTopic, urlServer);
            SubscriptionForm subscriptionForm = new SubscriptionForm(topicDetails);

            model.addAttribute("topic", topicDetails);
            model.addAttribute("subscriptionForm", subscriptionForm);
            model.addAttribute("idServer", idServer);
        } else {
            // TODO lanzar error
        }

        return "fhir/subscription-form";
    }

    /**
     * Maneja las solicitudes POST para eliminar una suscripción.
     * 
     * @param subscriptionId el ID de la suscripción a eliminar.
     * @param idFhirServer   el id del servidor FHIR.
     * @return una redirección a la página principal.
     */
    @PostMapping("/{idSubs}/delete")
    public String deleteSubscription(@PathVariable Long idServer, @PathVariable String idSubs) {

        Optional<FhirServer> optionalServer = fhirServerService.getFhirServer(idServer);

        if (optionalServer.isPresent()) {
            FhirServer server = optionalServer.get();
            String urlServer = server.getUrl();

            fhirService.deleteSubscription(idSubs, urlServer);
            subscriptionService.deleteSubscription(server, idSubs);
        } else {
            // TODO lanzar error
        }

        return "redirect:/fhir/servers/" + idServer + "/subscriptions";
    }

    /**
     * Maneja las solicitudes POST para enviar los filtros de una suscripción.
     * 
     * @param requestParams los parámetros de la solicitud.
     * @param urlServer     la URL del servidor FHIR.
     * @return una redirección a la página de suscripciones.
     */
    @PostMapping("/create")
    public String createSubscription(@PathVariable Long idServer,
            @ModelAttribute("subscriptionForm") SubscriptionForm subscriptionForm) {

        Optional<FhirServer> optionalServer = fhirServerService.getFhirServer(idServer);

        if (optionalServer.isPresent()) {
            FhirServer server = optionalServer.get();
            String urlServer = server.getUrl();

            Long idSubs = subscriptionService.getId();

            // Crea la subscripción en el servidor FHIR
            Subscription createdSubscription = fhirService.createSubscription(urlServer, idSubs, subscriptionForm);

            // Guarda los datos de la subscripción en BBDD
            SubscriptionData subscription = new SubscriptionData();
            subscription.setId(idSubs);
            subscription.setServer(server);
            subscription.setSubscription(createdSubscription.getIdElement().getIdPart());
            subscription.setResource(subscriptionForm.getResource());
            subscription.setInteraction(subscriptionForm.getInteraction());
            subscription.setEvents((long) 0);
            subscriptionService.saveSubscription(subscription);
        } else {
            // TODO lanzar error
        }

        return "redirect:/fhir/servers/" + idServer + "/subscriptions";
    }
}
