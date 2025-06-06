package us.dit.fkbroker.service.entities.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "ID_TOPIC")
    private String idTopic;

    @ManyToOne
    @JoinColumn(name = "ID_SERVER")
    private FhirServer server;

    @ManyToOne
    @JoinColumn(name = "ID_TRIGGER")
    private Trigger trigger;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdTopic() {
        return idTopic;
    }

    public void setIdTopic(String idTopic) {
        this.idTopic = idTopic;
    }

    public FhirServer getServer() {
        return server;
    }

    public void setServer(FhirServer server) {
        this.server = server;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }
}
