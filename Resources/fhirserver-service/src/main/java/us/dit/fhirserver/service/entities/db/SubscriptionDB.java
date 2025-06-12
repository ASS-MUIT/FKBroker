package us.dit.fhirserver.service.entities.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Entidad que representa los datos de una subscripción,
 * 
 * @author josperbel
 * @version 1.0
 * @date May 2025
 */
@Entity(name = "SUBSCRIPTIONS")
public class SubscriptionDB {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;
    @Column(name = "ENDPOINT")
    private String endpoint;
    @Column(name = "HEARTBEAT_PERIOD")
    private Integer heartbeatPeriod;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "LAST_EVENT")
    private Integer lastEvent;
    @ManyToOne
    @JoinColumn(name = "ID_TOPIC")
    private SubscriptionTopicDB topic;

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

    public SubscriptionTopicDB getTopic() {
        return topic;
    }

    public void setTopic(SubscriptionTopicDB topic) {
        this.topic = topic;
    }
}
