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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import us.dit.fkbroker.service.entities.db.KieServer;
import us.dit.fkbroker.service.entities.db.Trigger;
import us.dit.fkbroker.service.entities.domain.SignalDetails;
import us.dit.fkbroker.service.services.fhir.TriggerService;
import us.dit.fkbroker.service.services.kie.KieServerService;
import us.dit.fkbroker.service.services.kie.SignalService;

/**
 * Controlador para gestionar las operaciones sobre los servidores KIE.
 * 
 * @author juanmabrazo98
 * @author josperbel - Nueva ubicación de entidades
 * @version 1.1
 * @date Mar 2025
 */
@Controller
@RequestMapping("/kie")
public class KieController {

    private final KieServerService kieServerService;
    private final SignalService signalService;
    private final TriggerService triggerService;

    /**
     * Constructor que inyecta los servicios {@link KieServerService},
     * {@link SignalService} y {@link TriggerService}.
     * 
     * @param kieServerService servicio utilizado para gestionar los servidores KIE.
     * @param signalService    servicio utilizado para gestionar las señales.
     * @param triggerService   servicio utilizado para gestionar los triggers.
     */
    @Autowired
    public KieController(KieServerService kieServerService, SignalService signalService,
            TriggerService triggerService) {
        this.kieServerService = kieServerService;
        this.signalService = signalService;
        this.triggerService = triggerService;
    }

    /**
     * Maneja las solicitudes GET de la página principal de gestión KIE. Obtiene la
     * lista de servidores KIE, las señales y los triggers disponibles y los añade
     * en el modelo.
     * 
     * @param model el modelo de Spring para añadir los atributos.
     * @return el nombre de la vista de la página principal de gestión KIE..
     */
    @GetMapping
    public String getKieServers(Model model) {
        // Obtiene los datos de los servidores KIE y los añade al modelo
        List<KieServer> kieServers = kieServerService.getAllKieServers();
        model.addAttribute("kieServers", kieServers);

        // Obtiene los datos de las señales y los añade al modelo
        List<SignalDetails> signals = signalService.getAllSignals();
        model.addAttribute("signals", signals);

        // Obtiene los datos de los triggers y los añade al modelo
        List<Trigger> triggers = triggerService.getAllTriggers();
        model.addAttribute("triggers", triggers);

        return "kie/kieServers";
    }

    /**
     * Maneja las solicitudes POST para añadir un nuevo servidor KIE.
     * 
     * @param kieServer el objeto KieServer a añadir.
     * @return una redirección a la página principal de gestión KIE.
     */
    @PostMapping("/servers/add")
    public String addKieServer(@ModelAttribute KieServer kieServer) {
        kieServerService.saveKieServer(kieServer);
        return "redirect:/kie";
    }

    /**
     * Maneja las solicitudes POST para añadir una nueva señal.
     * 
     * @param idTrigger identificador del trigger que se quiere asociar a la nueva
     *                  señal.
     * @param name      nombre de la nueva señal.
     * @return una redirección a la página principal de gestión KIE.
     */
    @PostMapping("/signals/add")
    public String addSignal(@RequestParam Long idTrigger, @RequestParam String name) {
        signalService.saveSignal(idTrigger, name);
        return "redirect:/kie";
    }

    /**
     * Maneja las solicitudes POST para eliminar un servidor KIE.
     * 
     * @param url URL del servidor KIE a eliminar.
     * @return una redirección a la página principal de gestión KIE.
     */
    @PostMapping("/servers/delete")
    public String deleteKieServer(@RequestParam String url) {
        kieServerService.deleteKieServer(url);
        return "redirect:/kie";
    }

    /**
     * Maneja las solicitudes POST para eliminar una señal.
     * 
     * @param id identificador de la señal a eliminar.
     * @return una redirección a la página principal de gestión KIE.
     */
    @PostMapping("/signals/delete")
    public String deleteSignal(@RequestParam Long id) {
        signalService.deleteSignal(id);
        return "redirect:/kie";
    }

    /**
     * Maneja las solicitudes POST para editar una señal.
     * 
     * @param id   identificador de la señal a editar.
     * @param name nuevo nombre de la señal.
     * @return una redirección a la página principal de gestión KIE.
     */
    @PostMapping("/signals/edit")
    public String editSignal(@RequestParam Long id, @RequestParam String name) {
        signalService.updateSignal(id, name);
        return "redirect:/kie";
    }
}
