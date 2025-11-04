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
*
*  This software uses third-party dependencies, including libraries licensed under Apache 2.0.
*  See the project documentation for more details on dependency licenses.
**/
package us.dit.fkbroker.service.services.kafka;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.KafkaBroker;
import us.dit.fkbroker.service.repositories.KafkaBrokerRepository;

/**
 * Servicio para gestionar la publicación de mensajes en Kafka
 * 
 * @author josperbel
 * @version 1.0
 * @date Nov 2025
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LogManager.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaBrokerRepository kafkaBrokerRepository;

    /**
     * Constructor que inyecta el template de Kafka y el repositorio de brokers
     * 
     * @param kafkaTemplate         template de Kafka para publicar mensajes
     * @param kafkaBrokerRepository repositorio de brokers Kafka
     */
    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate,
            KafkaBrokerRepository kafkaBrokerRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaBrokerRepository = kafkaBrokerRepository;
    }

    /**
     * Publica un mensaje en un topic de Kafka
     * 
     * @param topicName nombre del topic de Kafka
     * @param message   mensaje a publicar (ID del recurso FHIR)
     */
    public void publishMessage(String topicName, String message) {
        try {
            // Verifica que exista un broker configurado
            Optional<KafkaBroker> broker = kafkaBrokerRepository.findFirstByOrderByIdAsc();
            if (!broker.isPresent()) {
                logger.error("No hay ningún broker Kafka configurado");
                return;
            }

            // Configura dinámicamente el bootstrap servers
            kafkaTemplate.getProducerFactory().getConfigurationProperties()
                    .put("bootstrap.servers", broker.get().getBootstrapServers());

            // Publica el mensaje
            kafkaTemplate.send(topicName, message);
            logger.info("Mensaje publicado en topic '{}': {}", topicName, message);

        } catch (Exception e) {
            logger.error("Error publicando mensaje en Kafka topic '{}': {}", topicName, e.getMessage(), e);
        }
    }
}
