package us.dit.fhirserver.service.entities.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entidad que representa los datos de un evento,
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Entity(name = "EVENTS")
public class EventDB {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;
    @Column(name = "ID_SUBSCRIPTION")
    private Long idSubscription;
    @Column(name = "NUMBER")
    private Long number;
    @Column(name = "ID_RESOURCE")
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
