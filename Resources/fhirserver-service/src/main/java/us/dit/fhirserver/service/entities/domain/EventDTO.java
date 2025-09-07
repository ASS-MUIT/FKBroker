package us.dit.fhirserver.service.entities.domain;

/**
 * Objeto del dominio que representa los datos de un evento,
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
public class EventDTO {

    private Long number;
    private Long idResource;

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Long getIdResource() {
        return idResource;
    }

    public void setIdResource(Long idResource) {
        this.idResource = idResource;
    }
}
