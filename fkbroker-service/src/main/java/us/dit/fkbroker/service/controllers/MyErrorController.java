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

import java.net.ConnectException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import us.dit.fkbroker.service.services.fhir.FhirService;

/**
 * Controlador que gestiona las llamadas a los métodos necesarios al navegar por
 * la interfaz web.
 * 
 * @author juanmabrazo98
 * @version 1.0
 * @date jul 2024
 * 
 */
@Controller
public class MyErrorController implements ErrorController {

    private static final Logger logger = LogManager.getLogger(FhirService.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;

        String error = null;
        if (exception instanceof Throwable) {
            Throwable root = getRootCause((Throwable) exception);

            if (root instanceof ConnectException) {
                error = "No se pudo establecer la conexión con el servidor FHIR";
            } else {
                error = "Error desconocido: " + root.getClass().getSimpleName();
            }
        } else {
            error = "No se pudo determinar el tipo de error";
        }

        model.addAttribute("status", getErrorName(statusCode));
        model.addAttribute("error", error);

        logger.error("Error {} - {}", statusCode, error);

        return "error";
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private String getErrorName(int statusCode) {
        if (statusCode == 400)
            return "Solicitud incorrecta (400)";
        else if (statusCode == 401)
            return "No autorizado (401)";
        else if (statusCode == 403)
            return "Prohibido (403)";
        else if (statusCode == 404)
            return "No encontrado (404)";
        else if (statusCode == 500)
            return "Error interno del servidor (500)";
        else
            return "Error desconocido";
    }
}