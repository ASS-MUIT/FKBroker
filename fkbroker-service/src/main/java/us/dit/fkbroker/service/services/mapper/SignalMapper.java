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
package us.dit.fkbroker.service.services.mapper;

import org.springframework.stereotype.Component;

import us.dit.fkbroker.service.entities.db.Signal;
import us.dit.fkbroker.service.entities.db.Trigger;
import us.dit.fkbroker.service.entities.domain.SignalDetails;

/**
 * Componente que transforma entidades en objetos del dominio y viceversa.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Component
public class SignalMapper {

    /**
     * Transforma un objeto {@link Signal} y {@link Trigger} en uno
     * {@link SignalDetails}
     * 
     * @param signal  entidad con datos de la señal que se desea transformar en
     *                {@link SignalDetails}.
     * @param trigger entidad con datos del trigger que se desea transformar en
     *                {@link SignalDetails}.
     * @return el objeto {@link SignalDetails}.
     */
    public SignalDetails toDetails(Signal signal, Trigger trigger) {
        SignalDetails signalDetails = new SignalDetails();
        signalDetails.setId(signal.getId());
        signalDetails.setResource(trigger.getResource());
        signalDetails.setInteraction(trigger.getInteraction());
        signalDetails.setName(signal.getName());
        return signalDetails;
    }

}
