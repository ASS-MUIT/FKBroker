package us.dit.fkbroker.service.services.mapper;

import org.springframework.stereotype.Component;

import us.dit.fkbroker.service.entities.db.Signal;
import us.dit.fkbroker.service.entities.db.Trigger;
import us.dit.fkbroker.service.entities.domain.SignalDetails;

/**
 * Componente que transforma entidades en objetos del dominio y viceversa.
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Component
public class SignalMapper {

    /**
     * Transforma un objeto {@link Signal} y {@link Trigger} en uno
     * {@link SignalDetails}
     * 
     * @param signal  entidad con datos de la señal que se desea transformar en
     *                {@link SignalDetails}.
     * @param trigger entidad con datos del trigger que se desea transformar en
     *                {@link SignalDetails}.
     * @return el objeto {@link SignalDetails}.
     */
    public SignalDetails toDetails(Signal signal, Trigger trigger) {
        SignalDetails signalDetails = new SignalDetails();
        signalDetails.setId(signal.getId());
        signalDetails.setResource(trigger.getResource());
        signalDetails.setInteraction(trigger.getInteraction());
        signalDetails.setName(signal.getName());
        return signalDetails;
    }

}
