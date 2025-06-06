package us.dit.fkbroker.service.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador que gestiona las llamadas a los métodos principales.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Controller
public class HomeController {

    /**
     * Maneja las solicitudes GET para obtener la página principal.
     * 
     * @param model el modelo de Spring para añadir atributos.
     * @return el nombre de la vista de la página principal.
     */
    @GetMapping("/")
    public String getHomePage(Model model) {
        return "index";
    }

}
