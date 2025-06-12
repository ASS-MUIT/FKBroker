package us.dit.fhirserver.service.entities.domain;

/**
 * Objeto del dominio que representa los datos de un evento,
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
public class EventDTO {

    private Long id;
    private Long number;
    private Long idSubscription;
    private Long idResource;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Long getIdSubscription() {
        return idSubscription;
    }

    public void setIdSubscription(Long idSubscription) {
        this.idSubscription = idSubscription;
    }

    public Long getIdResource() {
        return idResource;
    }

    public void setIdResource(Long idResource) {
        this.idResource = idResource;
    }
}
