package us.dit.fkbroker.service.services.fhir;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Integer64Type;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Subscription;
import org.hl7.fhir.r5.model.SubscriptionStatus;
import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Esta clase desarrolla las distintas operaciones que se realizan sobre
 * elementos FHIR.
 * 
 * Esta clase está desarrollada partiendo de la base de la clase FhirClient de
 * juanmabrazo98, optimizando el uso de recursos y clientes de HAPI FHIR en
 * lugar de RestTemplate y JSONObject.
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Service
public class FhirService {
    
    @Value("${application.address}")
    private String applicationAddress;

    private static final Logger logger = LogManager.getLogger(FhirService.class);

    private final FhirContext fhirContext;

    private final ConcurrentMap<String, IGenericClient> client = new ConcurrentHashMap<>();

    /**
     * Constructor que inyecta FhirContext.
     * 
     * @param fhirContext
     */
    public FhirService(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    /**
     * Obtiene un cliente para un servidor FHIR a partir de su URL.
     * 
     * @param fhirUrl la URL del servidor FHIR.
     * @return un cliente FHIR.
     */
    public IGenericClient getClient(String fhirUrl) {
        return client.computeIfAbsent(fhirUrl, url -> fhirContext.newRestfulGenericClient(url));
    }

    /**
     * Obtiene una lista de SubscriptionTopics desde un servidor FHIR.
     * 
     * @param fhirUrl la URL del servidor FHIR.
     * @return una lista de SubscriptionTopics.
     */
    public List<SubscriptionTopic> getSubscriptionTopics(String fhirUrl) {
        // Obtiene los SubscriptionTopics del servidor FHIR
        IGenericClient client = getClient(fhirUrl);
        Bundle bundle = client.search().forResource(SubscriptionTopic.class).returnBundle(Bundle.class).execute();

        // Comprueba si obtengo respuesta y no está vacía
        if (bundle == null || bundle.getEntry().isEmpty()) {
            return Collections.emptyList();
        } else {
            return bundle.getEntry().stream().map(entry -> (SubscriptionTopic) entry.getResource())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Obtiene los detalles de un SubscriptionTopic desde un servidor FHIR.
     * 
     * @param id  el id del recurso SubscriptionTopic.
     * @param url la URL del servidor FHIR.
     * @return detalles de un SubscriptionTopic
     */
    public SubscriptionTopic getSubscriptionTopic(String fhirUrl, String id) {
        IGenericClient client = getClient(fhirUrl);
        return client.read().resource(SubscriptionTopic.class).withId(id).execute();
    }
    
    /**
     * Obtiene una lista de suscripciones desde un servidor FHIR.
     * 
     * @param fhirUrl la URL del servidor FHIR.
     * @return una lista de suscripciones.
     */
    public List<Subscription> getSubscriptions(String fhirUrl) {
        // Obtiene los Subscriptions del servidor FHIR
        IGenericClient client = getClient(fhirUrl);
        Bundle bundle = client.search().forResource(Subscription.class).returnBundle(Bundle.class).execute();

        // Comprueba si obtengo respuesta y no está vacía
        if (bundle == null || bundle.getEntry().isEmpty()) {
            return Collections.emptyList();
        } else {
            return bundle.getEntry().stream().map(entry -> (Subscription) entry.getResource())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Obtiene los detalles de un SubscriptionTopic desde un servidor FHIR.
     * 
     * @param id  el id del recurso SubscriptionTopic.
     * @param url la URL del servidor FHIR.
     * @return detalles de un SubscriptionTopic
     */
    public Subscription getSubscription(String fhirUrl, String id) {
        IGenericClient client = getClient(fhirUrl);
        Subscription subscription = client.read().resource(Subscription.class).withId(id).execute();
        return subscription;
    }

    /**
     * Crea una nueva suscripción en el servidor FHIR.
     * 
     * @param topicUrl la URL del SubscriptionTopic.
     * @param payload  el payload de la suscripción.
     * @param filters  la lista de filtros de la suscripción.
     * @param fhirUrl  la URL del servidor FHIR.
     * @param endpoint el endpoint de la suscripción.
     */
    public Subscription createSubscription(String fhirUrl, Subscription subscription) {
        logger.debug("Entro en createSubscription del fhirClient");

        // Envía el recurso Subscription al servidor FHIR
        IGenericClient client = getClient(fhirUrl);
        Subscription createdSubscription = (Subscription) client.create().resource(subscription).execute()
                .getResource();

        logger.info("Suscripción creada: {}",
                fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(createdSubscription));

        return createdSubscription;
    }

    /**
     * Elimina una suscripción en el servidor FHIR.
     * 
     * @param subscriptionId el ID de la suscripción a eliminar.
     * @param fhirUrl        la URL del servidor FHIR.
     */
    public void deleteSubscription(String fhirUrl, String id) {
        IGenericClient client = getClient(fhirUrl);
        client.delete().resourceById("Subscription", id).execute();
    }

    /**
     * Obtiene las referencia de las notificaciones que se han perdido.
     * 
     * @param fhirUrl           la URL del servidor FHIR.
     * @param subscriptionId    el identificador de la subscripción.
     * @param eventsSinceNumber el número del primer evento perdido.
     * @param eventsUntilNumber el número del último evento perdido.
     * @return la respuesta del servidor.
     */
    public SubscriptionStatus getLostEvents(String fhirUrl, String subscriptionId, Long eventsSinceNumber,
            Long eventsUntilNumber) {
        // Prepara los parámetros de entrada para realizar la consulta
        Parameters inputParams = new Parameters();
        inputParams.addParameter().setName("eventsSinceNumber").setValue(new Integer64Type(eventsSinceNumber));
        inputParams.addParameter().setName("eventsUntilNumber").setValue(new Integer64Type(eventsUntilNumber));

        // Realiza la consulta
        IGenericClient client = getClient(fhirUrl);
        Bundle bundle = (Bundle) client.operation().onInstance(new IdType("Subscription", subscriptionId))
                .named("$events").withParameters(inputParams).useHttpGet().returnResourceType(Bundle.class).execute();

        logger.info("Respuesta del servidor: {}", fhirContext.newJsonParser().encodeResourceToString(bundle));

        // Comprueba que tenga SubscriptionStatus y lo extrae
        if (bundle.getEntry().isEmpty() || !bundle.getEntryFirstRep().hasResource()
                || bundle.getEntryFirstRep().getResource().getClass() != SubscriptionStatus.class) {
            throw new RuntimeException("Mensaje incorrecto. Bundle sin SubscriptionStatus.");
        } else {
            return (SubscriptionStatus) bundle.getEntryFirstRep().getResource();
        }
    }

    /**
     * Obtiene el estado de la subscripción.
     * 
     * @param fhirUrl        la URL del servidor FHIR.
     * @param subscriptionId el identificador de la subscripción.
     * @return la respuesta del servidor.
     */
    public SubscriptionStatus getStatus(String fhirUrl, String subscriptionId) {
        // Realiza la consulta
        IGenericClient client = getClient(fhirUrl);
        Bundle bundle = (Bundle) client.operation().onInstance(new IdType("Subscription", subscriptionId))
                .named("$status").withNoParameters(Parameters.class).useHttpGet().returnResourceType(Bundle.class)
                .execute();

        logger.info("Respuesta del servidor: {}", fhirContext.newJsonParser().encodeResourceToString(bundle));

        // Comprueba que tenga SubscriptionStatus y lo extrae
        if (bundle.getEntry().isEmpty() || !bundle.getEntryFirstRep().hasResource()
                || bundle.getEntryFirstRep().getResource().getClass() != SubscriptionStatus.class) {
            throw new RuntimeException("Mensaje incorrecto. Bundle sin SubscriptionStatus.");
        } else {
            return (SubscriptionStatus) bundle.getEntryFirstRep().getResource();
        }
    }

    /**
     * Obtiene el SubscriptionStatus que contiene el mensaje.
     * 
     * @param mesagge el JSON que contiene el SubscriptionStatus.
     * @return el recurso el SubscriptionStatus.
     */
    public SubscriptionStatus getSubscriptionStatus(String mesagge) {
        Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, mesagge);

        // Comprueba que tenga SubscriptionStatus y lo extrae
        if (bundle.getEntry().isEmpty() || !bundle.getEntryFirstRep().hasResource()
                || bundle.getEntryFirstRep().getResource().getClass() != SubscriptionStatus.class) {
            throw new RuntimeException("Mensaje incorrecto. Bundle sin SubscriptionStatus.");
        } else {
            return (SubscriptionStatus) bundle.getEntryFirstRep().getResource();
        }
    }

    public Subscription updateSubscriptionStatus(String fhirUrl, String subscriptionId) {
        IGenericClient client = getClient(fhirUrl);

        String patchBody = "[{ \"op\": \"replace\", \"path\": \"/status\", \"value\": \"requested\" }]";

        // Realizar la operación PATCH
        MethodOutcome methodOutcome = client.patch().withBody(patchBody)
                .withId(new IdType("Subscription", subscriptionId)).encodedJson().preferResponseType(Subscription.class)
                .execute();

        Subscription subscription = (Subscription) methodOutcome.getResource();

        logger.info("Respuesta del servidor: {}", fhirContext.newJsonParser().encodeResourceToString(subscription));

        return subscription;
    }

}