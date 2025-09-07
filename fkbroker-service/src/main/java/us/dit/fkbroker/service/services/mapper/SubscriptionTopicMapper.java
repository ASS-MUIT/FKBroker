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

import java.util.stream.Collectors;

import org.hl7.fhir.r5.model.Enumeration;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.parser.IParser;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicDetails;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicDetails.FilterDetail;
import us.dit.fkbroker.service.entities.domain.SubscriptionTopicEntry;

/**
 * Componente que transforma objetos FHIR en entidades.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Component
public class SubscriptionTopicMapper {

    private final IParser jsonParser;

    /**
     * Constructor que inyecta {@link IParser}.
     * 
     * @param jsonParser
     */
    @Autowired
    public SubscriptionTopicMapper(IParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    /**
     * Transforma un objeto FHIR {@link SubscriptionTopic} en el objeto de dominio
     * {@link SubscriptionTopicEntry}
     * 
     * @param subscriptionTopic objeto FHIR con datos del tema de la subscripción
     *                          que se desea transformar en el objeto de dominio
     *                          {@link SubscriptionTopicEntry}.
     * @return el objeto {@link SubscriptionTopicEntry}.
     */
    public SubscriptionTopicEntry toEntry(SubscriptionTopic subscriptionTopic) {
        SubscriptionTopicEntry subscriptionTopicEntry = new SubscriptionTopicEntry();

        subscriptionTopicEntry.setId(subscriptionTopic.getIdElement().getIdPart());
        subscriptionTopicEntry.setName(subscriptionTopic.getTitle());

        // Para esta implementación se está suponiendo que los Topics tendrán
        // configurado un único ResourceTrigger con una única SupportedInteraction
        subscriptionTopicEntry.setResource(subscriptionTopic.getResourceTriggerFirstRep().getResource());
        subscriptionTopicEntry.setInteraction(
                subscriptionTopic.getResourceTriggerFirstRep().getSupportedInteraction().get(0).asStringValue());

        return subscriptionTopicEntry;
    }

    /**
     * Transforma un objeto FHIR {@link SubscriptionTopic} en el objeto de dominio
     * {@link SubscriptionTopicDetails}
     * 
     * @param subscriptionTopic objeto FHIR con datos del tema de la subscripción
     *                          que se desea transformar en el objeto de dominio
     *                          {@link SubscriptionTopicDetails}.
     * @return el objeto {@link SubscriptionTopicDetails}.
     */
    public SubscriptionTopicDetails toDetails(SubscriptionTopic subscriptionTopic) {
        SubscriptionTopicDetails subscriptionTopicDetails = new SubscriptionTopicDetails();

        subscriptionTopicDetails.setId(subscriptionTopic.getIdElement().getIdPart());
        subscriptionTopicDetails.setName(subscriptionTopic.getTitle());
        subscriptionTopicDetails.setUrl(subscriptionTopic.getUrl());

        // Para esta implementación se está suponiendo que los Topics tendrán
        // configurado un único ResourceTrigger con una única SupportedInteraction
        subscriptionTopicDetails.setResource(subscriptionTopic.getResourceTriggerFirstRep().getResource());
        subscriptionTopicDetails.setInteraction(
                subscriptionTopic.getResourceTriggerFirstRep().getSupportedInteraction().get(0).asStringValue());

        // Mapea todos los filtros
        subscriptionTopicDetails.setFilters(subscriptionTopic.getCanFilterBy().stream().map(filterComponent -> {
            FilterDetail filter = new FilterDetail();
            filter.setDescription(filterComponent.getDescription());
            filter.setFilterParameter(filterComponent.getFilterParameter());
            filter.setComparators(
                    filterComponent.getComparator().stream().map(Enumeration::getCode).collect(Collectors.toList()));
            filter.setModifiers(
                    filterComponent.getModifier().stream().map(Enumeration::getCode).collect(Collectors.toList()));
            return filter;
        }).collect(Collectors.toList()));

        return subscriptionTopicDetails;
    }

    /**
     * Transforma un objeto FHIR {@link SubscriptionTopic} en una cadena de texto.
     * 
     * @param subscriptionTopic objeto FHIR con los datos del tema de subscripción.
     * @return la cadena de texto con los datos del tema de subscripción.
     */
    public String toString(SubscriptionTopic subscriptionTopic) {
        return jsonParser.setPrettyPrint(true).encodeResourceToString(subscriptionTopic);
    }

}
