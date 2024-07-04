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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import us.dit.fkbroker.service.entities.NotificationEP;
import us.dit.fkbroker.service.services.kie.NotificationEPService;

/**
 * Controlador para gestionar las operaciones sobre las entidades NotificationEP.
 * @author juanmabrazo98
 * @version 1.0
 * @date jul 2024
 * 
 */

@Controller
@RequestMapping("/notificationEPs")
public class NotificationEPController {

    @Autowired
    private NotificationEPService notificationEPService;

    /**
     * Maneja las solicitudes GET para obtener la lista de entidades NotificationEP.
     * 
     * @param model el modelo de Spring para añadir atributos.
     * @return el nombre de la vista "notificationEPs".
     */
    @GetMapping
    public String getNotificationEPs(Model model) {
        model.addAttribute("notificationEPs", notificationEPService.getAllNotificationEPs());
        model.addAttribute("newNotificationEP", new NotificationEP());
        return "notificationEPs";
    }

    /**
     * Maneja las solicitudes POST para añadir una nueva entidad NotificationEP.
     * 
     * @param notificationEP el objeto NotificationEP a añadir.
     * @return una redirección a la página de entidades NotificationEP.
     */
    @PostMapping("/add")
    public String addNotificationEP(@ModelAttribute NotificationEP notificationEP) {
        notificationEPService.saveNotificationEP(notificationEP);
        return "redirect:/notificationEPs";
    }

    /**
     * Maneja las solicitudes POST para eliminar una entidad NotificationEP.
     * 
     * @param id el ID de la entidad NotificationEP a eliminar.
     * @return una redirección a la página de entidades NotificationEP.
     */
    @PostMapping("/delete")
    public String deleteNotificationEP(@RequestParam Long id) {
        notificationEPService.deleteNotificationEP(id);
        return "redirect:/notificationEPs";
    }

    @PostMapping("/edit")
    public String editNotificationEP(@RequestParam Long id, @RequestParam String signalName) {
        Optional<NotificationEP> optionalNotificationEP = notificationEPService.findById(id);
        if (optionalNotificationEP.isPresent()) {
            NotificationEP notificationEP = optionalNotificationEP.get();
            notificationEP.setSignalName(signalName);
            notificationEPService.saveNotificationEP(notificationEP);
        }
        return "redirect:/notificationEPs"; // Redirigir a la página de Notification EPs
    }
}
