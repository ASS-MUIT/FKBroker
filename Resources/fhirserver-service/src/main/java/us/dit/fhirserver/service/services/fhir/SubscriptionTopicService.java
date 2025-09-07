package us.dit.fhirserver.service.services.fhir;

import java.util.List;

import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import us.dit.fhirserver.service.entities.db.Topic;
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
     *                                    {@link Topic}.
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

        Topic topic = subscriptionTopicMapper.toEntity(subscriptionTopic);
        topic = subscriptionTopicRepository.save(topic);

        subscriptionTopic = subscriptionTopicMapper.toFhir(topic);

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
        Topic topic = subscriptionTopicRepository.getById(idTopic);
        SubscriptionTopic subscriptionTopic = subscriptionTopicMapper.toFhir(topic);
        return fhirContext.newJsonParser().encodeResourceToString(subscriptionTopic);
    }

    /**
     * Obtiene todos los temas de subscripción de la base de datos del servidor,
     * creando el recurso FHIR {@link Bundle} correspondiente.
     * 
     * @return información de todos los temas de subscripción.
     */
    public String getTopics() {
        List<Topic> topics = subscriptionTopicRepository.findAll();

        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.SEARCHSET);

        for (Topic topic : topics) {
            SubscriptionTopic subscriptionTopic = subscriptionTopicMapper.toFhir(topic);

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
     * creando un listado de objetos de dominio {@link Topic} con la información
     * correspondiente.
     * 
     * @return información de todos los temas de subscripción.
     */
    public List<Topic> getTopicsDTO() {
        return subscriptionTopicRepository.findAll();
    }

    /**
     * Obtiene un tema de subscripción de la base de datos del servidor, creando un
     * objeto de dominio {@link Topic} con la información correspondiente.
     * 
     * @param idTopic identificador del tema de subscripción a obtener.
     * @return información del tema de subscripción solicitado.
     */
    public Topic getTopicDTO(Long idTopic) {
        return subscriptionTopicRepository.getById(idTopic);
    }

}
