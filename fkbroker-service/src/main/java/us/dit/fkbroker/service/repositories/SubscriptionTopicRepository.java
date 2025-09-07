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
package us.dit.fkbroker.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.Topic;

/**
 * Repositorio JPA de la entidad {@link Topic}
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
public interface SubscriptionTopicRepository extends JpaRepository<Topic, Long> {
    void deleteAllByServer(FhirServer server);
    
    List<Topic> findByServer(FhirServer server);

    Topic findByIdTopicAndServer(String idTopic, FhirServer server);
}
