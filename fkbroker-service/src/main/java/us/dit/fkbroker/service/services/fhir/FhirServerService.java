package us.dit.fkbroker.service.services.fhir;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.domain.FhirServerDetails;
import us.dit.fkbroker.service.repositories.FhirServerRepository;
import us.dit.fkbroker.service.services.mapper.FhirServerMapper;

/**
 * Servicio para gestionar las operaciones sobre los servidores FHIR.
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Service
public class FhirServerService {

    private final FhirServerRepository fhirServerRepository;
    private final FhirServerMapper fhirServerMapper;

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
    public FhirServerService(FhirServerRepository fhirServerRepository, FhirServerMapper fhirServerMapper) {
        this.fhirServerRepository = fhirServerRepository;
        this.fhirServerMapper = fhirServerMapper;
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
     * Obtiene todos los servidores FHIR guardadeos en la base de datos.
     * 
     * @return una lista de objetos {@link FhirServerDetails}.
     */
    public List<FhirServerDetails> getAllFhirServersDetails() {
        return getAllFhirServers().stream().map(fhirServerMapper::toDetails).collect(Collectors.toList());
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
     * @param serverDetails servidor FHIR a guardar.
     */
    public void saveFhirServer(FhirServerDetails serverDetails) {
        FhirServer server = fhirServerMapper.toEntity(serverDetails);
        server = fhirServerRepository.save(server);
    }

    /**
     * Elimina un servidor FHIR de la base de datos.
     * 
     * @param id identificador del servidor FHIR a eliminar.
     */
    public void deleteFhirServer(Long id) {
        fhirServerRepository.deleteById(id);
    }
}
