package us.dit.fkbroker.service.services.kie;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.service.entities.db.Signal;
import us.dit.fkbroker.service.services.fhir.FhirService;

/**
 * Servicio para gestionar las operaciones sobre serviores y señales KIE.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Service
public class KieService {
    
    private static final Logger logger = LogManager.getLogger(FhirService.class);

    private final KieServerService kieServerService;
    private final SignalService signalService;

    /**
     * Constructor que inyecta los servicios {@link KieServerService} y
     * {@link SignalService}
     * 
     * @param kieServerService servicio utilizado para gestionar los servidores KIE.
     * @param signalService    servicio utilizado para gestionar las señales.
     */
    public KieService(KieServerService kieServerService, SignalService signalService) {
        this.kieServerService = kieServerService;
        this.signalService = signalService;
    }

    public void sendSignal(Long idTrigger, List<String> references) {
        // Se obtiene la señal que se debe enviar
        Optional<Signal> optionalSignal = signalService.getSignalByTrigger(idTrigger);
        if (optionalSignal.isPresent()) {
            // Se envía una señal a todos los servidores KIE por cada evento notificado
            for (String reference : references) {
                logger.info("Llamamos a sendsignal. Id del recurso: {}", reference);
                kieServerService.sendSignalToAllKieServers(optionalSignal.get().getName(), reference);
            }
        }
    }
}
