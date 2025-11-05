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

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.services.fhir.FhirServerService;

/**
 * Controlador para gestionar las operaciones sobre los servidores FHIR.
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Controller
@RequestMapping("/fhir/servers")
public class FhirServerController {

    private final FhirServerService fhirServerService;

    /**
     * Constructor que inyecta el servicio {@link FhirServerService}.
     * 
     * @param fhirServerService servicio utilizado para gestionar los servidores
     *                          FHIR.
     */
    @Autowired
    public FhirServerController(FhirServerService fhirServerService) {
        this.fhirServerService = fhirServerService;
    }

    /**
     * Maneja las solicitudes GET de la pÃ¡gina principal de gestiÃ³n FHIR. Obtiene la
     * lista de servidores FHIR.
     * 
     * @param model el modelo de Spring para aÃ±adir atributos.
     * @return el nombre de la vista de la pÃ¡gina principal de gestiÃ³n FHIR.
     */
    @GetMapping
    public String getKieServers(Model model) {
        // Obtiene los datos de los servidores FHIR y los aÃ±ade al modelo
        List<FhirServer> fhirServers = fhirServerService.getAllFhirServers();
        model.addAttribute("fhirServers", fhirServers);

        return "fhir/servers";
    }

    /**
     * Maneja las solicitudes POST para aÃ±adir o editar un servidor FHIR.
     * 
     * @param fhirServer servidor FHIR que se desea aÃ±adir o editar.
     * @return una redirecciÃ³n a la pÃ¡gina principal de gestiÃ³n FHIR.
     */
    @PostMapping
    public String addKieServer(@ModelAttribute FhirServer fhirServer) {
        fhirServerService.saveFhirServer(fhirServer);
        return "redirect:/fhir/servers";
    }

    /**
     * Maneja las solicitudes POST para eliminar un servidor FHIR.
     * 
     * @param id identificador del servidor FHIR a eliminar.
     * @return una redirecciÃ³n a la pÃ¡gina principal de gestiÃ³n FHIR.
     */
    @PostMapping("/{id}/delete")
    public String deleteKieServer(@PathVariable Long id) {
        fhirServerService.deleteFhirServer(id);
        return "redirect:/fhir/servers";
    }

}
