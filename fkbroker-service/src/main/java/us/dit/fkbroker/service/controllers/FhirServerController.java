package us.dit.fkbroker.service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import us.dit.fkbroker.service.entities.domain.FhirServerDTO;
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
     * @param fhirServerService servicio para gestionar los servidores FHIR.
     */
    @Autowired
    public FhirServerController(FhirServerService fhirServerService) {
        this.fhirServerService = fhirServerService;
    }

    /**
     * Maneja las solicitudes GET para obtener la lista de servidores FHIR.
     * 
     * @param model el modelo de Spring para añadir atributos.
     * @return el nombre de la vista "fhirServers".
     */
    @GetMapping
    public String getKieServers(Model model) {
        model.addAttribute("fhirServers", fhirServerService.getAllFhirServers());
        return "fhir/servers";
    }

    /**
     * Maneja las solicitudes POST para añadir un nuevo servidor FHIR.
     * 
     * @param kieServer el objeto KieServer a añadir.
     * @return una redirección a la página de servidores FHIR.
     */
    @PostMapping
    public String addKieServer(@ModelAttribute FhirServerDTO fhirServer) {
        fhirServerService.saveFhirServer(fhirServer);
        return "redirect:/fhir/servers";
    }

    /**
     * Maneja las solicitudes POST para eliminar un servidor FHIR.
     * 
     * @param url la URL del servidor FHIR a eliminar.
     * @return una redirección a la página de servidores FHIR.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteKieServer(@PathVariable Long id) {
        fhirServerService.deleteFhirServer(id);
        return ResponseEntity.noContent().build();
    }

}
