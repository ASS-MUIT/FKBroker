package us.dit.fkbroker.service.entities.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entidad que representa los datos de configuración de un servidor fhir
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
@Entity(name = "FHIR_SERVERS")
public class FhirServer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "URL")
    private String url;
    @Column(name = "HEARTBEAT")
    private Boolean heartbeat;
    @Column(name = "QUERY_OPERATIONS")
    private Boolean queryOperations;

    public FhirServer() {
        super();
    }

    public FhirServer(Long id, String name, String url, Boolean heartbeat, Boolean queryOperations) {
        super();
        this.id = id;
        this.name = name;
        this.url = url;
        // Comprueba si es null, en ese caso guarda false
        this.heartbeat = (heartbeat != null) ? heartbeat : false;
        this.queryOperations = (queryOperations != null) ? queryOperations : false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Boolean heartbeat) {
        // Comprueba si es null, en ese caso guarda false
        this.heartbeat = (heartbeat != null) ? heartbeat : false;
    }

    public Boolean getQueryOperations() {
        return queryOperations;
    }

    public void setQueryOperations(Boolean queryOperations) {
        this.queryOperations = queryOperations;
    }

}
