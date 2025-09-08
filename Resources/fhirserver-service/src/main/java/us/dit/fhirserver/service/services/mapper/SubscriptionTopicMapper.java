package us.dit.fhirserver.service.services.mapper;

import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.hl7.fhir.r5.model.SubscriptionTopic.InteractionTrigger;
import org.hl7.fhir.r5.model.SubscriptionTopic.SubscriptionTopicResourceTriggerComponent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import us.dit.fhirserver.service.entities.db.Topic;

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
     * {@link Topic}
     * 
     * @param subscription recurso FHIR {@link SubscriptionTopic}.
     * @return la entidad {@link Topic}.
     */
    public Topic toEntity(SubscriptionTopic subscriptionTopic) {
        Topic topic = new Topic();

        topic.setName(subscriptionTopic.getTitle());
        topic.setResource(subscriptionTopic.getResourceTriggerFirstRep().getResource());
        topic.setOperation(
                subscriptionTopic.getResourceTriggerFirstRep().getSupportedInteraction().get(0).asStringValue());

        return topic;
    }

    /**
     * Transforma una entidad {@link Topic} en un recurso FHIR
     * {@link SubscriptionTopic}.
     * 
     * @param eventDB entidad {@link Topic}.
     * @return el recurso FHIR {@link SubscriptionTopic}.
     */
    public SubscriptionTopic toFhir(Topic topic) {
        SubscriptionTopic subscriptionTopic = new SubscriptionTopic();

        // Valores guardados
        subscriptionTopic.setId(topic.getId().toString());
        subscriptionTopic.setTitle(topic.getName());
        subscriptionTopic.setDescription(topic.getName());

        String urlTopic = fhirServerUrl + "/SubscriptionTopic/" + topic.getId().toString();
        subscriptionTopic.setUrl(urlTopic);

        SubscriptionTopicResourceTriggerComponent triggerComponent = new SubscriptionTopicResourceTriggerComponent();
        triggerComponent.setDescription(topic.getName());
        triggerComponent.setResource(topic.getResource());
        triggerComponent.addSupportedInteraction(InteractionTrigger.fromCode(topic.getOperation()));
        subscriptionTopic.addResourceTrigger(triggerComponent);

        // Valores simulados
        subscriptionTopic.setStatus(PublicationStatus.ACTIVE);

        return subscriptionTopic;
    }
}
