package us.dit.fkbroker.service.services.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Enumerations.SearchComparator;
import org.hl7.fhir.r5.model.Enumerations.SearchModifierCode;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.Subscription.SubscriptionFilterByComponent;
import org.hl7.fhir.r5.model.Subscription.SubscriptionPayloadContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.entities.domain.SubscriptionDetails;
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

    /**
     * Transforma una entidad {@link SubscriptionData} en un objeto de dominio
     * {@link SubscriptionEntry}
     * 
     * @param subscription entidad con datos de la subscripción que se desea
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
     * Transforma un objeto FHIR {@link Subscription} en el objeto de dominio
     * {@link SubscriptionDetails}
     * 
     * @param subscription objeto FHIR con datos de la subscripción que se desea
     *                     transformar en {@link SubscriptionDetails}.
     * @return el objeto {@link SubscriptionDetails}.
     */
    public SubscriptionDetails toDetails(Subscription subscription) {
        String endpoint = subscription.getEndpoint();
        String topicTitle = subscription.getTopic();
        String id = subscription.getId();

        // Mapea los filtros
        List<SubscriptionDetails.FilterDetail> filters = subscription.getFilterBy().stream()
                .map(filter -> new SubscriptionDetails.FilterDetail(filter.getFilterParameter(),
                        filter.getComparator() != null ? filter.getComparator().toCode() : null,
                        filter.getModifier() != null ? filter.getModifier().toCode() : null, filter.getValue()))
                .collect(Collectors.toList());

        return new SubscriptionDetails(endpoint, topicTitle, id, filters);
    }

    /**
     * Transforma un objeto de dominio {@link SubscriptionForm} en el objeto FHIR
     * {@link Subscription}
     * 
     * @param subscription objeto del dominio con datos de la subscripción que se
     *                     desea transformar en el objeto FHIR {@link Subscription}.
     * @param endpoint     cadena de texto con el endpoint de la subscripción.
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

}
