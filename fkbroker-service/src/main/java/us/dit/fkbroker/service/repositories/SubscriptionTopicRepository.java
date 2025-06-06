package us.dit.fkbroker.service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.Topic;

/**
 * Repositorio JPA de la entidad {@link Topic}
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
public interface SubscriptionTopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByServer(FhirServer server);

    Topic findByIdTopicAndServer(String idTopic, FhirServer server);
}
