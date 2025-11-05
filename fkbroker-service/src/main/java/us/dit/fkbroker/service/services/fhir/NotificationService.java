/**
*  This file is part of FKBroker - Broker sending signals to KIEServers from FHIR notifications.
*  Copyright (C) 2024  Universidad de Sevilla/Departamento de IngenierÃ­a TelemÃ¡tica
*
*  FKBroker is free software: you can redistribute it and/or
*  modify it under the terms of the GNU General Public License as published
*  by the Free Software Foundation, either version 3 of the License, or (at
*  your option) any later version.
*
*  FKBroker is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
*  Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with FKBroker. If not, see <https://www.gnu.org/licenses/>.
*
*  This software uses third-party dependencies, including libraries licensed under Apache 2.0.
*  See the project documentation for more details on dependency licenses.
**/
package us.dit.fkbroker.service.services.fhir;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionNotificationType;
import org.hl7.fhir.r5.model.SubscriptionStatus.SubscriptionStatusNotificationEventComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.parser.IParser;
import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.services.kafka.KafkaProducerService;

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
    private final KafkaProducerService kafkaProducerService;
    private final IParser jsonParser;

    private final Set<SubscriptionNotificationType> validTypes;

    /**
     * Constructor que inyecta los servicios {@link FhirService} y
     * {@link KafkaProducerService}.
     * 
     * @param fhirService           servicio para gestionar operaciones que se realizan sobre
     *                              elementos FHIR.
     * @param kafkaProducerService  servicio para publicar mensajes en Kafka.
     * @param jsonParser            parser JSON de FHIR.
     */
    @Autowired
    public NotificationService(FhirService fhirService, KafkaProducerService kafkaProducerService, IParser jsonParser) {
        this.fhirService = fhirService;
        this.kafkaProducerService = kafkaProducerService;
        this.jsonParser = jsonParser;
        this.validTypes = EnumSet.of(SubscriptionNotificationType.EVENTNOTIFICATION,
                SubscriptionNotificationType.HEARTBEAT, SubscriptionNotificationType.HANDSHAKE);
    }

    /**
     * Obtiene las referencia de los recursos que se han notificado.
     * 
     * @param notification el JSON que contiene la notificaciÃ³n.
     * @return la URL completa del recurso de notificaciÃ³n.
     */
    public SubscriptionData processNotification(String mesagge, SubscriptionData subscriptionData) {
        String kafkaTopicName = subscriptionData.getTopic().getKafkaTopicName();
        FhirServer server = subscriptionData.getServer();

        // Obtiene el Bundle de la notificaciÃ³n recibida
        Bundle bundle = jsonParser.parseResource(Bundle.class, mesagge);

        // Comprueba que tenga SubscriptionStatus y lo extrae
        SubscriptionStatus subscriptionStatus;
        if (bundle.getEntry().isEmpty() || !bundle.getEntryFirstRep().hasResource()
                || bundle.getEntryFirstRep().getResource().getClass() != SubscriptionStatus.class) {
            throw new RuntimeException("Mensaje incorrecto. Bundle sin SubscriptionStatus.");
        } else {
            subscriptionStatus = (SubscriptionStatus) bundle.getEntryFirstRep().getResource();
        }

        // Comprueba que sea un tipo de notificaciÃ³n vÃ¡lido, sino lanza una excepciÃ³n
        SubscriptionNotificationType notificationType = subscriptionStatus.getType();
        if (!validTypes.contains(notificationType)) {
            throw new RuntimeException("Invalid NotificationType");
        }

        // Comprueba el estado de la subscripciÃ³n, actualizandolo si es necesario
        SubscriptionStatusCodes status = subscriptionStatus.getStatus();
        if (status == SubscriptionStatusCodes.ERROR) {
            // Si detecta un error, vuelve a activar la subscripciÃ³n y actualiza el estado
            logger.warn("Se detecta estado de ERROR. Se inicia proceso de actualizaciÃ³n.");
            CompletableFuture.runAsync(
                    () -> fhirService.updateSubscriptionStatus(server.getUrl(), subscriptionData.getIdSubscription()));
            subscriptionData.setStatus(SubscriptionStatusCodes.REQUESTED.toCode());
        } else {
            // Si no, guarda el estado de la notificaciÃ³n en la subscripciÃ³n
            subscriptionData.setStatus(status.toCode());
        }

        // Comprueba si tiene la operaciÃ³n $events activada
        if (server.getQueryOperations()) {
            // Comprueba si se trata de una notificaciÃ³n de eventos
            if (notificationType == SubscriptionNotificationType.EVENTNOTIFICATION) {
                // En este caso recupera el primer evento recibido y el esperado
                Long receivedEvent = subscriptionStatus.getNotificationEventFirstRep().getEventNumber();
                Long expectedEvent = subscriptionData.getEvents() + 1;
                if (receivedEvent > expectedEvent) {
                    logger.warn("Se detectan eventos perdidos. Se inicia proceso de recuperaciÃ³n.");
                    CompletableFuture.runAsync(() -> getAndSendLostEvents(server.getUrl(),
                            subscriptionData.getIdSubscription(), expectedEvent, receivedEvent - 1, kafkaTopicName));
                }
            } else {
                // Si no se trata de una notificaciÃ³n de eventos, recupera el Ãºltimo evento
                // enviado y el Ãºltimo evento recibido
                Long lastEventSent = subscriptionStatus.getEventsSinceSubscriptionStart();
                Long lastEventReceived = subscriptionData.getEvents();
                if (lastEventSent > lastEventReceived) {
                    logger.warn("Se detectan eventos perdidos. Se inicia proceso de recuperaciÃ³n.");
                    CompletableFuture.runAsync(() -> getAndSendLostEvents(server.getUrl(),
                            subscriptionData.getIdSubscription(), lastEventReceived + 1, lastEventSent, kafkaTopicName));
                }
            }
        }

        // Si se trata de una notificaciÃ³n de eventos publica las referencias en Kafka
        if (notificationType == SubscriptionNotificationType.EVENTNOTIFICATION) {
            CompletableFuture
                    .runAsync(() -> publishToKafka(kafkaTopicName, getReferenceNotifications(subscriptionStatus)));
        }

        // Guarda el Ãºltimo evento recibido
        subscriptionData.setEvents(subscriptionStatus.getEventsSinceSubscriptionStart());

        return subscriptionData;
    }

    /**
     * Actualiza los datos de una subscripciÃ³n en la base de datos comprobando su
     * estado actual en el servidor FHIR. Si el servidor tiene activadas las
     * operaciones $status y $events, tambiÃ©n comprueba que no se haya perdido
     * ningÃºn evento.
     * 
     * @param server           informaciÃ³n del servidor FHIR.
     * @param subscriptionData datos de la subscripciÃ³n a actualizar.
     */
    public SubscriptionData updateSubscriptionStatus(FhirServer server, SubscriptionData subscriptionData) {
        SubscriptionStatusCodes status;

        // Comprueba si tiene las operaciones $status y $events activadas
        if (server.getQueryOperations()) {
            // Obtiene el estado de la subscripciÃ³n
            SubscriptionStatus subscriptionStatus = fhirService.getStatus(server.getUrl(),
                    subscriptionData.getIdSubscription());
            status = subscriptionStatus.getStatus();

            // Comprueba que no se haya perdido ningÃºn evento y, en caso de detectar
            // perdida, recupera estos eventos y se notifica a Kafka
            Long lastEventSent = subscriptionStatus.getEventsSinceSubscriptionStart();
            Long lastEventReceived = subscriptionData.getEvents();
            String kafkaTopicName = subscriptionData.getTopic().getKafkaTopicName();
            if (lastEventSent > lastEventReceived) {
                logger.warn("Se detectan eventos perdidos. Se inicia proceso de recuperaciÃ³n.");
                CompletableFuture.runAsync(() -> getAndSendLostEvents(server.getUrl(),
                        subscriptionData.getIdSubscription(), lastEventReceived + 1, lastEventSent, kafkaTopicName));
            }

            subscriptionData.setEvents(lastEventSent);
        } else {
            // Obtiene el estado de la subscripciÃ³n
            Subscription subscription = fhirService.getSubscription(server.getUrl(),
                    subscriptionData.getIdSubscription());
            status = subscription.getStatus();
        }

        // Comprueba el estado de la subscripciÃ³n, actualizandolo si es necesario
        if (status == SubscriptionStatusCodes.ERROR) {
            // Si detecta un error, vuelve a activar la subscripciÃ³n y actualiza el estado
            logger.warn("Se detecta estado de ERROR. Se inicia proceso de actualizaciÃ³n.");
            CompletableFuture.runAsync(
                    () -> fhirService.updateSubscriptionStatus(server.getUrl(), subscriptionData.getIdSubscription()));
            subscriptionData.setStatus(SubscriptionStatusCodes.REQUESTED.toCode());
        } else {
            // Si no, guarda el estado de la notificaciÃ³n en la subscripciÃ³n
            subscriptionData.setStatus(status.toCode());
        }

        return subscriptionData;
    }

    /**
     * Obtiene el listado de referencias de recursos notificados que contiene un
     * SubscriptionStatus.
     * 
     * @param subscriptionStatus recurso FHIR con la informaciÃ³n del estado de la
     *                           subscribciÃ³n.
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
    /**
     * Obtiene el listado de referencias de recursos notificados que contiene un
     * SubscriptionStatus.
     * 
     * @param urlServer         URL del servidor FHIR.
     * @param idSubscription    identificador de la subscripciÃ³n FHIR.
     * @param eventsSinceNumber nÃºmero del primer evento perdido.
     * @param eventsUntilNumber nÃºmero del Ãºltimo evento perdido.
     * @param kafkaTopicName    nombre del topic de Kafka.
     */
    private void getAndSendLostEvents(String urlServer, String idSubscription, Long eventsSinceNumber,
            Long eventsUntilNumber, String kafkaTopicName) {
        // Recupera los eventos perdidos
        SubscriptionStatus lostEvents = fhirService.getLostEvents(urlServer, idSubscription, eventsSinceNumber,
                eventsUntilNumber);

        // Publica las referencias de los recursos notificados en Kafka
        publishToKafka(kafkaTopicName, getReferenceNotifications(lostEvents));
    }

    /**
     * Publica una lista de referencias de recursos FHIR en un topic de Kafka
     * 
     * @param kafkaTopicName nombre del topic de Kafka
     * @param references     lista de referencias de recursos FHIR
     */
    private void publishToKafka(String kafkaTopicName, List<String> references) {
        if (kafkaTopicName == null || kafkaTopicName.isEmpty()) {
            logger.warn("No hay topic de Kafka configurado para esta subscripciÃ³n");
            return;
        }
        
        for (String reference : references) {
            logger.info("Publicando en Kafka topic '{}': {}", kafkaTopicName, reference);
            kafkaProducerService.publishMessage(kafkaTopicName, reference);
        }
    }

}
