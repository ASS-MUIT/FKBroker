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
/*package us.dit.consentimientos.service.services.fhir;

public class SubscriptionDetails {
    private String endpoint;
    private String topic;
    private String id;

    // Constructor, getters y setters
    public SubscriptionDetails(String endpoint, String topic, String id) {
        this.endpoint = endpoint;
        this.topic = topic;
        this.id = id;
    }

    // Getters y Setters
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}*/
package us.dit.fkbroker.service.entities.domain;

import java.util.List;

/**
 * Entidad con detalle de suscripciones
 * 
 * @author juanmabrazo98
 * @author josperbel - Clase movida de `us.dit.fkbroker.service.services.fhir` a
 *         `us.dit.fkbroker.service.entities.domain`
 * @version 1.1
 * @date Mar 2025
 */
public class SubscriptionDetails {
    private String endpoint;
    private String topic;
    private String id;
    private List<FilterDetail> filters;

    // Constructor, getters y setters
    public SubscriptionDetails(String endpoint, String topic, String id, List<FilterDetail> filters) {
        this.endpoint = endpoint;
        this.topic = topic;
        this.id = id;
        this.filters = filters;
    }

    // Getters y Setters
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FilterDetail> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterDetail> filters) {
        this.filters = filters;
    }

    // Clase interna FilterDetail
    public static class FilterDetail {
        private String filterParameter;
        private String comparator;
        private String modifier;
        private String value;

        public FilterDetail(String filterParameter, String comparator, String modifier, String value) {
            this.filterParameter = filterParameter;
            this.comparator = comparator;
            this.modifier = modifier;
            this.value = value;
        }

        public String getFilterParameter() {
            return filterParameter;
        }

        public void setFilterParameter(String filterParameter) {
            this.filterParameter = filterParameter;
        }

        public String getComparator() {
            return comparator;
        }

        public void setComparator(String comparator) {
            this.comparator = comparator;
        }

        public String getModifier() {
            return modifier;
        }

        public void setModifier(String modifier) {
            this.modifier = modifier;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
