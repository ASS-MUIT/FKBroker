package us.dit.fhirserver.service.services;

import java.util.List;
import java.util.stream.Collectors;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;
import us.dit.fhirserver.service.entities.domain.SubscriptionTopicDTO;
import us.dit.fhirserver.service.repositories.SubscriptionTopicRepository;

@Service
public class SubscriptionTopicService {

    private final FhirContext fhirContext;
    private final SubscriptionTopicRepository subscriptionTopicRepository;
    private final SubscriptionTopicMapper subscriptionTopicMapper;

    @Autowired
    public SubscriptionTopicService(FhirContext fhirContext, SubscriptionTopicRepository subscriptionTopicRepository,
            SubscriptionTopicMapper subscriptionTopicMapper) {
        this.fhirContext = fhirContext;
        this.subscriptionTopicRepository = subscriptionTopicRepository;
        this.subscriptionTopicMapper = subscriptionTopicMapper;
    }

    public String saveTopic(String message) {
        SubscriptionTopic subscriptionTopic = fhirContext.newJsonParser().parseResource(SubscriptionTopic.class,
                message);

        SubscriptionTopicDB subscriptionTopicDB = subscriptionTopicMapper.toEntity(subscriptionTopic);
        subscriptionTopicDB = subscriptionTopicRepository.save(subscriptionTopicDB);

        subscriptionTopic = subscriptionTopicMapper.toFhir(subscriptionTopicDB);

        return fhirContext.newJsonParser().encodeResourceToString(subscriptionTopic);
    }

    public String getTopic(Long idTopic) {
        SubscriptionTopicDB subscriptionTopicDB = subscriptionTopicRepository.getById(idTopic);
        SubscriptionTopic subscriptionTopic = subscriptionTopicMapper.toFhir(subscriptionTopicDB);
        return fhirContext.newJsonParser().encodeResourceToString(subscriptionTopic);
    }

    public String getTopics() {
        List<SubscriptionTopicDB> subscriptionTopicDBs = subscriptionTopicRepository.findAll();

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);

        for (SubscriptionTopicDB subscriptionTopicDB : subscriptionTopicDBs) {
            SubscriptionTopic subscriptionTopic = subscriptionTopicMapper.toFhir(subscriptionTopicDB);

            BundleEntryComponent entry = new BundleEntryComponent();
            entry.setResource(subscriptionTopic);
            bundle.addEntry(entry);
        }

        return fhirContext.newJsonParser().encodeResourceToString(bundle);
    }

    public String deleteTopic(Long idTopic) {
        subscriptionTopicRepository.deleteById(idTopic);

        OperationOutcome response = new OperationOutcome();

        return fhirContext.newJsonParser().encodeResourceToString(response);
    }

    public List<SubscriptionTopicDTO> getTopicsDTO() {
        List<SubscriptionTopicDB> subscriptionTopicDBs = subscriptionTopicRepository.findAll();

        List<SubscriptionTopicDTO> subscriptionTopicDTOs = subscriptionTopicDBs.stream()
                .map(subscriptionTopicMapper::toDTO).collect(Collectors.toList());

        return subscriptionTopicDTOs;
    }

    public SubscriptionTopicDTO getTopicDTO(Long idTopic) {
        SubscriptionTopicDB subscriptionTopicDB = subscriptionTopicRepository.getById(idTopic);

        return subscriptionTopicMapper.toDTO(subscriptionTopicDB);
    }

}
