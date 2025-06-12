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
import us.dit.fhirserver.service.services.fhir.EventService;
import us.dit.fhirserver.service.services.fhir.SubscriptionService;
import us.dit.fhirserver.service.services.fhir.SubscriptionTopicService;

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

    /**
     * Constructor que inyecta los servicios {@link SubscriptionTopicService},
     * {@link SubscriptionService} y {@link EventService}.
     * 
     * @param subscriptionTopicService servicio utilizado para gestionar los temas
     *                                 de las subscripciones.
     * @param subscriptionService      servicio utilizado para gestionar las
     *                                 subscripciones.
     * @param eventService             servicio utilizado para gestionar los
     *                                 eventos.
     */
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
     * @param model modelo de Spring para añadir atributos.
     * @return el nombre de la vista de la página principal.
     */
    @GetMapping("/")
    public String getHomePage(Model model) {
        return "index";
    }

    /**
     * Maneja las solicitudes GET para obtener la página principal de las
     * subscripciones.
     * 
     * @param model modelo de Spring para añadir atributos.
     * @return el nombre de la vista de la página principal de las subscripciones.
     */
    @GetMapping("/subscriptions")
    public String getSubscriptionsPage(Model model) {

        List<SubscriptionTopicDTO> subscriptionTopics = subscriptionTopicService.getTopicsDTO();
        List<SubscriptionDTO> subscriptions = subscriptionService.getSubscriptionsDTO();

        model.addAttribute("subscriptionTopics", subscriptionTopics);
        model.addAttribute("subscriptions", subscriptions);

        return "subscriptions";
    }

    /**
     * Maneja las solicitudes GET para obtener la página del detalle de eventos de
     * una subscripción.
     * 
     * @param model          modelo de Spring para añadir atributos.
     * @param idSubscription identificador de la subscripción.
     * @return el nombre de la vista de la página del detalle de eventos de una
     *         subscripción.
     */
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

    /**
     * Maneja las solicitudes POST para obtener el formulario de creación de
     * eventos.
     * 
     * @param model          modelo de Spring para añadir atributos.
     * @param idSubscription identificador de la subscripción.
     * @param number         numero de eventos con los que se debe crear el
     *                       formulario.
     * @return el nombre de la vista del formulario de creación de eventos.
     */
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

    /**
     * Maneja las solicitudes POST para añadir eventos en una subscripción.
     * 
     * @param model          el modelo de Spring para añadir atributos.
     * @param idSubscription identificador de la subscripción.
     * @param eventsDTO      listado de eventos que se deben crear y notificar.
     * @return redirecciona a la página del detalle de eventos de una subscripción.
     */
    @PostMapping("/subscriptions/{idSubscription}/events/add")
    public String addEvents(@PathVariable Long idSubscription, @ModelAttribute("events") EventsDTO eventsDTO) {

        eventService.sendEvents(idSubscription, eventsDTO.getEvents());

        return "redirect:/subscriptions/" + idSubscription + "/events";
    }

}
