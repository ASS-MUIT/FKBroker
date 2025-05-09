package us.dit.fhirserver.service.entities.domain;

public class SubscriptionDTO {

    private Long id;
    private String endpoint;
    private Integer heartbeatPeriod;
    private String status;
    private Integer lastEvent;
    private Long idTopic;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getHeartbeatPeriod() {
        return heartbeatPeriod;
    }

    public void setHeartbeatPeriod(Integer heartbeatPeriod) {
        this.heartbeatPeriod = heartbeatPeriod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(Integer lastEvent) {
        this.lastEvent = lastEvent;
    }

    public Long getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(Long idTopic) {
        this.idTopic = idTopic;
    }
}
