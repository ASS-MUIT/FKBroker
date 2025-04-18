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

import us.dit.fkbroker.service.entities.db.Signal;
import us.dit.fkbroker.service.services.kie.SignalService;

/**
 * Controlador para gestionar las operaciones sobre las entidades
 * {@link Signal}.
 * 
 * @author juanmabrazo98
 * @author josperbel - Nueva ubicación de entidades
 * @version 1.1
 * @date Mar 2025
 */
@Controller
@RequestMapping("/signals")
public class SignalController {

    @Autowired
    private SignalService signalService;

    /**
     * Maneja las solicitudes GET para obtener la lista de entidades {@link Signal}.
     * 
     * @param model el modelo de Spring para añadir atributos.
     * @return el nombre de la vista.
     */
    @GetMapping
    public String getSignals(Model model) {
        model.addAttribute("signals", signalService.getAllSignals());
        return "signals";
    }

    /**
     * Maneja las solicitudes POST para añadir una nueva entidad {@link Signal}.
     * 
     * @param notificationEP el objeto {@link Signal} a añadir.
     * @return una redirección a la página de entidades {@link Signal}.
     */
    @PostMapping("/add")
    public String addSignal(@ModelAttribute Signal signal) {
        signalService.saveSignal(signal);
        return "redirect:/signals";
    }

    /**
     * Maneja las solicitudes POST para eliminar una entidad {@link Signal}.
     * 
     * @param id el ID de la entidad NotificationEP a eliminar.
     * @return una redirección a la página de entidades {@link Signal}.
     */
    @PostMapping("/delete")
    public String deleteSignal(@RequestParam Long id) {
        signalService.deleteSignal(id);
        return "redirect:/signals";
    }

    /**
     * Maneja las solicitudes POST para editar una entidad {@link Signal}.
     * 
     * @param id el ID de la entidad NotificationEP a editar.
     * @param signalName el nuevo nombre de la señal.
     * @return una redirección a la página de entidades {@link Signal}.
     */
    @PostMapping("/edit")
    public String editSignal(@RequestParam Long id, @RequestParam String signalName) {
        Optional<Signal> optionalSignal = signalService.findById(id);
        if (optionalSignal.isPresent()) {
            Signal signal = optionalSignal.get();
            signal.setSignalName(signalName);
            signalService.saveSignal(signal);
        }
        return "redirect:/signals";
    }
}
