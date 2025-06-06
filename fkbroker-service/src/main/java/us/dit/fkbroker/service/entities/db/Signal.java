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
package us.dit.fkbroker.service.entities.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entidad que representa los datos de un endpoint que atiende notificaciones
 * desde servidores FHIR
 * 
 * @author Isabel Román
 * @author josperbel - Clase movida de `us.dit.fkbroker.service.entities` a
 *         `us.dit.fkbroker.service.entities.db`
 * @version 1.1
 * @date Mar 2025
 */
@Entity
public class Signal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    @Column(name = "ID_TRIGGER")
    private Long idTrigger;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getIdTrigger() {
        return idTrigger;
    }

    public void setIdTrigger(Long idTrigger) {
        this.idTrigger = idTrigger;
    }

}
