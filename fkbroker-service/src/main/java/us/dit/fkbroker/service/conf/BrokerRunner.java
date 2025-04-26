package us.dit.fkbroker.service.conf;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import us.dit.fkbroker.service.entities.db.KieServer;
import us.dit.fkbroker.service.entities.db.Signal;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.services.fhir.FhirService;
import us.dit.fkbroker.service.services.fhir.NotificationService;
import us.dit.fkbroker.service.services.fhir.SubscriptionService;
import us.dit.fkbroker.service.services.kie.KieServerService;
import us.dit.fkbroker.service.services.kie.SignalService;

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

    private static final Logger logger = LogManager.getLogger();

    private final FhirService fhirService;
    private final SubscriptionService subscriptionService;
    private final KieServerService kieServerService;
    private final SignalService signalService;
    private final NotificationService notificationService;

    /**
     * Constructor que inyecta los servicios {@link FhirService},
     * {@link SubscriptionService} y {@link NotificationService}.
     * 
     * @param fhirService         servicio para gestionar operaciones que se
     *                            realizan sobre elementos FHIR.
     * @param subscriptionService servicio para gestionar las operaciones sobre las
     *                            entidades {@link SubscriptionData}.
     * @param kieServerService    servicio para gestionar las operaciones sobre las
     *                            entidades {@link KieServer}.
     * @param signalService       servicio para gestionar las operaciones sobre las
     *                            entidades {@link Signal}.
     * @param notificationService servicio para gestionar las notificaciones de
     *                            subscripciones.
     */
    @Autowired
    public BrokerRunner(FhirService fhirService, SubscriptionService subscriptionService,
            KieServerService kieServerService, SignalService signalService, NotificationService notificationService) {
        this.fhirService = fhirService;
        this.subscriptionService = subscriptionService;
        this.kieServerService = kieServerService;
        this.signalService = signalService;
        this.notificationService = notificationService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Obtiene de base de datos todas las subscripciones que deben estar activas
        List<SubscriptionData> subscriptions = subscriptionService.findAll();

        for (SubscriptionData subscription : subscriptions) {
            // Ontiene el estado de las subscripciones de cada servidor
            SubscriptionStatus status = fhirService.getStatus(subscription.getServer(), subscription.getSubscription());

            // TODO debe comprobar que estén activas, sino debe volver a activarlas

            // Comprueba que no se haya perdido ninguna subscripción
            Long lastEventSent = status.getEventsSinceSubscriptionStart();
            Long lastEventReceived = subscription.getEvents();
            if (lastEventSent > lastEventReceived) {
                // En caso de detectar perdida, recupera estos eventos
                logger.warn("Se detectan eventos perdidos. Se inicia proceso de recuperación.");

                SubscriptionStatus LostEvents = fhirService.getLostEvents(subscription.getServer(),
                        subscription.getSubscription(), lastEventReceived + 1, lastEventSent);

                List<String> resources = notificationService.getNotifications(LostEvents);

                if (resources.size() > 0) {
                    // Actualiza el número de eventos recibidos
                    subscription.setEvents(lastEventSent);
                    subscriptionService.saveSubscription(subscription);

                    // Se obtiene la señal que se debe enviar
                    Optional<Signal> optionalSignal = signalService.getSignalByResourceAndInteraction(
                            subscription.getResource(), subscription.getInteraction());
                    if (optionalSignal.isPresent()) {
                        // Se envía una señal a todos los servidores KIE por cada recurso notificado
                        for (String resource : resources) {
                            logger.info("Llamamos a sendsignal. Id del recurso: {}", resource);
                            kieServerService.sendSignalToAllKieServers(optionalSignal.get(), resource);
                        }
                    }
                }
            }
        }
    }
}
