package us.dit.fhirserver.service.controllers;

import org.hl7.fhir.r5.model.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import us.dit.fhirserver.service.services.fhir.SubscriptionService;

/**
 * Controlador que gestiona las operaciones spbre el recurso FHIR
 * {@link Subscription}.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@RestController
@RequestMapping("/fhir/Subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Constructor que inyecta el servicio {@link SubscriptionService}.
     * 
     * @param subscriptionService servicio utilizado para gestionar las
     *                            subscripciones.
     */
    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Maneja las solicitudes POST para crear un nuevo recurso FHIR
     * {@link Subscription}.
     * 
     * @param message mensaje recibido con los datos de la subscripción.
     * @return la respuesta HTTP correspondiente con la información de la
     *         subscripción creada.
     */
    @PostMapping
    public ResponseEntity<String> addSubscription(@RequestBody String message) {

        String response = subscriptionService.saveSubscription(message);

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.valueOf("application/fhir+json"))
                .body(response);
    }

    /**
     * Maneja las solicitudes GET para obtener un recurso FHIR {@link Subscription}.
     * 
     * @param id identificador de la subscripción a obtener.
     * @return la respuesta HTTP correspondiente con la información de la
     *         subscripción.
     */
    @GetMapping("/{id}")
    public ResponseEntity<String> getSubscription(@PathVariable Long id) {

        String response = subscriptionService.getSubscription(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    /**
     * Maneja las solicitudes GET para obtener todos los recursos FHIR
     * {@link Subscription} del servidor.
     * 
     * @return la respuesta HTTP correspondiente con la información de todas las
     *         subscripciones.
     */
    @GetMapping
    public ResponseEntity<String> getSubscriptions() {

        String response = subscriptionService.getSubscriptions();

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    /**
     * Maneja las solicitudes DELETE para eliminar un recurso FHIR
     * {@link Subscription}.
     * 
     * @param id identificador de la subscripción a eliminar.
     * @return la respuesta HTTP correspondiente con la información de la
     *         subscripción eliminada.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscription(@PathVariable Long id) {

        String response = subscriptionService.deleteSubscription(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    /**
     * Maneja las solicitudes PATCH para actualizar un recurso FHIR
     * {@link Subscription}.
     * 
     * @param id identificador de la subscripción a eliminar.
     * @return la respuesta HTTP correspondiente con la información de la
     *         subscripción actualizada.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<String> updateSubscription(@PathVariable Long id) {

        String response = subscriptionService.updateSubscription(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    /**
     * Maneja las solicitudes GET para obtener la información del estado de un
     * recurso FHIR {@link Subscription}.
     * 
     * @param id identificador de la subscripción.
     * @return la respuesta HTTP correspondiente con la información del estado de la
     *         subscripción.
     */
    @GetMapping("/{id}/$status")
    public ResponseEntity<String> getStatus(@PathVariable Long id) {

        String response = subscriptionService.getStatus(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    /**
     * Maneja las solicitudes GET para obtener la información de un rango de eventos
     * de un recurso FHIR {@link Subscription}.
     * 
     * @param id                identificador de la subscripción.
     * @param eventsSinceNumber numero del evento inferior que se desea obtener.
     * @param eventsUntilNumber numero del evento superior que se desea obtener.
     * @return la respuesta HTTP correspondiente con la información del rango de
     *         eventos de la subscripción.
     */
    @GetMapping("/{id}/$events")
    public ResponseEntity<String> getEvents(@PathVariable Long id,
            @RequestParam(required = false) Long eventsSinceNumber,
            @RequestParam(required = false) Long eventsUntilNumber) {

        String response = subscriptionService.getEvents(id, eventsSinceNumber, eventsUntilNumber);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

}
