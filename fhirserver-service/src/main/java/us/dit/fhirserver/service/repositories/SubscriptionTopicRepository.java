package us.dit.fhirserver.service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import us.dit.fhirserver.service.entities.db.SubscriptionTopicDB;

@Repository
public interface SubscriptionTopicRepository extends JpaRepository<SubscriptionTopicDB, Long> {

}
