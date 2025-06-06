package us.dit.fkbroker.service.entities.domain;

import java.util.Date;

public class SubscriptionEntry {

    private String id;
    private String idTopic;
    private Long events;
    private String status;
    private Date lastUpdate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(String idTopic) {
        this.idTopic = idTopic;
    }

    public Long getEvents() {
        return events;
    }

    public void setEvents(Long events) {
        this.events = events;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
