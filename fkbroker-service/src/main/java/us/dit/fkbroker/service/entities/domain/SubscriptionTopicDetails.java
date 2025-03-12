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
package us.dit.fkbroker.service.entities.domain;

import java.util.List;

/**
 * Entidad con detalles de los SubscriptionTopic
 * 
 * @author juanmabrazo98
 * @author josperbel - Clase movida de `us.dit.fkbroker.service.services.fhir` a
 *         `us.dit.fkbroker.service.entities.domain`
 * @version 1.1
 * @date Mar 2025
 */
public class SubscriptionTopicDetails {
    private String name;
    private String url;
    private String id;
    private String resource;
    private String interaction;
    private List<FilterDetail> filters;

    public SubscriptionTopicDetails(String name, String id, String url, String resource, String interaction,
            List<FilterDetail> filters) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.resource = resource;
        this.interaction = interaction;
        this.filters = filters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getInteraction() {
        return interaction;
    }

    public void setInteraction(String interaction) {
        this.interaction = interaction;
    }

    public List<FilterDetail> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterDetail> filters) {
        this.filters = filters;
    }

    public static class FilterDetail {
        private String description;
        private String filterParameter;
        private List<String> comparators;
        private List<String> modifiers;

        public FilterDetail(String description, String filterParameter, List<String> comparators,
                List<String> modifiers) {
            this.description = description;
            this.filterParameter = filterParameter;
            this.comparators = comparators;
            this.modifiers = modifiers;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getFilterParameter() {
            return filterParameter;
        }

        public void setFilterParameter(String filterParameter) {
            this.filterParameter = filterParameter;
        }

        public List<String> getComparators() {
            return comparators;
        }

        public void setComparators(List<String> comparators) {
            this.comparators = comparators;
        }

        public List<String> getModifiers() {
            return modifiers;
        }

        public void setModifiers(List<String> modifiers) {
            this.modifiers = modifiers;
        }
    }
}