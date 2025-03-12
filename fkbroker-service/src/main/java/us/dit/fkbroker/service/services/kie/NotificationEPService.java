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
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.NotificationEP;
import us.dit.fkbroker.service.repositories.NotificationEPRepository;

/**
 * Servicio para gestionar las operaciones sobre las entidades NotificationEP.
 * 
 * @author juanmabrazo98
 * @author josperbel - Nueva ubicación de entidades
 * @version 1.1
 * @date Mar 2025
 */
@Service
public class NotificationEPService {

    @Autowired
    private NotificationEPRepository notificationEPRepository;

    /**
     * Obtiene todas las entidades NotificationEP.
     * 
     * @return una lista de objetos NotificationEP que representan todas las
     *         entidades NotificationEP.
     */
    public List<NotificationEP> getAllNotificationEPs() {
        return notificationEPRepository.findAll();
    }

    /**
     * Guarda una entidad NotificationEP en la base de datos.
     * 
     * @param notificationEP el objeto NotificationEP a guardar.
     * @return el objeto NotificationEP guardado.
     */
    public NotificationEP saveNotificationEP(NotificationEP notificationEP) {
        return notificationEPRepository.save(notificationEP);
    }

    /**
     * Elimina una entidad NotificationEP de la base de datos por su ID.
     * 
     * @param id el ID de la entidad NotificationEP a eliminar.
     */
    public void deleteNotificationEP(Long id) {
        notificationEPRepository.deleteById(id);
    }

    /**
     * Busca una entidad NotificationEP por su recurso e interacción.
     * 
     * @param resource    el recurso de la entidad NotificationEP.
     * @param interaction la interacción de la entidad NotificationEP.
     * @return un Optional que contiene la entidad NotificationEP si se encuentra, o
     *         un Optional vacío si no.
     */
    public Optional<NotificationEP> findNotificationEPByResourceAndInteraction(String resource, String interaction) {
        return notificationEPRepository.findByResourceAndInteraction(resource, interaction);
    }

    /**
     * Busca una entidad NotificationEP por su ID.
     * 
     * @param id el ID de la entidad NotificationEP.
     * @return un Optional que contiene la entidad NotificationEP si se encuentra, o
     *         un Optional vacío si no.
     */
    public Optional<NotificationEP> findById(Long id) {
        return notificationEPRepository.findById(id);
    }
}
