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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.services.fhir.FhirServerService;
import us.dit.fkbroker.service.services.fhir.SubscriptionTopicService;

/**
 * Controlador que gestiona las llamadas a los métodos necesarios al navegar por
 * la interfaz web.
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Controller
@RequestMapping("/fhir/servers/{idServer}/topics")
public class SubscriptionTopicController {

    private final FhirServerService fhirServerService;
    private final SubscriptionTopicService subscriptionTopicService;

    /**
     * Constructor que inyecta los servicios {@link FhirServerService} y
     * {@link SubscriptionTopicService}.
     * 
     * @param fhirServerService        servicio utilizado para gestionar los
     *                                 servidores FHIR.
     * @param subscriptionTopicService servicio utilizado para gestionar los temas
     *                                 de las subscripciones.
     */
    @Autowired
    public SubscriptionTopicController(FhirServerService fhirServerService,
            SubscriptionTopicService subscriptionTopicService) {
        this.fhirServerService = fhirServerService;
        this.subscriptionTopicService = subscriptionTopicService;
    }

    /**
     * Maneja las solicitudes GET para obtener la página de detalles de un tema de
     * subscripción. Obtiene los datos del servidor FHIR y el SubscriptionTopic.
     * 
     * @param model    el modelo de Spring para añadir atributos.
     * @param idServer identificador del servidor FHIR.
     * @param idTopic  identificador del SubscriptionTopic.
     * @return el nombre de la vista la página de detalles de un tema de
     *         subscripción.
     * 
     */
    @GetMapping("/{idTopic}")
    public String getTopic(Model model, @PathVariable Long idServer, @PathVariable String idTopic) {
        // Obtiene los datos del servidor FHIR y los añade al modelo
        FhirServer server = fhirServerService.getFhirServer(idServer);
        model.addAttribute("fhirServer", server);

        // Obtiene los detalles del SubscriptionTopic y los añade al modelo
        String topic = subscriptionTopicService.getSubscriptionTopicString(server.getUrl(), idTopic);
        model.addAttribute("topic", topic);

        return "fhir/subscriptiontopic-detail";
    }
}
