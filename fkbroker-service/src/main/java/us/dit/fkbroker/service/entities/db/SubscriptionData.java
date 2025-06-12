/**
*  This file is part of FKBroker - Broker sending signals to KIEServers from FHIR notifications.
*  Copyright (C) 2024  Universidad de Sevilla/Departamento de Ingeniería Telemática
*
*  FKBroker is free software: you can redistribute it and/or
*  modify it under the terms of the GNU General Public License as published
*  by the Free Software Foundation, either version 3 of the License, or (at
*  your option) any later version.
*
*  FKBroker is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
*  Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with FKBroker. If not, see <https://www.gnu.org/licenses/>.
**/
package us.dit.fkbroker.service.entities.db;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Entidad que representa los datos de las subscripciones
 * 
 * @author josperbel
 * @version 1.0
 * @date Abr 2025
 */
@Entity(name = "SUBSCRIPTIONS")
public class SubscriptionData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;
    @Column(name = "ID_SUBSCRIPTION")
    private String idSubscription;
    @Column(name = "EVENTS")
    private Long events;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "UPDATED")
    private Date updated;

    @ManyToOne
    @JoinColumn(name = "ID_SERVER")
    private FhirServer server;

    @ManyToOne
    @JoinColumn(name = "ID_TOPIC")
    private Topic topic;

    public SubscriptionData() {

    }

    public SubscriptionData(FhirServer server, Topic topic) {
        this.server = server;
        this.topic = topic;
        this.events = 0l;
        this.updated = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdSubscription() {
        return idSubscription;
    }

    public void setIdSubscription(String idSubscription) {
        this.idSubscription = idSubscription;
    }

    public Long getEvents() {
        return events;
    }

    public void setEvents(Long events) {
        this.events = events;
    }

    public FhirServer getServer() {
        return server;
    }

    public void setServer(FhirServer server) {
        this.server = server;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }
}
