/**
 * 
 */
package us.dit.fkbroker.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.entities.KieServer;
import us.dit.fkbroker.repositories.KieServerRepository;
import us.dit.fkbroker.repositories.NotificationEPRepository;

/**
 * 
 */
@Service
public class Broker implements FhirClient, KieClient {
	@Autowired
	KieServerRepository kieRepo;
	@Autowired
	NotificationEPRepository notRepo;
	
	private static final Logger log = LogManager.getLogger();
	
	@Override
	public KieServer addServer(String url, String user, String pwd) {
		KieServer newKie=new KieServer(url,user,pwd);
		newKie=kieRepo.save(newKie);
		return newKie;
	}

	@Override
	public List<KieServer> getAllServers() {
		List<KieServer> servers=new ArrayList<KieServer>();
		
		for(KieServer kie:kieRepo.findAll()) {
			servers.add(kie);
		}
		
		return servers;
	}

	@Override
	public void sendSignal(String signal,String resourceId) {
		log.debug("Aquí falta el código para enviar la señal a todos los servidores kie");
		for(KieServer server:kieRepo.findAll()) {
			sendToServer(server,signal,resourceId);
		}		
		
	}
	private void sendToServer(KieServer server,String signalName,String resourceId) {
		log.debug("Aquí va el código para enviar a un único servidor kie");
		MarshallingFormat FORMAT = MarshallingFormat.JSON;
		KieServicesConfiguration conf;
		KieServicesClient kieServicesClient;
		ProcessServicesClient processClient;
	    conf = KieServicesFactory.newRestConfiguration(server.getUrl(),server.getUsu(),server.getPwd());
	    conf.setMarshallingFormat(FORMAT);
	    kieServicesClient = KieServicesFactory.newKieServicesClient(conf);
	    processClient=kieServicesClient.getServicesClient(ProcessServicesClient.class);
	    //Envío la señal a cada contenedor del servidor
	    KieContainerResourceList containersList = kieServicesClient.listContainers().getResult();
	    List<KieContainerResource> kieContainers = containersList.getContainers();
	    for (KieContainerResource container : kieContainers) {
	        processClient.signal(container.getContainerId(), signalName, resourceId);
	    }		    
	}
	

		

}
