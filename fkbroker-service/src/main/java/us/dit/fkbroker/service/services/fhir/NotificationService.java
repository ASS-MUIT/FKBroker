package us.dit.fkbroker.service.services.fhir;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionNotificationType;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionStatusNotificationEventComponent;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.entities.domain.NotificationDetails;

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

    /**
     * Constructor que inyecta FhirContext.
     * 
     * @param fhirService servicio para gestionar operaciones que se realizan sobre
     *                    elementos FHIR.
     */
    public NotificationService(FhirContext fhirContext, FhirService fhirService) {
        this.fhirService = fhirService;
    }

    /**
     * Obtiene el SubscriptionStatus que contiene el mensaje.
     * 
     * @param mesagge el JSON que contiene el SubscriptionStatus.
     * @return el recurso el SubscriptionStatus.
     */
    public List<String> getNotifications(SubscriptionStatus subscriptionStatus) {
        List<String> resources = new ArrayList<>();

        for (SubscriptionStatusNotificationEventComponent event : subscriptionStatus.getNotificationEvent()) {
            if (event.hasFocus() && event.getFocus().hasReference()) {
                resources.add(event.getFocus().getReference());
            }
        }

        return resources;
    }

    /**
     * Obtiene las referencia de los recursos que se han notificado.
     * 
     * @param notification el JSON que contiene la notificación.
     * @return la URL completa del recurso de notificación.
     */
    public NotificationDetails processNotification(String mesagge, SubscriptionData subscription) {
        NotificationDetails notificationDetails = new NotificationDetails();

        SubscriptionStatus subscriptionStatus = fhirService.getSubscriptionStatus(mesagge);

        // Comprueba si se trata de una notificación de eventos
        if (subscriptionStatus.getType().equals(SubscriptionNotificationType.EVENTNOTIFICATION)) {
            notificationDetails = processEventNotification(subscriptionStatus, subscription);
        } else {
            logger.warn("Tipo de SubscriptionStatus no tratado");
            notificationDetails.setHasNewEvents(false);
        }

        return notificationDetails;
    }

    /**
     * Obtiene las referencia de los recursos que se han notificado.
     * 
     * @param notification el JSON que contiene la notificación.
     * @return la URL completa del recurso de notificación.
     */
    public NotificationDetails processEventNotification(SubscriptionStatus subscriptionStatus,
            SubscriptionData subscription) {
        NotificationDetails notificationDetails = new NotificationDetails();
        List<String> resources = new ArrayList<>();

        // Obtiene el número de evento esperado y el recibido
        Long receivedEvent = subscriptionStatus.getNotificationEventFirstRep().getEventNumber();
        Long expectedEvent = subscription.getEvents() + 1;

        // Comprueba si ya se ha tratado este evento
        if (receivedEvent < expectedEvent) {
            logger.warn("Eventos ya tratados. Se ignora la notificación.");
            notificationDetails.setHasNewEvents(false);
        } else {
            // Comprueba que no se haya perdido ningún evento
            if (receivedEvent > expectedEvent) {
                logger.warn("Se detectan eventos perdidos. Se inicia proceso de recuperación.");

                SubscriptionStatus lostEvents = fhirService.getLostEvents(subscription.getServer().getUrl(),
                        subscription.getSubscription(), expectedEvent, receivedEvent - 1);

                resources.addAll(getNotifications(lostEvents));
            }

            // Obtiene las referencia de los recursos notificados
            resources.addAll(getNotifications(subscriptionStatus));

            notificationDetails.setHasNewEvents(true);
            notificationDetails.setLastEvent(subscriptionStatus.getEventsSinceSubscriptionStart());
            notificationDetails.setReferenceEvents(resources);
        }

        return notificationDetails;
    }

}
