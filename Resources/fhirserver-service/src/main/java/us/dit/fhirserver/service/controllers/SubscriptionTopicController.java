package us.dit.fhirserver.service.controllers;

import org.hl7.fhir.r5.model.SubscriptionTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import us.dit.fhirserver.service.services.fhir.SubscriptionTopicService;

/**
 * Controlador que gestiona las operaciones spbre el recurso FHIR
 * {@link SubscriptionTopic}.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@RestController
@RequestMapping("/fhir/SubscriptionTopic")
public class SubscriptionTopicController {

    private final SubscriptionTopicService subscriptionTopicService;

    /**
     * Constructor que inyecta el servicio {@link SubscriptionTopicService}.
     * 
     * @param subscriptionTopicService servicio utilizado para gestionar los temas
     *                                 de las subscripciones.
     */
    @Autowired
    public SubscriptionTopicController(SubscriptionTopicService subscriptionTopicService) {
        this.subscriptionTopicService = subscriptionTopicService;
    }

    /**
     * Maneja las solicitudes POST para crear un nuevo recurso FHIR
     * {@link SubscriptionTopic}.
     * 
     * @param message mensaje recibido con los datos del tema de la subscripción.
     * @return la respuesta HTTP correspondiente con la información del tema de la
     *         subscripción creada.
     */
    @PostMapping
    public ResponseEntity<String> addSubscriptionTopic(@RequestBody String message) {

        String response = subscriptionTopicService.saveTopic(message);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    /**
     * Maneja las solicitudes GET para obtener un recurso FHIR
     * {@link SubscriptionTopic}.
     * 
     * @param id identificador del tema de la subscripción a obtener.
     * @return la respuesta HTTP correspondiente con la información del tema de la
     *         subscripción.
     */
    @GetMapping("/{id}")
    public ResponseEntity<String> getSubscriptionTopic(@PathVariable Long id) {

        String response = subscriptionTopicService.getTopic(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    /**
     * Maneja las solicitudes GET para obtener todos los recursos FHIR
     * {@link SubscriptionTopic} del servidor.
     * 
     * @return la respuesta HTTP correspondiente con la información de todos los
     *         temas de las subscripciones.
     */
    @GetMapping
    public ResponseEntity<String> getSubscriptionTopics() {

        String response = subscriptionTopicService.getTopics();

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    /**
     * Maneja las solicitudes DELETE para eliminar un recurso FHIR
     * {@link SubscriptionTopic}.
     * 
     * @param id identificador del tema de la subscripción a eliminar.
     * @return la respuesta HTTP correspondiente con la información del tema de la
     *         subscripción eliminada.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscriptionTopic(@PathVariable Long id) {

        String response = subscriptionTopicService.deleteTopic(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

}
