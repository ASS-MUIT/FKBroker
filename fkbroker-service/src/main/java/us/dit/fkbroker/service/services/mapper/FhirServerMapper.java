package us.dit.fkbroker.service.services.mapper;

import org.springframework.stereotype.Component;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.domain.FhirServerDetails;

/**
 * Componente que transforma objetos {@link FhirServerDetails} en
 * {@link FhirServer} y viceversa.
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Component
public class FhirServerMapper {

    /**
     * Transforma un objeto {@link FhirServer} en {@link FhirServerDetails}
     * 
     * @param server el objeto {@link FhirServer} que se desea transformar en
     *               {@link FhirServerDetails}.
     * @return el objeto {@link FhirServerDetails}.
     */
    public FhirServerDetails toDTO(FhirServer server) {
        return new FhirServerDetails(server.getId(), server.getName(), server.getUrl(), server.getHeartbeat(),
                server.getQueryOperations());
    }

    /**
     * Transforma un objeto {@link FhirServerDetails} en {@link FhirServer}
     * 
     * @param details el objeto {@link FhirServerDetails} que se desea transformar
     *                en {@link FhirServer}.
     * @return el objeto {@link FhirServer}.
     */
    public FhirServer toEntity(FhirServerDetails details) {
        return new FhirServer(details.getId(), details.getName(), details.getUrl(), details.getHeartbeat(),
                details.getQueryOperations());
    }
}
