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

/**
 * Servicio para comprobar que no se haya perdido la conexiÃ³n en las
 * suscripciones que se encuentran en servidores con Heartbeat configurado.
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
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
        logger.debug("Se inicia proceso de comprobaciÃ³n de heartbeats");

        // Obtiene todos los servidores con heartbeats activados
        List<FhirServer> fhirServers = fhirServerService.getFhirServersWithHeartbeat();

        for (FhirServer fhirServer : fhirServers) {
            // Obtiene todas las subscripciones de ese servidor
            List<SubscriptionData> subscriptionDatas = subscriptionService.getSubscriptions(fhirServer.getId());

            for (SubscriptionData subscriptionData : subscriptionDatas) {
                // Comprueda cuanto tiempo hace desde que recibiÃ³ la Ãºltima actualizaciÃ³n
                Instant lastUpdate = subscriptionData.getUpdated().toInstant();
                Long secondsElapsed = Duration.between(lastUpdate, Instant.now()).getSeconds();

                // En caso de que haya pasado pasado mÃ¡s tiempo del configurado
                if (secondsElapsed > heartbeatPeriod * heartbeatErrors) {
                    logger.warn(
                            "Se detecta periodo de inactividad mayor al permitido para la subscripciÃ³n {} del servidor {}.",
                            subscriptionData.getIdSubscription(), fhirServer.getId());
                    // Se actualiza la informaciÃ³n de la subscripciÃ³n
                    notificationService.updateSubscriptionStatus(fhirServer, subscriptionData);
                    subscriptionService.updateSubscription(subscriptionData);
                }
            }
        }
    }
}
