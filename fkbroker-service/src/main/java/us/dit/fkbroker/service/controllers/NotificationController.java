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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import us.dit.fkbroker.service.entities.db.NotificationEP;
import us.dit.fkbroker.service.services.fhir.FhirService;
import us.dit.fkbroker.service.services.kie.KieServerService;
import us.dit.fkbroker.service.services.kie.NotificationEPService;

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

    @Autowired
    private NotificationEPService notificationEPService;

    @Autowired
    private KieServerService kieServerService;

    @Autowired
    private FhirService fhirService;

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
    public ResponseEntity<String> sendNotification(@PathVariable Long id, @RequestBody String json) {
        Optional<NotificationEP> optionalNotificationEP = notificationEPService.findById(id);
        if (optionalNotificationEP.isPresent()) {
            // Responder inmediatamente con 200 OK
            CompletableFuture.runAsync(() -> {
                NotificationEP notificationEP = optionalNotificationEP.get();
                String idRecurso = fhirService.getNotificationResourceId(json);
                System.out.println("Llamamos a sendsignal. Id del recurso: " + idRecurso);
                kieServerService.sendSignalToAllKieServers(notificationEP, idRecurso);
            });
        } else {
            // Manejar el caso cuando no se encuentra la entidad
            throw new RuntimeException("NotificationEP not found with id: " + id);
        }

        return ResponseEntity.ok(json);
    }
}
