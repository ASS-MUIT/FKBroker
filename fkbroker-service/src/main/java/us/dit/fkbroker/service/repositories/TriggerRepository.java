package us.dit.fkbroker.service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import us.dit.fkbroker.service.entities.db.Trigger;

/**
 * Repositorio JPA de la entidad {@link Trigger}
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
public interface TriggerRepository extends JpaRepository<Trigger, Long> {
    Optional<Trigger> findByResourceAndInteraction(String resource, String interaction);
}
