package us.dit.fkbroker.service.services.mapper;

import org.springframework.stereotype.Component;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.domain.FhirServerDTO;

/**
 * Componente que transforma objetos {@link FhirServerDTO} en {@link FhirServer}
 * y viceversa.
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Component
public class FhirServerMapper {

    /**
     * Transforma un objeto {@link FhirServer} en {@link FhirServerDTO}
     * 
     * @param server el objeto {@link FhirServer} que se desea transformar en
     *               {@link FhirServerDTO}.
     * @return el objeto {@link FhirServerDTO}.
     */
    public FhirServerDTO toDTO(FhirServer server) {
        return new FhirServerDTO(server.getId(), server.getUrl());
    }

    /**
     * Transforma un objeto {@link FhirServerDTO} en {@link FhirServer}
     * 
     * @param server el objeto {@link FhirServerDTO} que se desea transformar en
     *               {@link FhirServer}.
     * @return el objeto {@link FhirServer}.
     */
    public FhirServer toEntity(FhirServerDTO dto) {
        FhirServer server = new FhirServer();
        server.setId(dto.getId());
        server.setUrl(dto.getUrl());
        return server;
    }
}
