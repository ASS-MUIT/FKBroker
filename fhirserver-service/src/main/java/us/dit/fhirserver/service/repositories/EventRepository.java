package us.dit.fhirserver.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import us.dit.fhirserver.service.entities.db.EventDB;

@Repository
public interface EventRepository extends JpaRepository<EventDB, Long> {
    List<EventDB> findByIdSubscriptionAndIdEventBetweenOrderByIdEvent(Long idSubscription, Long min, Long max);
    List<EventDB> findByIdSubscriptionOrderByIdEvent(Long idSubscription);
}
