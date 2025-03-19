package us.dit.fkbroker.service.services.fhir;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.domain.FhirServerDTO;
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
     *                             {@link FhirServerDTO} en {@link FhirServer} y
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
    public Optional<FhirServerDTO> getFhirServer(Long id) {
        Optional<FhirServer> fhirServer = fhirServerRepository.findById(id);
        return fhirServer.map(entity -> new FhirServerDTO(entity.getId(), entity.getUrl()));
    }

    /**
     * Obtiene todos los servidores FHIR.
     * 
     * @return una lista de objetos {@link FhirServerDTO} que representan todos los
     *         servidores FHIR.
     */
    public List<FhirServerDTO> getAllFhirServers() {
        return fhirServerRepository.findAll().stream().map(fhirServerMapper::toDTO).collect(Collectors.toList());
    }

    /**
     * Guarda un servidor FHIR en la base de datos.
     * 
     * @param dto el servidor FHIR a guardar.
     * @return el servidor FHIR guardado.
     */
    public FhirServerDTO saveFhirServer(FhirServerDTO dto) {
        FhirServer server = fhirServerMapper.toEntity(dto);
        server = fhirServerRepository.save(server);
        return fhirServerMapper.toDTO(server);
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
