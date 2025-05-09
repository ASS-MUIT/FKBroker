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

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad con detalles del formulario de suscripción
 * 
 * @author juanmabrazo98
 * @author josperbel - Clase movida de `us.dit.fkbroker.service.services.fhir` a
 *         `us.dit.fkbroker.service.entities.domain`
 * @version 1.1
 * @date Mar 2025
 */
public class SubscriptionForm {

    private String topicUrl;
    private String payload;
    private String resource;
    private String interaction;
    private List<FilterDetail> filters;

    public SubscriptionForm() {

    }

    public SubscriptionForm(SubscriptionTopicDetails topic) {
        topicUrl = topic.getUrl();
        resource = topic.getResource();
        interaction = topic.getInteraction();

        filters = new ArrayList<SubscriptionForm.FilterDetail>();

        for (SubscriptionTopicDetails.FilterDetail filterDetail : topic.getFilters()) {
            FilterDetail filter = new FilterDetail();
            filter.setActive(false);
            filter.setParameter(filterDetail.getFilterParameter());
            filters.add(filter);
        }
    }

    public String getTopicUrl() {
        return topicUrl;
    }

    public void setTopicUrl(String topicUrl) {
        this.topicUrl = topicUrl;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
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

    // Clase interna FilterDetail
    public static class FilterDetail {

        private String parameter;
        private String comparator;
        private String modifier;
        private String value;
        private Boolean active;

        public String getParameter() {
            return parameter;
        }

        public void setParameter(String parameter) {
            this.parameter = parameter;
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

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }
}