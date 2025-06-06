/**
*  This file is part of FKBroker - Broker sending signals to KIEServers from FHIR notifications.
*  Copyright (C) 2024  Universidad de Sevilla/Departamento de Ingeniería Telemática
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
**/
package us.dit.fkbroker.service.services.kie;

import java.util.List;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.KieServer;
import us.dit.fkbroker.service.repositories.KieServerRepository;

/**
 * Servicio para gestionar las operaciones sobre los servidores KIE.
 * 
 * @author Isabel Román, juanmabrazo98
 * @author josperbel - Nueva ubicación de entidades
 * @version 1.1
 * @date Mar 2025
 */
@Service
public class KieServerService {

    private static final Logger logger = LoggerFactory.getLogger(KieServerService.class);

    @Autowired
    private KieServerRepository kieServerRepository;

    /**
     * Obtiene todos los servidores KIE.
     * 
     * @return una lista de objetos KieServer que representan todos los servidores
     *         KIE.
     */
    public List<KieServer> getAllKieServers() {
        return kieServerRepository.findAll();
    }

    /**
     * Guarda un servidor KIE en la base de datos.
     * 
     * @param kieServer el objeto KieServer a guardar.
     * @return el objeto KieServer guardado.
     */
    public KieServer saveKieServer(KieServer kieServer) {
        return kieServerRepository.save(kieServer);
    }

    /**
     * Elimina un servidor KIE de la base de datos por su URL.
     * 
     * @param url la URL del servidor KIE a eliminar.
     */
    public void deleteKieServer(String url) {
        kieServerRepository.deleteById(url);
    }

    /**
     * Envía una señal a todos los servidores KIE configurados. Convendría hacer un
     * método específico para enviar a UN servidor KIE e invocar a este desde
     * sendSignalToAllKieServers
     * 
     * @param signal  nombre de la señal a enviar.
     * @param message mensaje a enviar como señal.
     */
    public void sendSignalToAllKieServers(String signal, String message) {
        List<KieServer> kieServers = getAllKieServers();
        for (KieServer kieServer : kieServers) {
            String serverUrl = kieServer.getUrl();
            String username = kieServer.getUsu();
            String password = kieServer.getPwd();

            MarshallingFormat FORMAT = MarshallingFormat.JSON;
            KieServicesConfiguration conf;
            KieServicesClient kieServicesClient;
            ProcessServicesClient processClient;
            try {
                conf = KieServicesFactory.newRestConfiguration(serverUrl, username, password);
                conf.setMarshallingFormat(FORMAT);
                kieServicesClient = KieServicesFactory.newKieServicesClient(conf);
                processClient = kieServicesClient.getServicesClient(ProcessServicesClient.class);
            } catch (Exception e) {
                logger.error("Error creando KieServicesClient o obteniendo ProcessServicesClient: ", e);
                continue;
            }
            // Envío la señal a cada contenedor del servidor
            try {
                KieContainerResourceList containersList = kieServicesClient.listContainers().getResult();
                List<KieContainerResource> kieContainers = containersList.getContainers();
                for (KieContainerResource container : kieContainers) {
                    logger.info("Enviando a " + serverUrl + ". la señal " + signal);
                    processClient.signal(container.getContainerId(), signal, message);
                }
            } catch (Exception e) {
                logger.error("Error enviando señal a los contenedores: ", e);
            }
        }
    }
}
