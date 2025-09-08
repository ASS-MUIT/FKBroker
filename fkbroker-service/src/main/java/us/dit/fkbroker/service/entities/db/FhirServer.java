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
package us.dit.fkbroker.service.entities.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entidad que representa los datos de configuración de un servidor fhir
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Entity(name = "FHIR_SERVERS")
public class FhirServer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "URL")
    private String url;
    @Column(name = "HEARTBEAT")
    private Boolean heartbeat;
    @Column(name = "QUERY_OPERATIONS")
    private Boolean queryOperations;

    public FhirServer() {
        super();
    }

    public FhirServer(Long id, String name, String url, Boolean heartbeat, Boolean queryOperations) {
        super();
        this.id = id;
        this.name = name;
        this.url = url;
        // Comprueba si es null, en ese caso guarda false
        this.heartbeat = (heartbeat != null) ? heartbeat : false;
        this.queryOperations = (queryOperations != null) ? queryOperations : false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Boolean heartbeat) {
        // Comprueba si es null, en ese caso guarda false
        this.heartbeat = (heartbeat != null) ? heartbeat : false;
    }

    public Boolean getQueryOperations() {
        return queryOperations;
    }

    public void setQueryOperations(Boolean queryOperations) {
        this.queryOperations = queryOperations;
    }

}
