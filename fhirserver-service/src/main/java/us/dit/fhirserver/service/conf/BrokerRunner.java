package us.dit.fhirserver.service.conf;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import us.dit.fhirserver.service.entities.db.SubscriptionDB;
import us.dit.fhirserver.service.repositories.SubscriptionRepository;
import us.dit.fhirserver.service.services.fhir.EventService;
import us.dit.fhirserver.service.services.fhir.SubscriptionSchedulerManager;

/**
 * Componente ejecutado al iniciar la aplicación.
 * 
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Component
public class BrokerRunner implements ApplicationRunner {

    private final SubscriptionSchedulerManager subscriptionSchedulerManager;
    private final SubscriptionRepository subscriptionRepository;
    private final EventService eventService;

    @Autowired
    public BrokerRunner(SubscriptionSchedulerManager subscriptionSchedulerManager,
            SubscriptionRepository subscriptionRepository, EventService eventService) {
        this.subscriptionSchedulerManager = subscriptionSchedulerManager;
        this.subscriptionRepository = subscriptionRepository;
        this.eventService = eventService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SubscriptionDB> subscriptionDBs = subscriptionRepository.findAll();

        for (SubscriptionDB subscriptionDB : subscriptionDBs) {
            subscriptionSchedulerManager.iniciarTarea(subscriptionDB.getId(), subscriptionDB.getHeartbeatPeriod(),
                    () -> eventService.sendHeartbeat(subscriptionDB.getId()));
        }
    }

}
