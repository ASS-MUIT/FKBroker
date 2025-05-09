package us.dit.fhirserver.service.controllers;

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

import us.dit.fhirserver.service.services.SubscriptionTopicService;

@RestController
@RequestMapping("/fhir/SubscriptionTopic")
public class SubscriptionTopicController {

    private final SubscriptionTopicService subscriptionTopicService;

    @Autowired
    public SubscriptionTopicController(SubscriptionTopicService subscriptionTopicService) {
        this.subscriptionTopicService = subscriptionTopicService;
    }

    @PostMapping
    public ResponseEntity<String> addSubscriptionTopic(@RequestBody String message) {

        String response = subscriptionTopicService.saveTopic(message);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getSubscriptionTopic(@PathVariable Long id) {

        String response = subscriptionTopicService.getTopic(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    @GetMapping
    public ResponseEntity<String> getSubscriptionTopics() {

        String response = subscriptionTopicService.getTopics();

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscriptionTopic(@PathVariable Long id) {

        String response = subscriptionTopicService.deleteTopic(id);

        return ResponseEntity.ok().contentType(MediaType.valueOf("application/fhir+json")).body(response);
    }

}
