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

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Entidad que representa los datos de un servidor kie
 * 
 * @author Isabel Roman
 * @author josperbel - Clase movida de `us.dit.fkbroker.service.entities` a
 *         `us.dit.fkbroker.service.entities.db`
 * @version 1.1
 * @date Mar 2025
 */
@Entity
public class KieServer {

    public KieServer() {
        super();
    }

    public KieServer(String url, String usu, String pwd) {
        super();
        this.url = url;
        this.usu = usu;
        this.pwd = pwd;
    }

    @Id
    private String url;
    private String usu;
    private String pwd;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsu() {
        return usu;
    }

    public void setUsu(String usu) {
        this.usu = usu;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

}
