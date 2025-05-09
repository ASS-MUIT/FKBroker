package us.dit.fhirserver.service.services;

import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.hl7.fhir.r5.model.SubscriptionTopic.InteractionTrigger;
import org.hl7.fhir.r5.model.SubscriptionTopic.SubscriptionTopicResourceTriggerComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;
import us.dit.fhirserver.service.entities.domain.SubscriptionTopicDTO;

@Component
public class SubscriptionTopicMapper {

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    public SubscriptionTopicDB toEntity(SubscriptionTopic subscriptionTopic) {
        SubscriptionTopicDB subscriptionTopicDB = new SubscriptionTopicDB();

        subscriptionTopicDB.setName(subscriptionTopic.getTitle());
        subscriptionTopicDB.setResource(subscriptionTopic.getResourceTriggerFirstRep().getResource());
        subscriptionTopicDB.setOperation(
                subscriptionTopic.getResourceTriggerFirstRep().getSupportedInteraction().get(0).asStringValue());

        return subscriptionTopicDB;
    }

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

    public SubscriptionTopicDTO toDTO(SubscriptionTopicDB subscriptionTopicDB) {
        SubscriptionTopicDTO subscriptionTopicDTO = new SubscriptionTopicDTO();

        subscriptionTopicDTO.setId(subscriptionTopicDB.getId());
        subscriptionTopicDTO.setName(subscriptionTopicDB.getName());
        subscriptionTopicDTO.setOperation(subscriptionTopicDB.getOperation());
        subscriptionTopicDTO.setResource(subscriptionTopicDB.getResource());

        return subscriptionTopicDTO;
    }
}
