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
package us.dit.fkbroker.service.services.kafka;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.KafkaBroker;
import us.dit.fkbroker.service.repositories.KafkaBrokerRepository;

/**
 * Servicio para gestionar las operaciones sobre los brokers Kafka
 * 
 * @author josperbel
 * @version 1.0
 * @date Nov 2025
 */
@Service
public class KafkaBrokerService {

    private final KafkaBrokerRepository kafkaBrokerRepository;

    /**
     * Constructor que inyecta el repositorio de brokers Kafka
     * 
     * @param kafkaBrokerRepository repositorio JPA de la entidad KafkaBroker
     */
    @Autowired
    public KafkaBrokerService(KafkaBrokerRepository kafkaBrokerRepository) {
        this.kafkaBrokerRepository = kafkaBrokerRepository;
    }

    /**
     * Obtiene todos los brokers Kafka configurados
     * 
     * @return lista de brokers Kafka
     */
    public List<KafkaBroker> getAllKafkaBrokers() {
        return kafkaBrokerRepository.findAll();
    }

    /**
     * Obtiene el broker Kafka configurado (sÃ³lo deberÃ­a haber uno)
     * 
     * @return el broker Kafka si existe
     */
    public Optional<KafkaBroker> getKafkaBroker() {
        return kafkaBrokerRepository.findFirstByOrderByIdAsc();
    }

    /**
     * Guarda o actualiza un broker Kafka en la base de datos
     * 
     * @param kafkaBroker el broker Kafka a guardar
     * @return el broker Kafka guardado
     */
    public KafkaBroker saveKafkaBroker(KafkaBroker kafkaBroker) {
        return kafkaBrokerRepository.save(kafkaBroker);
    }

    /**
     * Elimina un broker Kafka de la base de datos por su ID
     * 
     * @param id el ID del broker Kafka a eliminar
     */
    public void deleteKafkaBroker(Long id) {
        kafkaBrokerRepository.deleteById(id);
    }
}
