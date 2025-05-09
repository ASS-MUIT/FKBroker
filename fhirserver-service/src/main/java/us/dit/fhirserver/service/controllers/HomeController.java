package us.dit.fhirserver.service.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import us.dit.fhirserver.service.entities.domain.EventDTO;
import us.dit.fhirserver.service.entities.domain.EventsDTO;
import us.dit.fhirserver.service.entities.domain.SubscriptionDTO;
import us.dit.fhirserver.service.entities.domain.SubscriptionTopicDTO;
import us.dit.fhirserver.service.services.EventService;
import us.dit.fhirserver.service.services.SubscriptionService;
import us.dit.fhirserver.service.services.SubscriptionTopicService;

/**
 * Controlador que gestiona las llamadas a los métodos principales.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Controller
public class HomeController {

    private final SubscriptionTopicService subscriptionTopicService;
    private final SubscriptionService subscriptionService;
    private final EventService eventService;

    @Autowired
    public HomeController(SubscriptionTopicService subscriptionTopicService, SubscriptionService subscriptionService,
            EventService eventService) {
        this.subscriptionTopicService = subscriptionTopicService;
        this.subscriptionService = subscriptionService;
        this.eventService = eventService;
    }

    /**
     * Maneja las solicitudes GET para obtener la página principal.
     * 
     * @param model el modelo de Spring para añadir atributos.
     * @return el nombre de la vista "index".
     */
    @GetMapping("/")
    public String getHomePage(Model model) {
        return "index";
    }

    @GetMapping("/subscriptions")
    public String getSubscriptionsPage(Model model) {

        List<SubscriptionTopicDTO> subscriptionTopics = subscriptionTopicService.getTopicsDTO();
        List<SubscriptionDTO> subscriptions = subscriptionService.getSubscriptionsDTO();

        model.addAttribute("subscriptionTopics", subscriptionTopics);
        model.addAttribute("subscriptions", subscriptions);

        return "subscriptions";
    }

    @GetMapping("/subscriptions/{idSubscription}/events")
    public String getEventsPage(Model model, @PathVariable Long idSubscription) {

        SubscriptionDTO subscription = subscriptionService.getSubscriptionDTO(idSubscription);
        SubscriptionTopicDTO subscriptionTopic = subscriptionTopicService.getTopicDTO(subscription.getIdTopic());
        List<EventDTO> events = eventService.getEventsDTO(idSubscription);

        model.addAttribute("subscription", subscription);
        model.addAttribute("topic", subscriptionTopic);
        model.addAttribute("events", events);

        return "events";
    }

    @PostMapping("/subscriptions/{idSubscription}/events")
    public String getEventFormPage(Model model, @PathVariable Long idSubscription, @RequestParam Long number) {

        SubscriptionDTO subscription = subscriptionService.getSubscriptionDTO(idSubscription);
        SubscriptionTopicDTO subscriptionTopic = subscriptionTopicService.getTopicDTO(subscription.getIdTopic());

        EventsDTO eventsDTO = new EventsDTO(idSubscription, (long) subscription.getLastEvent(), number);

        model.addAttribute("idSubscription", idSubscription);
        model.addAttribute("resource", subscriptionTopic.getResource());
        model.addAttribute("operation", subscriptionTopic.getOperation());
        model.addAttribute("events", eventsDTO);

        return "events-form";
    }

    @PostMapping("/subscriptions/{idSubscription}/events/add")
    public String addEvents(@PathVariable Long idSubscription, @ModelAttribute("events") EventsDTO eventsDTO) {

        eventService.sendEvents(idSubscription, eventsDTO.getEvents());

        return "redirect:/subscriptions/" + idSubscription + "/events";
    }

}
