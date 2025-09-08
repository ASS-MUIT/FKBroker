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
package us.dit.fkbroker.service.services.fhir;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.Trigger;
import us.dit.fkbroker.service.repositories.TriggerRepository;

/**
 * Servicio para gestionar las operaciones sobre los triggers.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Service
public class TriggerService {

    private final TriggerRepository triggerRepository;

    /**
     * Constructor que inyecta el repositorio {@link TriggerRepository}.
     * 
     * @param triggerRepository repositorio JPA de la entidad {@link Trigger}
     */
    @Autowired
    public TriggerService(TriggerRepository triggerRepository) {
        this.triggerRepository = triggerRepository;
    }

    /**
     * Obtiene todos los triggers guardados en la base de datos.
     * 
     * @return una lista de objetos {@link Trigger} con todos los triggers guardados
     *         en la base de datos.
     */
    public List<Trigger> getAllTriggers() {
        return triggerRepository.findAll();
    }

    /**
     * Comprueba si existe un trigger en base de datos con el recurso e iteracción
     * pasados, sino lo crea.
     * 
     * @param resource    recurso del trigger a obtener o crear.
     * @param interaction interacción del trigger a obtener o crear.
     * @return el objeto trigger creado u obtenido.
     */
    public Trigger getTrigger(String resource, String interaction) {
        Optional<Trigger> optionalTrigger = triggerRepository.findByResourceAndInteraction(resource, interaction);
        if (optionalTrigger.isPresent()) {
            return optionalTrigger.get();
        } else {
            Trigger trigger = new Trigger();
            trigger.setResource(resource);
            trigger.setInteraction(interaction);
            return triggerRepository.save(trigger);
        }
    }

}
