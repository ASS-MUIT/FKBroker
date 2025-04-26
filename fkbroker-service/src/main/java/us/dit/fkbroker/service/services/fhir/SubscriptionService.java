package us.dit.fkbroker.service.services.fhir;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.repositories.SubscriptionRepository;

/**
 * Servicio para manejar las operaciones de la entidad {@link SubscriptionData}.
 * 
 * @author josperbel
 * @version 1.0
 * @date Abr 2025
 */
@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    private final EntityManager entityManager;

    /**
     * Constructor que inyecta el {@link SubscriptionRepository}.
     * 
     * @param subscriptionRepository repositorio JPA de la entidad
     *                               {@link SubscriptionData}.
     * @param entityManager          interfaz usada para interaccionar con el
     *                               contexto de persistencia.
     */
    @Autowired
    public SubscriptionService(SubscriptionRepository subscriptionRepository, EntityManager entityManager) {
        this.subscriptionRepository = subscriptionRepository;
        this.entityManager = entityManager;
    }

    /**
     * Obtiene un nuevo identificador de una secuencia.
     * 
     * @return el identificador obtenido de la secuencia para una nueva entidad
     *         {@link SubscriptionData}.
     */
    public Long getId() {
        return ((Number) entityManager.createNativeQuery("SELECT nextval('sub_seq')").getSingleResult()).longValue();
    }

    /**
     * Busca una entidad {@link SubscriptionData} por su ID.
     * 
     * @param id el ID de la entidad {@link SubscriptionData}.
     * @return un Optional que contiene la entidad {@link SubscriptionData} si se
     *         encuentra, o un Optional vacío si no.
     */
    public Optional<SubscriptionData> findById(Long id) {
        return subscriptionRepository.findById(id);
    }

    /**
     * Obtiene todas las entidades {@link SubscriptionData}.
     * 
     * @return un listado de {@link SubscriptionData}.
     */
    public List<SubscriptionData> findAll() {
        return subscriptionRepository.findAll();
    }

    /**
     * Guarda una entidad {@link SubscriptionData} en la base de datos.
     * 
     * @param subscription el objeto {@link SubscriptionData} a guardar.
     * @return el objeto {@link SubscriptionData} guardado.
     */
    public SubscriptionData saveSubscription(SubscriptionData subscription) {
        return subscriptionRepository.save(subscription);
    }

    /**
     * Elimina una entidad {@link SubscriptionData} de la base de datos por su ID.
     * 
     * @param server       la url del servidor de la subscripción a eliminar.
     * @param subscription el identificador de la subscripción a eliminar.
     */
    @Transactional
    public void deleteSubscription(String server, String subscription) {
        subscriptionRepository.deleteByServerAndSubscription(server, subscription);
    }

}
