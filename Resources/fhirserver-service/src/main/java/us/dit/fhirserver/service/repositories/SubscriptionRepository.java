package us.dit.fhirserver.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import us.dit.fhirserver.service.entities.db.Subs;

/**
 * Repositorio JPA de la entidad {@link Subs}
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subs, Long> {

}
