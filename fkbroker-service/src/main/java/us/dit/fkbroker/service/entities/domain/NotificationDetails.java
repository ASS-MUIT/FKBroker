package us.dit.fkbroker.service.entities.domain;

import java.util.List;

public class NotificationDetails {

    Boolean hasNewEvents;
    Long lastEvent;
    List<String> referenceEvents;

    public Boolean getHasNewEvents() {
        return hasNewEvents;
    }

    public void setHasNewEvents(Boolean hasNewEvents) {
        this.hasNewEvents = hasNewEvents;
    }

    public Long getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(Long lastEvent) {
        this.lastEvent = lastEvent;
    }

    public List<String> getReferenceEvents() {
        return referenceEvents;
    }

    public void setReferenceEvents(List<String> referenceEvents) {
        this.referenceEvents = referenceEvents;
    }

}
