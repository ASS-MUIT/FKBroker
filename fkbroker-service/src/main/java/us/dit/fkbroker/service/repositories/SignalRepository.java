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
package us.dit.fkbroker.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import us.dit.fkbroker.service.entities.db.Signal;

/**
 * https://spring.io/guides/gs/accessing-data-rest
 * 
 * @author Isabel Román
 * @author josperbel - Nueva ubicación de entidades
 * @version 1.1
 * @date Mar 2025
 */
public interface SignalRepository extends JpaRepository<Signal, Long> {
    Optional<Signal> findByIdTrigger(Long idTrigger);
}
