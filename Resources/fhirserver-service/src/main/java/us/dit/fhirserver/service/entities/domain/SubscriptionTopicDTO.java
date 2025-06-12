package us.dit.fhirserver.service.entities.domain;

/**
 * Objeto del dominio que representa los datos de un tema de una subscripción,
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
public class SubscriptionTopicDTO {

    private Long id;
    private String name;
    private String resource;
    private String operation;

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

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
