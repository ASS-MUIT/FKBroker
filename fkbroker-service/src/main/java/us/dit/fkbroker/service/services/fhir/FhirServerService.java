package us.dit.fkbroker.service.services.fhir;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.repositories.FhirServerRepository;

/**
 * Servicio para gestionar las operaciones sobre los servidores FHIR.
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Service
public class FhirServerService {

    private final SubscriptionService subscriptionService;
    private final SubscriptionTopicService subscriptionTopicService;
    private final FhirServerRepository fhirServerRepository;

    /**
     * Constructor que inyecta el repositorio {@link FhirServerRepository} y el
     * componente {@link FhirServerMapper}.
     * 
     * @param fhirServerRepository repositorio JPA de la entidad {@link FhirServer}
     * @param fhirServerMapper     componente que transforma objetos
     *                             {@link FhirServerDetails} en {@link FhirServer} y
     *                             viceversa.
     */
    @Autowired
    public FhirServerService(SubscriptionService subscriptionService, SubscriptionTopicService subscriptionTopicService,
            FhirServerRepository fhirServerRepository) {
        this.subscriptionService = subscriptionService;
        this.subscriptionTopicService = subscriptionTopicService;
        this.fhirServerRepository = fhirServerRepository;
    }

    /**
     * Obtiene un servidor FHIR de la base de datos por su identificador. Si no
     * existe ningún servidor con ese identificador lanza una excepción.
     * 
     * @param id identificador del servidor FHIR a obtener.
     * @return el servidor FHIR obtenido.
     * @throws RuntimeException si no encuentra el servidor FHIR.
     */
    public FhirServer getFhirServer(Long id) {
        Optional<FhirServer> optionalServer = fhirServerRepository.findById(id);

        if (optionalServer.isPresent()) {
            return optionalServer.get();
        } else {
            throw new RuntimeException("FhirServer not found with id: " + id);
        }
    }

    /**
     * Obtiene todos los servidores FHIR guardadeos en la base de datos.
     * 
     * @return una lista de objetos {@link FhirServer}.
     */
    public List<FhirServer> getAllFhirServers() {
        return fhirServerRepository.findAll();
    }

    /**
     * Obtiene todos los servidores FHIR guardadeos en la base de datos con el campo
     * Heartbeat a TRUE
     * 
     * @return una lista de objetos {@link FhirServer}.
     */
    public List<FhirServer> getFhirServersWithHeartbeat() {
        return fhirServerRepository.findByHeartbeat(true);
    }

    /**
     * Guarda un servidor FHIR en la base de datos.
     * 
     * @param server servidor FHIR a guardar.
     */
    public void saveFhirServer(FhirServer server) {
        fhirServerRepository.save(server);
    }

    /**
     * Elimina un servidor FHIR de la base de datos. También elimina todas las
     * subscripciones del servidor FHIR y de la base de datos y todos los temas de
     * subscripción de la base de datos.
     * 
     * @param id identificador del servidor FHIR a eliminar.
     */
    @Transactional
    public void deleteFhirServer(Long id) {
        // Obtiene el servidor a eliminar
        FhirServer fhirServer = fhirServerRepository.getById(id);

        // Obtiene todas las subscripciones del servidor a eliminar
        List<SubscriptionData> subscriptionDatas = subscriptionService.getSubscriptions(id);

        // Elimina todas las subscripciones del servidor FHIR y de la base de datos
        for (SubscriptionData subscriptionData : subscriptionDatas) {
            subscriptionService.deleteSubscription(fhirServer, subscriptionData.getIdSubscription());
        }

        // Elimina todos los temas de subscripción de la base de datos
        subscriptionTopicService.deleteSubscriptionTopics(fhirServer);

        // Elimina los datos del servidor de la base de datos
        fhirServerRepository.deleteById(id);
    }
}
