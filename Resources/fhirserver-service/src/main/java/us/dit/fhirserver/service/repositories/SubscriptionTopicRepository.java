package us.dit.fhirserver.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;

/**
 * Repositorio JPA de la entidad {@link SubscriptionTopicDB}
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Repository
public interface SubscriptionTopicRepository extends JpaRepository<SubscriptionTopicDB, Long> {

}
