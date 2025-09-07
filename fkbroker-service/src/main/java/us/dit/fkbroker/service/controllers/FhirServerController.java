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
     * Maneja las solicitudes GET de la página principal de gestión FHIR. Obtiene la
     * lista de servidores FHIR.
     * 
     * @param model el modelo de Spring para añadir atributos.
     * @return el nombre de la vista de la página principal de gestión FHIR.
     */
    @GetMapping
    public String getKieServers(Model model) {
        // Obtiene los datos de los servidores FHIR y los añade al modelo
        List<FhirServer> fhirServers = fhirServerService.getAllFhirServers();
        model.addAttribute("fhirServers", fhirServers);

        return "fhir/servers";
    }

    /**
     * Maneja las solicitudes POST para añadir o editar un servidor FHIR.
     * 
     * @param fhirServer servidor FHIR que se desea añadir o editar.
     * @return una redirección a la página principal de gestión FHIR.
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
     * @return una redirección a la página principal de gestión FHIR.
     */
    @PostMapping("/{id}/delete")
    public String deleteKieServer(@PathVariable Long id) {
        fhirServerService.deleteFhirServer(id);
        return "redirect:/fhir/servers";
    }

}
