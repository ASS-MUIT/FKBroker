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
package us.dit.fkbroker.service.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.services.fhir.FhirService;
import us.dit.fkbroker.service.services.fhir.NotificationService;
import us.dit.fkbroker.service.services.fhir.SubscriptionService;

/**
 * Controlador para manejar las notificaciones.
 * 
 * @author juanmabrazo98
 * @author josperbel - Nueva ubicación de entidades y utilización de nuevo
 *         servicio {@link FhirService}
 * @version 1.1
 * @date Mar 2025
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private static final Logger logger = LogManager.getLogger();

    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;

    /**
     * Constructor que inyecta los servicios {@link SubscriptionService} y
     * {@link NotificationService}.
     * 
     * @param subscriptionService servicio para gestionar las operaciones sobre las
     *                            entidades {@link SubscriptionData}.
     * @param notificationService servicio para gestionar las notificaciones de
     *                            subscripciones.
     */
    @Autowired
    public NotificationController(SubscriptionService subscriptionService, NotificationService notificationService) {
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
    }

    /**
     * Método que maneja las notificaciones. Publica las referencias de recursos
     * en Kafka y responde al servidor FHIR indicando que se ha recibido
     * la notificación.
     * 
     * @param id      identificador del endpoint de la notificación.
     * @param message mensaje que contiene los detalles de la notificación.
     * @return una respuesta HTTP con el cuerpo del JSON proporcionado.
     */
    @PostMapping("/{id}")
    public ResponseEntity<String> sendNotification(@PathVariable Long id, @RequestBody String message) {
        logger.info("Se recibe un mensaje de notificación: {}", message);

        // Obtiene los datos de la subscripción
        SubscriptionData subscription = subscriptionService.getSubscriptionData(id);

        // Procesa el mensaje de notificación, obtiene los detalles de la subscripción
        subscription = notificationService.processNotification(message, subscription);

        // Actualiza la subscripción con los detalles de la notificación
        subscriptionService.updateSubscription(subscription);

        return ResponseEntity.ok(message);
    }
}
