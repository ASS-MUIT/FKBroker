/**
 * 
 */
package us.dit.fkbroker.entities;

import java.util.List;
import java.util.Map;

import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;

/**
 * 
 */
@Entity
public class NotificationEP {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String resource;
	private String interaction;
	private String signalName;
	@OneToMany
	private List<Subscription> subscriptions;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getInteraction() {
		return interaction;
	}
	public void setInteraction(String interaction) {
		this.interaction = interaction;
	}
	public String getSignalName() {
		return signalName;
	}
	public void setSignalName(String signalName) {
		this.signalName = signalName;
	}
	public List<Subscription> getSubscritions() {
		return subscriptions;
	}
	public void setSubscritions(List<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
}
