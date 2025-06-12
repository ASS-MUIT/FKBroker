package us.dit.fhirserver.service.services.fhir;

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
import us.dit.fhirserver.service.services.mapper.SubscriptionTopicMapper;

/**
 * Servicio para gestionar las operaciones sobre los temas de las subscripciones
 * FHIR.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Service
public class SubscriptionTopicService {

    private final FhirContext fhirContext;
    private final SubscriptionTopicRepository subscriptionTopicRepository;
    private final SubscriptionTopicMapper subscriptionTopicMapper;

    /**
     * Constructor que inyecta el repositorio {@link SubscriptionTopicRepository} y
     * los componentes {@link SubscriptionTopicMapper} y {@link FhirContext}.
     * 
     * @param fhirContext                 componente que contiene el contexto FHIR.
     * @param subscriptionTopicRepository repositorio JPA de la entidad
     *                                    {@link SubscriptionTopicDB}.
     * @param subscriptionTopicMapper     componente que transforma entidades,
     *                                    objetos del dominio y recursos FHIR
     *                                    relacionados con los temas de
     *                                    subscripción.
     */
    @Autowired
    public SubscriptionTopicService(FhirContext fhirContext, SubscriptionTopicRepository subscriptionTopicRepository,
            SubscriptionTopicMapper subscriptionTopicMapper) {
        this.fhirContext = fhirContext;
        this.subscriptionTopicRepository = subscriptionTopicRepository;
        this.subscriptionTopicMapper = subscriptionTopicMapper;
    }

    /**
     * Crea un nuevo tema de subscripción en la base de datos del servidor en
     * función de la información recibida.
     * 
     * @param message mensaje recibido con la información del tema de subscripción.
     * @return información del tema de subscripción que se acaba de crear.
     */
    public String saveTopic(String message) {
        SubscriptionTopic subscriptionTopic = fhirContext.newJsonParser().parseResource(SubscriptionTopic.class,
                message);

        SubscriptionTopicDB subscriptionTopicDB = subscriptionTopicMapper.toEntity(subscriptionTopic);
        subscriptionTopicDB = subscriptionTopicRepository.save(subscriptionTopicDB);

        subscriptionTopic = subscriptionTopicMapper.toFhir(subscriptionTopicDB);

        return fhirContext.newJsonParser().encodeResourceToString(subscriptionTopic);
    }

    /**
     * Obtiene un tema de subscripción de la base de datos del servidor, creando el
     * recurso FHIR {@link SubscriptionTopic} correspondiente.
     * 
     * @param idTopic identificador del tema de subscripción a obtener.
     * @return información del tema de subscripción solicitado.
     */
    public String getTopic(Long idTopic) {
        SubscriptionTopicDB subscriptionTopicDB = subscriptionTopicRepository.getById(idTopic);
        SubscriptionTopic subscriptionTopic = subscriptionTopicMapper.toFhir(subscriptionTopicDB);
        return fhirContext.newJsonParser().encodeResourceToString(subscriptionTopic);
    }

    /**
     * Obtiene todos los temas de subscripción de la base de datos del servidor,
     * creando el recurso FHIR {@link Bundle} correspondiente.
     * 
     * @return información de todos los temas de subscripción.
     */
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

    /**
     * Elimina un tema de subscripción de la base de datos del servidor.
     * 
     * @param idTopic identificador del tema de subscripción a eliminar.
     * @return información del tema de subscripción eliminado.
     */
    public String deleteTopic(Long idTopic) {
        subscriptionTopicRepository.deleteById(idTopic);

        OperationOutcome response = new OperationOutcome();

        return fhirContext.newJsonParser().encodeResourceToString(response);
    }

    /**
     * Obtiene todos los temas de subscripción de la base de datos del servidor,
     * creando un listado de objetos de dominio {@link SubscriptionTopicDTO} con la
     * información correspondiente.
     * 
     * @return información de todos los temas de subscripción.
     */
    public List<SubscriptionTopicDTO> getTopicsDTO() {
        List<SubscriptionTopicDB> subscriptionTopicDBs = subscriptionTopicRepository.findAll();

        List<SubscriptionTopicDTO> subscriptionTopicDTOs = subscriptionTopicDBs.stream()
                .map(subscriptionTopicMapper::toDTO).collect(Collectors.toList());

        return subscriptionTopicDTOs;
    }

    /**
     * Obtiene un tema de subscripción de la base de datos del servidor, creando un
     * objeto de dominio {@link SubscriptionTopicDTO} con la información
     * correspondiente.
     * 
     * @param idTopic identificador del tema de subscripción a obtener.
     * @return información del tema de subscripción solicitado.
     */
    public SubscriptionTopicDTO getTopicDTO(Long idTopic) {
        SubscriptionTopicDB subscriptionTopicDB = subscriptionTopicRepository.getById(idTopic);

        return subscriptionTopicMapper.toDTO(subscriptionTopicDB);
    }

}
