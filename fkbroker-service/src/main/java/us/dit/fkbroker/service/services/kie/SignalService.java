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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.Signal;
import us.dit.fkbroker.service.entities.db.Trigger;
import us.dit.fkbroker.service.entities.domain.SignalDetails;
import us.dit.fkbroker.service.repositories.SignalRepository;
import us.dit.fkbroker.service.repositories.TriggerRepository;
import us.dit.fkbroker.service.services.mapper.SignalMapper;

/**
 * Servicio para gestionar las operaciones sobre las entidades {@link Signal}.
 * 
 * @author juanmabrazo98
 * @author josperbel - Nueva ubicación de entidades
 * @version 1.1
 * @date May 2025
 */
@Service
public class SignalService {

    private final SignalRepository signalRepository;
    private final SignalMapper signalMapper;
    private final TriggerRepository triggerRepository;

    /**
     * Constructor que inyecta los repositorios {@link SignalRepository} y
     * {@link TriggerRepository} y el componente {@link signalMapper}.
     * 
     * @param signalRepository  repositorio JPA de la entidad {@link Signal}.
     * @param signalMapper      componente que transforma entidades y objetos de
     *                          dominio de señales.
     * @param triggerRepository repositorio JPA de la entidad {@link Trigger}.
     */
    @Autowired
    public SignalService(SignalRepository signalRepository, SignalMapper signalMapper,
            TriggerRepository triggerRepository) {
        this.signalRepository = signalRepository;
        this.signalMapper = signalMapper;
        this.triggerRepository = triggerRepository;
    }

    /**
     * Obtiene todas las señales guardadas en la base de datos.
     * 
     * @return una lista de objetos {@link SignalDetails} que representan todas las
     *         señales guardadas en la base de datos.
     */
    public List<SignalDetails> getAllSignals() {
        List<Signal> signals = signalRepository.findAll();
        List<SignalDetails> signalsDetails = new ArrayList<SignalDetails>();

        for (Signal signal : signals) {
            Trigger trigger = triggerRepository.getById(signal.getIdTrigger());
            SignalDetails signalDetails = signalMapper.toDetails(signal, trigger);
            signalsDetails.add(signalDetails);
        }

        return signalsDetails;
    }

    /**
     * Guarda una nueva entidad {@link Signal} en la base de datos.
     * 
     * @param idTrigger identificador del trigger de la señal.
     * @param name      nombre de la señal.
     * @return el objeto {@link Signal} guardado.
     */
    public Signal saveSignal(Long idTrigger, String name) {
        Signal signal = new Signal();
        signal.setName(name);
        signal.setIdTrigger(idTrigger);
        return signalRepository.save(signal);
    }

    /**
     * Elimina una entidad {@link Signal} de la base de datos por su ID.
     * 
     * @param id identificador de la señal a eliminar.
     */
    public void deleteSignal(Long id) {
        signalRepository.deleteById(id);
    }

    /**
     * Actualiza el nombre de una entidad {@link Signal} en la base de datos.
     * 
     * @param id   identificador de la señal a actualizar.
     * @param name nuevo nombre de la señal.
     */
    public void updateSignal(Long id, String name) {
        Optional<Signal> optionalSignal = findById(id);
        if (optionalSignal.isPresent()) {
            Signal signal = optionalSignal.get();
            signal.setName(name);
            signalRepository.save(signal);
        }
    }

    /**
     * Obtiene una entidad {@link Signal} por el identificador del trigger.
     * 
     * @param idTrigger    identificador del trigger de la señal.
     * @return un Optional que contiene la entidad {@link Signal} si se encuentra, o
     *         un Optional vacío si no.
     */
    public Optional<Signal> getSignalByTrigger(Long idTrigger) {
        return signalRepository.findByIdTrigger(idTrigger);
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
