package us.dit.fkbroker.service.conf;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.services.fhir.FhirServerService;
import us.dit.fkbroker.service.services.fhir.NotificationService;
import us.dit.fkbroker.service.services.fhir.SubscriptionService;

/**
 * Componente ejecutado al iniciar la aplicación.
 * 
 *
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Component
public class BrokerRunner implements ApplicationRunner {

    private static final Logger logger = LogManager.getLogger();

    private final FhirServerService fhirServerService;
    private final SubscriptionService subscriptionService;
    private final NotificationService notificationService;

    /**
     * Constructor que inyecta los servicios {@link FhirServerService} y
     * {@link SubscriptionService}.
     * 
     * @param fhirServerService   servicio utilizado para gestionar los servidores
     *                            FHIR.
     * @param subscriptionService servicio utilizado para gestionar las
     *                            subscripciones.
     * @param notificationService servicio para gestionar las notificaciones de
     *                            subscripciones.
     */
    @Autowired
    public BrokerRunner(FhirServerService fhirServerService, SubscriptionService subscriptionService,
            NotificationService notificationService) {
        this.fhirServerService = fhirServerService;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.debug("Se comprueba el estado de las subscripciones de los servidores FHIR configurados.");

        // Obtiene todos los servidores guardados en la base de datos
        List<FhirServer> fhirServers = fhirServerService.getAllFhirServers();

        for (FhirServer fhirServer : fhirServers) {
            // Obtiene todas las subscripciones del servidor FHIR
            List<SubscriptionData> subscriptionDatas = subscriptionService.getSubscriptions(fhirServer.getId());

            for (SubscriptionData subscriptionData : subscriptionDatas) {
                // Actualiza la información de la subscripción
                subscriptionData = notificationService.updateSubscriptionStatus(fhirServer, subscriptionData);
                subscriptionService.updateSubscription(subscriptionData);
            }
        }
    }
}
