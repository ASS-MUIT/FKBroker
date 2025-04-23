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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import us.dit.fkbroker.service.entities.db.KieServer;
import us.dit.fkbroker.service.entities.db.Signal;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.entities.domain.NotificationDetails;
import us.dit.fkbroker.service.services.fhir.FhirService;
import us.dit.fkbroker.service.services.fhir.NotificationService;
import us.dit.fkbroker.service.services.fhir.SubscriptionService;
import us.dit.fkbroker.service.services.kie.KieServerService;
import us.dit.fkbroker.service.services.kie.SignalService;

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

    private final KieServerService kieServerService;
    private final SignalService signalService;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;

    /**
     * Constructor que inyecta los servicios {@link FhirService},
     * {@link KieServerService} y {@link SignalService}.
     * 
     * @param kieServerService    servicio para gestionar las operaciones sobre las
     *                            entidades {@link KieServer}.
     * @param signalService       servicio para gestionar las operaciones sobre las
     *                            entidades {@link Signal}.
     * @param subscriptionService servicio para gestionar las operaciones sobre las
     *                            entidades {@link SubscriptionData}.
     * @param notificationService servicio para gestionar las notificaciones de
     *                            subscripciones.
     */
    @Autowired
    public NotificationController(KieServerService kieServerService, SignalService signalService,
            SubscriptionService subscriptionService, NotificationService notificationService) {
        this.kieServerService = kieServerService;
        this.signalService = signalService;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
    }

    /**
     * Método que maneja las notificaciones. Llama al método para enviar las señales
     * a los servidores kie y responde al servidor FHIR indicando que se ha recibido
     * la notificación
     * 
     * @param id   el ID de la notificación.
     * @param json el JSON que contiene los detalles de la notificación.
     * @return una respuesta HTTP con el cuerpo del JSON proporcionado.
     */
    @PostMapping("/{id}")
    public ResponseEntity<String> sendNotification(@PathVariable Long id, @RequestBody String message) {
        Optional<SubscriptionData> optionalSubscription = subscriptionService.findById(id);
        if (optionalSubscription.isPresent()) {
            // Responder inmediatamente con 200 OK
            CompletableFuture.runAsync(() -> {
                logger.info("Se recibe un mensaje de notificación: {}", message);
                SubscriptionData subscription = optionalSubscription.get();

                // Se obtienen los recursos de las notificaciones
                NotificationDetails notification = notificationService.processNotification(message, subscription);

                if (notification.getHasNewEvents()) {
                    // Actualiza el número de eventos recibidos
                    subscription.setEvents(notification.getLastEvent());
                    subscriptionService.saveSubscription(subscription);

                    // Se obtiene la señal que se debe enviar
                    Optional<Signal> optionalSignal = signalService.getSignalByResourceAndInteraction(
                            subscription.getResource(), subscription.getInteraction());
                    if (optionalSignal.isPresent()) {
                        // Se envía una señal a todos los servidores KIE por cada recurso notificado
                        for (String resource : notification.getReferenceEvents()) {
                            logger.info("Llamamos a sendsignal. Id del recurso: {}", resource);
                            kieServerService.sendSignalToAllKieServers(optionalSignal.get(), resource);
                        }
                    }
                }
            });
        } else {
            // Manejar el caso cuando no se encuentra la entidad
            throw new RuntimeException("NotificationEP not found with id: " + id);
        }

        return ResponseEntity.ok(message);
    }
}
