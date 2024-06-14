/**
 * 
 */
package us.dit.fkbroker.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;


/**
 * 
 */
@Entity
public class Subscription {
	@Id
	String url;
}
