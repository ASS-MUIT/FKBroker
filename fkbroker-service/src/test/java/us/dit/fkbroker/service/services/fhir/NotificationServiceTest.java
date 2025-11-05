package us.dit.fkbroker.service.services.fhir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hl7.fhir.r5.model.Enumerations.SubscriptionStatusCodes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import ca.uhn.fhir.parser.IParser;
import us.dit.fkbroker.service.entities.db.FhirServer;
import us.dit.fkbroker.service.entities.db.SubscriptionData;
import us.dit.fkbroker.service.entities.db.Topic;

/**
 * Test para verificar que NotificationService publica correctamente mensajes a Kafka
 * cuando recibe notificaciones FHIR.
 * 
 * @author GitHub Copilot
 * @version 1.0
 * @date Nov 2025
 */
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class NotificationServiceTest {

    private static final String TEST_TOPIC = "fhir-patient-create";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private NotificationService notificationService;

    @MockBean
    private FhirService fhirService;

    @Autowired
    private IParser jsonParser;

    private KafkaMessageListenerContainer<String, String> container;
    private BlockingQueue<ConsumerRecord<String, String>> records;

    @BeforeEach
    public void setUp() {
        // Configurar el consumidor de Kafka para el test
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(TEST_TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();

        container.setupMessageListener((MessageListener<String, String>) record -> {
            records.add(record);
        });

        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @AfterEach
    public void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    public void testProcessNotification_PublishesToKafka_WhenEventNotificationReceived() throws Exception {
        // Given: Una notificaciÃ³n FHIR con un evento de creaciÃ³n de paciente
        String fhirNotification = createFhirEventNotification("Patient/12345");
        
        SubscriptionData subscriptionData = createSubscriptionData();

        // When: Se procesa la notificaciÃ³n
        notificationService.processNotification(fhirNotification, subscriptionData);

        // Then: Se debe publicar el mensaje en Kafka
        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(received, "DeberÃ­a haberse publicado un mensaje en Kafka");
        assertEquals(TEST_TOPIC, received.topic(), "El mensaje deberÃ­a publicarse en el topic correcto");
        assertEquals("Patient/12345", received.value(), "El mensaje deberÃ­a contener la referencia del paciente");
    }

    @Test
    public void testProcessNotification_DoesNotPublishToKafka_WhenHeartbeatReceived() throws Exception {
        // Given: Una notificaciÃ³n FHIR de tipo heartbeat
        String fhirHeartbeat = createFhirHeartbeatNotification();
        
        SubscriptionData subscriptionData = createSubscriptionData();

        // When: Se procesa la notificaciÃ³n heartbeat
        notificationService.processNotification(fhirHeartbeat, subscriptionData);

        // Then: NO se debe publicar ningÃºn mensaje en Kafka
        ConsumerRecord<String, String> received = records.poll(2, TimeUnit.SECONDS);
        
        assertNull(received, "No deberÃ­a publicarse ningÃºn mensaje para notificaciones de heartbeat");
    }

    @Test
    public void testProcessNotification_PublishesMultipleMessages_WhenMultipleEventsReceived() throws Exception {
        // Given: Una notificaciÃ³n FHIR con mÃºltiples eventos
        String fhirNotification = createFhirMultipleEventsNotification(
            "Patient/12345", 
            "Patient/67890", 
            "Observation/abc123"
        );
        
        SubscriptionData subscriptionData = createSubscriptionData();

        // When: Se procesa la notificaciÃ³n
        notificationService.processNotification(fhirNotification, subscriptionData);

        // Then: Se deben publicar 3 mensajes en Kafka
        ConsumerRecord<String, String> msg1 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, String> msg2 = records.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, String> msg3 = records.poll(10, TimeUnit.SECONDS);
        
        assertNotNull(msg1, "DeberÃ­a recibirse el primer mensaje");
        assertNotNull(msg2, "DeberÃ­a recibirse el segundo mensaje");
        assertNotNull(msg3, "DeberÃ­a recibirse el tercer mensaje");
        
        assertTrue(
            records.isEmpty(), 
            "No deberÃ­an recibirse mÃ¡s mensajes de los esperados"
        );
    }

    @Test
    public void testProcessNotification_UpdatesSubscriptionStatus_Correctly() {
        // Given: Una notificaciÃ³n FHIR con estado activo
        String fhirNotification = createFhirEventNotification("Patient/12345");
        
        SubscriptionData subscriptionData = createSubscriptionData();
        subscriptionData.setEvents(0L);

        // When: Se procesa la notificaciÃ³n
        SubscriptionData result = notificationService.processNotification(fhirNotification, subscriptionData);

        // Then: El estado y eventos deben actualizarse
        assertEquals("active", result.getStatus(), "El estado deberÃ­a ser 'active'");
        assertEquals(1L, result.getEvents(), "Los eventos deberÃ­an incrementarse a 1");
    }

    /**
     * Crea una notificaciÃ³n FHIR de tipo event-notification con un evento
     */
    private String createFhirEventNotification(String resourceReference) {
        return "{\n" +
               "  \"resourceType\": \"Bundle\",\n" +
               "  \"type\": \"history\",\n" +
               "  \"entry\": [\n" +
               "    {\n" +
               "      \"resource\": {\n" +
               "        \"resourceType\": \"SubscriptionStatus\",\n" +
               "        \"status\": \"active\",\n" +
               "        \"type\": \"event-notification\",\n" +
               "        \"eventsSinceSubscriptionStart\": \"1\",\n" +
               "        \"notificationEvent\": [\n" +
               "          {\n" +
               "            \"eventNumber\": \"1\",\n" +
               "            \"focus\": {\n" +
               "              \"reference\": \"" + resourceReference + "\"\n" +
               "            }\n" +
               "          }\n" +
               "        ]\n" +
               "      }\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }

    /**
     * Crea una notificaciÃ³n FHIR de tipo heartbeat
     */
    private String createFhirHeartbeatNotification() {
        return "{\n" +
               "  \"resourceType\": \"Bundle\",\n" +
               "  \"type\": \"history\",\n" +
               "  \"entry\": [\n" +
               "    {\n" +
               "      \"resource\": {\n" +
               "        \"resourceType\": \"SubscriptionStatus\",\n" +
               "        \"status\": \"active\",\n" +
               "        \"type\": \"heartbeat\",\n" +
               "        \"eventsSinceSubscriptionStart\": \"0\"\n" +
               "      }\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }

    /**
     * Crea una notificaciÃ³n FHIR con mÃºltiples eventos
     */
    private String createFhirMultipleEventsNotification(String... resourceReferences) {
        StringBuilder events = new StringBuilder();
        for (int i = 0; i < resourceReferences.length; i++) {
            if (i > 0) events.append(",\n");
            events.append("          {\n");
            events.append("            \"eventNumber\": \"").append(i + 1).append("\",\n");
            events.append("            \"focus\": {\n");
            events.append("              \"reference\": \"").append(resourceReferences[i]).append("\"\n");
            events.append("            }\n");
            events.append("          }");
        }

        return "{\n" +
               "  \"resourceType\": \"Bundle\",\n" +
               "  \"type\": \"history\",\n" +
               "  \"entry\": [\n" +
               "    {\n" +
               "      \"resource\": {\n" +
               "        \"resourceType\": \"SubscriptionStatus\",\n" +
               "        \"status\": \"active\",\n" +
               "        \"type\": \"event-notification\",\n" +
               "        \"eventsSinceSubscriptionStart\": \"" + resourceReferences.length + "\",\n" +
               "        \"notificationEvent\": [\n" +
               events.toString() + "\n" +
               "        ]\n" +
               "      }\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }

    /**
     * Crea datos de suscripciÃ³n de prueba
     */
    private SubscriptionData createSubscriptionData() {
        FhirServer server = new FhirServer();
        server.setUrl("http://localhost:8080/fhir");
        server.setQueryOperations(false);

        Topic topic = new Topic();
        topic.setKafkaTopicName(TEST_TOPIC);

        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setIdSubscription("test-subscription-123");
        subscriptionData.setServer(server);
        subscriptionData.setTopic(topic);
        subscriptionData.setStatus(SubscriptionStatusCodes.ACTIVE.toCode());
        subscriptionData.setEvents(0L);

        return subscriptionData;
    }
}
