package us.dit.fkbroker.service.entities.domain;

/**
 * Entidad con el detalle de servidores FHIR
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
public class FhirServerDetails {

    private Long id;
    private String name;
    private String url;
    private Boolean heartbeat;
    private Boolean queryOperations;

    public FhirServerDetails() {
        super();
    }
    
    public FhirServerDetails(Long id, String name, String url, Boolean heartbeat, Boolean queryOperations) {
        super();
        this.id = id;
        this.name = name;
        this.url = url;
        this.heartbeat = heartbeat;
        this.queryOperations = queryOperations;
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
        this.heartbeat = heartbeat;
    }

    public Boolean getQueryOperations() {
        return queryOperations;
    }

    public void setQueryOperations(Boolean queryOperations) {
        this.queryOperations = queryOperations;
    }

}
