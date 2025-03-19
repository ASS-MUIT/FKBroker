package us.dit.fkbroker.service.entities.domain;

/**
 * Entidad con el detalle de servidores FHIR
 * 
 * @author josperbel
 * @version 1.0
 * @date Mar 2025
 */
public class FhirServerDTO {

    private Long id;
    private String url;

    public FhirServerDTO(Long id, String url) {
        super();
        this.id = id;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
