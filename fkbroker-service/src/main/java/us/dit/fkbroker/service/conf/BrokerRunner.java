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
 * Componente ejecutado al iniciar la aplicaciÃ³n.
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
                // Actualiza la informaciÃ³n de la subscripciÃ³n
                try {
                    subscriptionData = notificationService.updateSubscriptionStatus(fhirServer, subscriptionData);
                    subscriptionService.updateSubscription(subscriptionData);
                } catch (Exception e) {
                    logger.warn("No se ha podido actualizar la subscripciÃ³n {} del servidor {}.",
                            subscriptionData.getIdSubscription(), fhirServer.getId());
                }
            }
        }
    }
}
