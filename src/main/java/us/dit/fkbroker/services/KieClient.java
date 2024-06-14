/**
 * 
 */
package us.dit.fkbroker.services;

import java.util.List;

import us.dit.fkbroker.entities.KieServer;

/**
 * 
 */
public interface KieClient {
	/**
	 * Incluir un nuevo servidor kie en el broker
	 * @param url
	 * @param user
	 * @param pwd
	 * @return The id of the new server
	 */
	KieServer addServer(String url,String user,String pwd);
	/**
	 * Listado completo de los servidores kie configurados en el broker
	 * @return
	 */
	List<KieServer> getAllServers();
	
	/**
	 * Envía la señal a todos los servidores kie configurados, como evento el resourceId
	 * @param signal nombre de la señal que se va a enviar a los servidores
	 * @param resourceId evento de la señal enviada (mensaje)
	 */
	void sendSignal(String signal, String resourceId);
	

}
