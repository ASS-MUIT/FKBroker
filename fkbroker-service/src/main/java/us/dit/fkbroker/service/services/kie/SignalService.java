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
package us.dit.fkbroker.service.services.kie;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.Signal;
import us.dit.fkbroker.service.repositories.SignalRepository;

/**
 * Servicio para gestionar las operaciones sobre las entidades {@link Signal}.
 * 
 * @author juanmabrazo98
 * @author josperbel - Nueva ubicación de entidades
 * @version 1.1
 * @date Mar 2025
 */
@Service
public class SignalService {

    @Autowired
    private SignalRepository signalRepository;

    /**
     * Obtiene todas las entidades {@link Signal}.
     * 
     * @return una lista de objetos {@link Signal} que representan todas las
     *         entidades {@link Signal}.
     */
    public List<Signal> getAllSignals() {
        return signalRepository.findAll();
    }

    /**
     * Guarda una entidad {@link Signal} en la base de datos.
     * 
     * @param notificationEP el objeto {@link Signal} a guardar.
     * @return el objeto {@link Signal} guardado.
     */
    public Signal saveSignal(Signal signal) {
        return signalRepository.save(signal);
    }

    /**
     * Elimina una entidad {@link Signal} de la base de datos por su ID.
     * 
     * @param id el ID de la entidad {@link Signal} a eliminar.
     */
    public void deleteSignal(Long id) {
        signalRepository.deleteById(id);
    }

    /**
     * Obtiene una entidad {@link Signal} por su recurso e interacción.
     * 
     * @param resource    el recurso de la entidad {@link Signal}.
     * @param interaction la interacción de la entidad {@link Signal}.
     * @return un Optional que contiene la entidad {@link Signal} si se encuentra, o
     *         un Optional vacío si no.
     */
    public Optional<Signal> getSignalByResourceAndInteraction(String resource, String interaction) {
        return signalRepository.findByResourceAndInteraction(resource, interaction);
    }

    /**
     * Busca una entidad {@link Signal} por su ID.
     * 
     * @param id el ID de la entidad {@link Signal}.
     * @return un Optional que contiene la entidad {@link Signal} si se encuentra, o
     *         un Optional vacío si no.
     */
    public Optional<Signal> findById(Long id) {
        return signalRepository.findById(id);
    }
}
