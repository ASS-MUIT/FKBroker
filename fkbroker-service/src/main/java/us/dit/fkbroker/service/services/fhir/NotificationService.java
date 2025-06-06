package us.dit.fkbroker.service.services.fhir;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionNotificationType;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionStatusNotificationEventComponent;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.services.kie.KieServerService;
import us.dit.fkbroker.service.services.kie.KieService;
import us.dit.fkbroker.service.services.kie.SignalService;

/**
 * Servicio que procesa los distintos tipos de notificaciones FHIR
 * 
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Service
public class NotificationService {

    private static final Logger logger = LogManager.getLogger(FhirService.class);

    private final FhirService fhirService;
    private final KieService kieService;

    private final Set<SubscriptionNotificationType> validTypes;

    /**
     * Constructor que inyecta los servicios {@link FhirService},
     * {@link KieServerService} y {@link SignalService}
     * 
     * @param fhirService servicio para gestionar operaciones que se realizan sobre
     *                    elementos FHIR.
     * @param kieService  servicio para gestionar las operaciones sobre los
     *                    servidores y las señales KIE.
     */
    public NotificationService(FhirService fhirService, KieService kieService) {
        this.fhirService = fhirService;
        this.kieService = kieService;
        this.validTypes = EnumSet.of(SubscriptionNotificationType.EVENTNOTIFICATION,
                SubscriptionNotificationType.HEARTBEAT, SubscriptionNotificationType.HANDSHAKE);
    }

    /**
     * Obtiene las referencia de los recursos que se han notificado.
     * 
     * @param notification el JSON que contiene la notificación.
     * @return la URL completa del recurso de notificación.
     */
    public SubscriptionData processNotification(String mesagge, SubscriptionData subscriptionData) {
        Long idTrigger = subscriptionData.getTopic().getTrigger().getId();
        FhirServer server = subscriptionData.getServer();

        // Obtiene el estado de la subscripción de la notificación recibida
        SubscriptionStatus subscriptionStatus = fhirService.getSubscriptionStatus(mesagge);

        // Comprueba que sea un tipo de notificación válido, sino lanza una excepción
        SubscriptionNotificationType notificationType = subscriptionStatus.getType();
        if (!validTypes.contains(notificationType)) {
            throw new RuntimeException("Invalid NotificationType");
        }

        // Comprueba el estado de la subscripción, actualizandolo si es necesario
        SubscriptionStatusCodes status = subscriptionStatus.getStatus();
        if (status == SubscriptionStatusCodes.ERROR) {
            // Si detecta un error, vuelve a activar la subscripción y actualiza el estado
            logger.warn("Se detecta estado de ERROR. Se inicia proceso de actualización.");
            CompletableFuture.runAsync(
                    () -> fhirService.updateSubscriptionStatus(server.getUrl(), subscriptionData.getIdSubscription()));
            subscriptionData.setStatus(SubscriptionStatusCodes.REQUESTED.toCode());
        } else {
            // Si no, guarda el estado de la notificación en la subscripción
            subscriptionData.setStatus(status.toCode());
        }

        // Comprueba si tiene la operación $events activada
        if (server.getQueryOperations()) {
            // Comprueba si se trata de una notificación de eventos
            if (notificationType == SubscriptionNotificationType.EVENTNOTIFICATION) {
                // En este caso recupera el primer evento recibido y el esperado
                Long receivedEvent = subscriptionStatus.getNotificationEventFirstRep().getEventNumber();
                Long expectedEvent = subscriptionData.getEvents() + 1;
                if (receivedEvent > expectedEvent) {
                    logger.warn("Se detectan eventos perdidos. Se inicia proceso de recuperación.");
                    CompletableFuture.runAsync(() -> getAndSendLostEvents(server.getUrl(),
                            subscriptionData.getIdSubscription(), expectedEvent, receivedEvent - 1, idTrigger));
                }
            } else {
                // Si no se trata de una notificación de eventos, recupera el último evento
                // enviado y el último evento recibido
                Long lastEventSent = subscriptionStatus.getEventsSinceSubscriptionStart();
                Long lastEventReceived = subscriptionData.getEvents();
                if (lastEventSent > lastEventReceived) {
                    logger.warn("Se detectan eventos perdidos. Se inicia proceso de recuperación.");
                    CompletableFuture.runAsync(() -> getAndSendLostEvents(server.getUrl(),
                            subscriptionData.getIdSubscription(), lastEventReceived + 1, lastEventSent, idTrigger));
                }
            }
        }

        // Si se trata de una notificación de eventos envía las referencia de los
        // recursos notificados mediante señales a los servidores KIE
        if (notificationType == SubscriptionNotificationType.EVENTNOTIFICATION) {
            CompletableFuture
                    .runAsync(() -> kieService.sendSignal(idTrigger, getReferenceNotifications(subscriptionStatus)));
        }

        // Guarda el último evento recibido
        subscriptionData.setEvents(subscriptionStatus.getEventsSinceSubscriptionStart());

        return subscriptionData;
    }

    /**
     * Actualiza los datos de una subscripción en la base de datos comprobando su
     * estado actual en el servidor FHIR. Si el servidor tiene activadas las
     * operaciones $status y $events, también comprueba que no se haya perdido
     * ningún evento.
     * 
     * @param server           información del servidor FHIR.
     * @param subscriptionData datos de la subscripción a actualizar.
     */
    public SubscriptionData updateSubscriptionStatus(FhirServer server, SubscriptionData subscriptionData) {
        SubscriptionStatusCodes status;

        // Comprueba si tiene las operaciones $status y $events activadas
        if (server.getQueryOperations()) {
            // Obtiene el estado de la subscripción
            SubscriptionStatus subscriptionStatus = fhirService.getStatus(server.getUrl(),
                    subscriptionData.getIdSubscription());
            status = subscriptionStatus.getStatus();

            // Comprueba que no se haya perdido ningún evento y, en caso de detectar
            // perdida, recupera estos eventos y se notifica al servidor KIE correspondiente
            Long lastEventSent = subscriptionStatus.getEventsSinceSubscriptionStart();
            Long lastEventReceived = subscriptionData.getEvents();
            Long idTrigger = subscriptionData.getTopic().getTrigger().getId();
            if (lastEventSent > lastEventReceived) {
                logger.warn("Se detectan eventos perdidos. Se inicia proceso de recuperación.");
                CompletableFuture.runAsync(() -> getAndSendLostEvents(server.getUrl(),
                        subscriptionData.getIdSubscription(), lastEventReceived + 1, lastEventSent, idTrigger));
            }

            subscriptionData.setEvents(lastEventSent);
        } else {
            // Obtiene el estado de la subscripción
            Subscription subscription = fhirService.getSubscription(server.getUrl(),
                    subscriptionData.getIdSubscription());
            status = subscription.getStatus();
        }

        // Comprueba el estado de la subscripción, actualizandolo si es necesario
        if (status == SubscriptionStatusCodes.ERROR) {
            // Si detecta un error, vuelve a activar la subscripción y actualiza el estado
            logger.warn("Se detecta estado de ERROR. Se inicia proceso de actualización.");
            CompletableFuture.runAsync(
                    () -> fhirService.updateSubscriptionStatus(server.getUrl(), subscriptionData.getIdSubscription()));
            subscriptionData.setStatus(SubscriptionStatusCodes.REQUESTED.toCode());
        } else {
            // Si no, guarda el estado de la notificación en la subscripción
            subscriptionData.setStatus(status.toCode());
        }

        return subscriptionData;
    }

    /**
     * Obtiene el listado de referencias de recursos notificados que contiene un
     * SubscriptionStatus.
     * 
     * @param subscriptionStatus recurso FHIR con la información del estado de la
     *                           subscribción.
     * @return el listado de recursos que contiene el SubscriptionStatus.
     */
    private List<String> getReferenceNotifications(SubscriptionStatus subscriptionStatus) {
        List<String> resources = new ArrayList<>();

        for (SubscriptionStatusNotificationEventComponent event : subscriptionStatus.getNotificationEvent()) {
            if (event.hasFocus() && event.getFocus().hasReference()) {
                resources.add(event.getFocus().getReference());
            }
        }

        return resources;
    }

    /**
     * Obtiene el listado de referencias de recursos notificados que contiene un
     * SubscriptionStatus.
     * 
     * @param urlServer         URL del servidor FHIR.
     * @param idSubscription    identificador de la subscripción FHIR.
     * @param eventsSinceNumber número del primer evento perdido.
     * @param eventsUntilNumber número del último evento perdido.
     * @param idTrigger         identificador del trigger.
     */
    private void getAndSendLostEvents(String urlServer, String idSubscription, Long eventsSinceNumber,
            Long eventsUntilNumber, Long idTrigger) {
        // Recupera los eventos perdidos
        SubscriptionStatus lostEvents = fhirService.getLostEvents(urlServer, idSubscription, eventsSinceNumber,
                eventsUntilNumber);

        // Envía las referencia de los recursos notificados mediante señales a los
        // servidores KIE configurados.
        kieService.sendSignal(idTrigger, getReferenceNotifications(lostEvents));
    }

}
