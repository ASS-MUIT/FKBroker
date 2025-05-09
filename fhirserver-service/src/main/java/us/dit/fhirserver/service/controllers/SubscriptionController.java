package us.dit.fhirserver.service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import us.dit.fhirserver.service.services.SubscriptionService;

@RestController
@RequestMapping("/fhir/Subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService; 

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<String> addSubscription(@RequestBody String message) {

        String response = subscriptionService.saveSubscription(message);

        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.valueOf("application/fhir+json"))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getSubscription(@PathVariable Long id) {

        String response = subscriptionService.getSubscription(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    @GetMapping
    public ResponseEntity<String> getSubscriptions() {

        String response = subscriptionService.getSubscriptions();

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscription(@PathVariable Long id) {

        String response = subscriptionService.deleteSubscription(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    @GetMapping("/{id}/$status")
    public ResponseEntity<String> getStatus(@PathVariable Long id) {

        String response = subscriptionService.getStatus(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    @GetMapping("/{id}/$events")
    public ResponseEntity<String> getEvents(@PathVariable Long id,
            @RequestParam(required = false) Long eventsSinceNumber,
            @RequestParam(required = false) Long eventsUntilNumber) {

        String response = subscriptionService.getEvents(id, eventsSinceNumber, eventsUntilNumber);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

}
