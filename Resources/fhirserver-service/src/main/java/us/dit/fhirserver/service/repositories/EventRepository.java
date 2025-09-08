package us.dit.fhirserver.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import us.dit.fhirserver.service.entities.db.Event;

/**
 * Repositorio JPA de la entidad {@link Event}
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByIdSubscriptionAndNumberBetweenOrderByNumber(Long idSubscription, Long min, Long max);

    List<Event> findByIdSubscriptionOrderByNumber(Long idSubscription);
}
