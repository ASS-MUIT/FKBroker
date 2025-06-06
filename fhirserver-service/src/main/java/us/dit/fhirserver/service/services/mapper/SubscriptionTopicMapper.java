package us.dit.fhirserver.service.services.mapper;

import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.hl7.fhir.r5.model.SubscriptionTopic.InteractionTrigger;
import org.hl7.fhir.r5.model.SubscriptionTopic.SubscriptionTopicResourceTriggerComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;
import us.dit.fhirserver.service.entities.domain.SubscriptionTopicDTO;

/**
 * Componente que transforma entidades, objetos del dominio y recursos FHIR
 * relacionados con los temas de las subscripciones.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Component
public class SubscriptionTopicMapper {

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    /**
     * Transforma un objeto un recurso FHIR {@link SubscriptionTopic} en una entidad
     * {@link SubscriptionTopicDB}
     * 
     * @param subscription recurso FHIR {@link SubscriptionTopic}.
     * @return la entidad {@link SubscriptionTopicDB}.
     */
    public SubscriptionTopicDB toEntity(SubscriptionTopic subscriptionTopic) {
        SubscriptionTopicDB subscriptionTopicDB = new SubscriptionTopicDB();

        subscriptionTopicDB.setName(subscriptionTopic.getTitle());
        subscriptionTopicDB.setResource(subscriptionTopic.getResourceTriggerFirstRep().getResource());
        subscriptionTopicDB.setOperation(
                subscriptionTopic.getResourceTriggerFirstRep().getSupportedInteraction().get(0).asStringValue());

        return subscriptionTopicDB;
    }

    /**
     * Transforma una entidad {@link subscriptionTopicDB} en un objeto del dominio
     * {@link SubscriptionTopicDTO}.
     * 
     * @param eventDB entidad {@link subscriptionTopicDB}.
     * @return el objeto del dominio {@link SubscriptionTopicDTO}.
     */
    public SubscriptionTopicDTO toDTO(SubscriptionTopicDB subscriptionTopicDB) {
        SubscriptionTopicDTO subscriptionTopicDTO = new SubscriptionTopicDTO();

        subscriptionTopicDTO.setId(subscriptionTopicDB.getId());
        subscriptionTopicDTO.setName(subscriptionTopicDB.getName());
        subscriptionTopicDTO.setOperation(subscriptionTopicDB.getOperation());
        subscriptionTopicDTO.setResource(subscriptionTopicDB.getResource());

        return subscriptionTopicDTO;
    }

    /**
     * Transforma una entidad {@link SubscriptionTopicDB} en un recurso FHIR
     * {@link SubscriptionTopic}.
     * 
     * @param eventDB entidad {@link SubscriptionTopicDB}.
     * @return el recurso FHIR {@link SubscriptionTopic}.
     */
    public SubscriptionTopic toFhir(SubscriptionTopicDB subscriptionTopicDB) {
        SubscriptionTopic subscriptionTopic = new SubscriptionTopic();

        // Valores guardados
        subscriptionTopic.setId(subscriptionTopicDB.getId().toString());
        subscriptionTopic.setTitle(subscriptionTopicDB.getName());
        subscriptionTopic.setDescription(subscriptionTopicDB.getName());

        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + subscriptionTopicDB.getId().toString();
        subscriptionTopic.setUrl(urlTopic);

        SubscriptionTopicResourceTriggerComponent triggerComponent = new SubscriptionTopicResourceTriggerComponent();
        triggerComponent.setDescription(subscriptionTopicDB.getName());
        triggerComponent.setResource(subscriptionTopicDB.getResource());
        triggerComponent.addSupportedInteraction(InteractionTrigger.fromCode(subscriptionTopicDB.getOperation()));
        subscriptionTopic.addResourceTrigger(triggerComponent);

        // Valores simulados
        subscriptionTopic.setStatus(PublicationStatus.ACTIVE);

        return subscriptionTopic;
    }
}
