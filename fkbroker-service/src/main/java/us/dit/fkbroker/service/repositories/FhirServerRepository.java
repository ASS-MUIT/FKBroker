package us.dit.fkbroker.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import us.dit.fkbroker.service.entities.db.FhirServer;

/**
 * Repositorio JPA de la entidad {@link FhirServer}
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Repository
public interface FhirServerRepository extends JpaRepository<FhirServer, Long> {

}
