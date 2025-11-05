/**
*  This file is part of FKBroker - Broker sending signals to KIEServers from FHIR notifications.
*  Copyright (C) 2024  Universidad de Sevilla/Departamento de IngenierÃ­a TelemÃ¡tica
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
package us.dit.fkbroker.service.controllers;

import java.util.List;

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
import us.dit.fkbroker.service.entities.db.Topic;
import us.dit.fkbroker.service.entities.domain.SubscriptionEntry;
import us.dit.fkbroker.service.entities.domain.SubscriptionForm;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicDetails;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicEntry;
import us.dit.fkbroker.service.services.fhir.FhirServerService;
import us.dit.fkbroker.service.services.fhir.FhirService;
import us.dit.fkbroker.service.services.fhir.SubscriptionService;
import us.dit.fkbroker.service.services.fhir.SubscriptionTopicService;

/**
 * Controlador que gestiona las llamadas a los mÃ©todos necesarios al navegar por
 * la interfaz web.
 * 
 * @author juanmabrazo98
 * @author josperbel - Nueva ubicaciÃ³n de entidades, utilizaciÃ³n de nuevos
 *         servicios {@link FhirService} y {@link FhirServerService} y
 *         optimizaciÃ³n del mÃ©todo createSubscription evitando varias llamadas
 *         para obtener un SubscriptionTopic
 * @version 1.2
 * @date Mar 2025
 */
@Controller
@RequestMapping("/fhir/servers/{idServer}/subscriptions")
public class SubscriptionController {

    private final FhirServerService fhirServerService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionTopicService subscriptionTopicService;

    /**
     * Constructor que inyecta los servicios {@link FhirServerService},
     * {@link SubscriptionService} y {@link SubscriptionTopicService}.
     * 
     * @param fhirServerService        servicio utilizado para gestionar los
     *                                 servidores FHIR.
     * @param subscriptionService      servicio utilizado para gestionar las
     *                                 subscripciones.
     * @param subscriptionTopicService servicio utilizado para gestionar los temas
     *                                 de las subscripciones.
     */
    @Autowired
    public SubscriptionController(FhirServerService fhirServerService, SubscriptionService subscriptionService,
            SubscriptionTopicService subscriptionTopicService) {
        this.fhirServerService = fhirServerService;
        this.subscriptionService = subscriptionService;
        this.subscriptionTopicService = subscriptionTopicService;
    }

    /**
     * Maneja las solicitudes GET para obtener la pÃ¡gina principal de un servidor
     * FHIR.
     * 
     * @param model    el modelo de Spring para aÃ±adir atributos.
     * @param idServer identificador del servidor FHIR.
     * @return el nombre de la vista "subscriptions-manager".
     */
    @GetMapping
    public String getAndUpdateSubscriptionsAndTopics(Model model, @PathVariable Long idServer) {
        // Obtiene los datos del servidor FHIR y los aÃ±ade al modelo
        FhirServer server = fhirServerService.getFhirServer(idServer);
        model.addAttribute("fhirServer", server);

        // Obtiene los datos de los SubscriptionTopics, guardando en la base de datos
        // aquellos que no se encuentren, y los aÃ±ade al modelo
        List<SubscriptionTopicEntry> topics = subscriptionTopicService.getAndUpdateSubscriptionTopics(server);
        model.addAttribute("subscriptionTopics", topics);

        // Obtiene los datos de los Subscriptions, actualizando el estado si detecta
        // errores o eventos perdidos, y los aÃ±ade al modelo
        List<SubscriptionEntry> subscriptions = subscriptionService.getAndUpdateSubscriptions(server);
        model.addAttribute("subscriptions", subscriptions);

        return "fhir/subscriptions-manager";
    }

    /**
     * Maneja las solicitudes POST para crear una nueva suscripciÃ³n.
     * 
     * @param model    el modelo de Spring para aÃ±adir atributos.
     * @param idServer identificador del servidor FHIR.
     * @param idTopic  identificador del tema de subscripciÃ³n al que se desea
     *                 subscribirse.
     * @return el nombre de la vista "subscription-form".
     */
    @PostMapping("/form")
    public String subscriptionForm(Model model, @PathVariable Long idServer, @RequestParam String idTopic) {
        // Obtiene los datos del servidor FHIR y los aÃ±ade al modelo
        FhirServer server = fhirServerService.getFhirServer(idServer);
        model.addAttribute("fhirServer", server);

        // Obtiene los detalles del SubscriptionTopic y los aÃ±ade al modelo
        SubscriptionTopicDetails topic = subscriptionTopicService.getSubscriptionTopicDetails(server.getUrl(), idTopic);
        model.addAttribute("topic", topic);

        // Crea la entidad SubscriptionForm base y la aÃ±ade al modelo
        SubscriptionForm subscriptionForm = new SubscriptionForm(topic);
        model.addAttribute("subscriptionForm", subscriptionForm);

        return "fhir/subscription-form";
    }

    /**
     * Maneja las solicitudes POST para eliminar una suscripciÃ³n.
     * 
     * @param idServer identificador del servidor FHIR.
     * @param idSubs   identificador de la subscripciÃ³n.
     * @return una redirecciÃ³n a la pÃ¡gina principal.
     */
    @PostMapping("/{idSubs}/delete")
    public String deleteSubscription(@PathVariable Long idServer, @PathVariable String idSubs) {
        // Obtiene los datos del servidor FHIR
        FhirServer server = fhirServerService.getFhirServer(idServer);

        // Elimina la subscripciÃ³n del servidor FHIR y de la base de datos
        subscriptionService.deleteSubscription(server, idSubs);

        return "redirect:/fhir/servers/" + idServer + "/subscriptions";
    }

    /**
     * Maneja las solicitudes POST para enviar los filtros de una suscripciÃ³n.
     * 
     * @param idServer         identificador del servidor FHIR.
     * @param subscriptionForm atributos del formulario de subscripciÃ³n.
     * @return una redirecciÃ³n a la pÃ¡gina de suscripciones.
     */
    @PostMapping("/create")
    public String createSubscription(@PathVariable Long idServer,
            @ModelAttribute("subscriptionForm") SubscriptionForm subscriptionForm) {
        // Obtiene los datos del servidor FHIR
        FhirServer server = fhirServerService.getFhirServer(idServer);

        // Obtiene los datos del Subscription Topic
        Topic topic = subscriptionTopicService.getSubscriptionTopic(server, subscriptionForm.getIdTopic());

        // Crea la subscripciÃ³n en el servidor FHIR y en la base de datos
        subscriptionService.createSubscription(server, topic, subscriptionForm);

        return "redirect:/fhir/servers/" + idServer + "/subscriptions";
    }

    /**
     * Maneja las solicitudes GET para obtener la pÃ¡gina de suscripciones.
     * 
     * @param model        el modelo de Spring para aÃ±adir atributos.
     * @param idFhirServer el id del servidor FHIR.
     * @return el nombre de la vista "subscriptions-manager".
     */
    @GetMapping("/{idSubs}")
    public String getSubscription(@PathVariable Long idServer, @PathVariable String idSubs, Model model) {
        // Obtiene los datos del servidor FHIR y los aÃ±ade al modelo
        FhirServer server = fhirServerService.getFhirServer(idServer);
        model.addAttribute("fhirServer", server);

        // Obtiene los datos de la subscripciÃ³n y los aÃ±ade al modelo
        String subscription = subscriptionService.getSubscriptionDetails(server, idSubs);
        model.addAttribute("subscription", subscription);

        return "fhir/subscription-detail";
    }
}
