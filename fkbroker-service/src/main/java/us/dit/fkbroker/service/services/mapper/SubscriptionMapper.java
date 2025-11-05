/**
*  This file is part of FKBroker - Broker sending signals to KIEServers from FHIR notifications.
*  Copyright (C) 2024  Universidad de Sevilla/Departamento de IngenierÃ­a TelemÃ¡tica
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

import java.util.Collections;

import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Enumerations.SearchComparator;
import org.hl7.fhir.r5.model.Enumerations.SearchModifierCode;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.Subscription.SubscriptionFilterByComponent;
import org.hl7.fhir.r5.model.Subscription.SubscriptionPayloadContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.parser.IParser;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.entities.domain.SubscriptionEntry;
import us.dit.fkbroker.service.entities.domain.SubscriptionForm;

/**
 * Componente que transforma entidades, objetos FHIR y objetos del dominio.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Component
public class SubscriptionMapper {

    @Value("${fhir.subscription.heartbeat.period}")
    private Integer heartbeatPeriod;
    
    private final IParser jsonParser;

    /**
     * Constructor que inyecta {@link IParser}.
     * 
     * @param jsonParser
     */
    @Autowired
    public SubscriptionMapper(IParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    /**
     * Transforma una entidad {@link SubscriptionData} en un objeto de dominio
     * {@link SubscriptionEntry}
     * 
     * @param subscription entidad con datos de la subscripciÃ³n que se desea
     *                     transformar en {@link SubscriptionEntry}.
     * @return el objeto {@link SubscriptionEntry}.
     */
    public SubscriptionEntry toEntry(SubscriptionData subscription) {
        SubscriptionEntry subscriptionEntry = new SubscriptionEntry();

        subscriptionEntry.setId(subscription.getIdSubscription());
        subscriptionEntry.setIdTopic(subscription.getTopic().getIdTopic());
        subscriptionEntry.setEvents(subscription.getEvents());
        subscriptionEntry.setStatus(subscription.getStatus());
        subscriptionEntry.setLastUpdate(subscription.getUpdated());

        return subscriptionEntry;
    }

    /**
     * Transforma un objeto de dominio {@link SubscriptionForm} en el objeto FHIR
     * {@link Subscription}
     * 
     * @param subscription objeto del dominio con datos de la subscripciÃ³n que se
     *                     desea transformar en el objeto FHIR {@link Subscription}.
     * @param endpoint     cadena de texto con el endpoint de la subscripciÃ³n.
     * @return el objeto {@link Subscription}.
     */
    public Subscription toSubscription(SubscriptionForm subscriptionForm, String endpoint) {
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatusCodes.REQUESTED);
        subscription.setTopic(subscriptionForm.getUrlTopic());

        Coding coding = new Coding();
        coding.setCode("rest-hook");
        subscription.setChannelType(coding);

        subscription.setEndpoint(endpoint);
        subscription.setHeartbeatPeriod(heartbeatPeriod);
        subscription.setContent(SubscriptionPayloadContent.fromCode(subscriptionForm.getPayload()));
        subscription.setContentType("application/fhir+json");

        // Mapea los filtros, en el caso de que tenga
        if (subscriptionForm.getFilters() != null) {
            SubscriptionFilterByComponent filterBy = new SubscriptionFilterByComponent();
            subscriptionForm.getFilters().stream().filter(filter -> filter.getActive()).forEach(filter -> {
                filterBy.setFilterParameter(filter.getParameter());

                if (filter.getComparator() != null && !filter.getComparator().isEmpty()) {
                    filterBy.setComparator(SearchComparator.fromCode(filter.getComparator()));
                }

                if (filter.getModifier() != null && !filter.getModifier().isEmpty()) {
                    filterBy.setModifier(SearchModifierCode.fromCode(filter.getModifier()));
                }

                filterBy.setValue(filter.getValue());
            });
            subscription.setFilterBy(Collections.singletonList(filterBy));
        }
        return subscription;
    }
    
    /**
     * Transforma un objeto FHIR {@link Subscription} en una cadena de texto.
     * 
     * @param subscription objeto FHIR con los datos de la subscripciÃ³n.
     * @return la cadena de texto con los datos de la subscripciÃ³n.
     */
    public String toString(Subscription subscription) {
        return jsonParser.setPrettyPrint(true).encodeResourceToString(subscription);
    }

}
