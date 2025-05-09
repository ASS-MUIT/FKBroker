package us.dit.fkbroker.service.services.fhir;

import java.util.List;
import java.util.Optional;

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
     * Obtiene un servidor FHIR de la base de datos por su ID.
     * 
     * @param id el id del servidor FHIR a obtener.
     * @return el servidor FHIR obtenido.
     */
    public Optional<FhirServer> getFhirServer(Long id) {
        return fhirServerRepository.findById(id);
    }

    /**
     * Obtiene todos los servidores FHIR.
     * 
     * @return una lista de objetos {@link FhirServerDetails} que representan todos los
     *         servidores FHIR.
     */
    public List<FhirServer> getAllFhirServers() {
        return fhirServerRepository.findAll();
    }

    /**
     * Guarda un servidor FHIR en la base de datos.
     * 
     * @param dto el servidor FHIR a guardar.
     * @return el servidor FHIR guardado.
     */
    public void saveFhirServer(FhirServerDetails dto) {
        FhirServer server = fhirServerMapper.toEntity(dto);
        server = fhirServerRepository.save(server);
    }

    /**
     * Elimina un servidor FHIR de la base de datos por su ID.
     * 
     * @param id el id del servidor FHIR a eliminar.
     */
    public void deleteFhirServer(Long id) {
        fhirServerRepository.deleteById(id);
    }
}
