package us.dit.fkbroker.service.services.fhir;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.SubscriptionData;

@Service
public class CheckService {

    private static final Logger logger = LogManager.getLogger();

    @Value("${fhir.subscription.heartbeat.period}")
    private Long heartbeatPeriod;
    @Value("${fhir.subscription.heartbeat.errors}")
    private Long heartbeatErrors;

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
    public CheckService(FhirServerService fhirServerService, SubscriptionService subscriptionService,
            NotificationService notificationService) {
        this.fhirServerService = fhirServerService;
        this.subscriptionService = subscriptionService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRateString = "${fhir.subscription.heartbeat.check.fixed.rate}", initialDelayString = "${fhir.subscription.heartbeat.check.initial.delay}")
    public void checkSystem() {
        logger.debug("Se inicia proceso de comprobación de heartbeats");

        // Obtiene todos los servidores con heartbeats activados
        List<FhirServer> fhirServers = fhirServerService.getFhirServersWithHeartbeat();

        for (FhirServer fhirServer : fhirServers) {
            // Obtiene todas las subscripciones de ese servidor
            List<SubscriptionData> subscriptionDatas = subscriptionService.getSubscriptions(fhirServer.getId());

            for (SubscriptionData subscriptionData : subscriptionDatas) {
                // Comprueda cuanto tiempo hace desde que recibió la última actualización
                Instant lastUpdate = subscriptionData.getUpdated().toInstant();
                Long secondsElapsed = Duration.between(lastUpdate, Instant.now()).getSeconds();

                // En caso de que haya pasado pasado más tiempo del configurado
                if (secondsElapsed > heartbeatPeriod * heartbeatErrors) {
                    logger.warn(
                            "Se detecta periodo de inactividad mayor al permitido para la subscripción {} del servidor {}.",
                            subscriptionData.getIdSubscription(), fhirServer.getId());
                    // Se actualiza la información de la subscripción
                    notificationService.updateSubscriptionStatus(fhirServer, subscriptionData);
                    subscriptionService.updateSubscription(subscriptionData);
                }
            }
        }
    }
}
