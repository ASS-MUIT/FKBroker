/**
 * 
 */
package us.dit.fkbroker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import us.dit.fkbroker.entities.KieServer;
import us.dit.fkbroker.services.Broker;


/**
 * 
 */
@Controller
@RequestMapping("/kieserver")
public class KieServerController {
	@Autowired
	Broker broker;
	@GetMapping()
	public String getAllKieServers(Model model) {
		model.addAttribute("servers", broker.getAllServers());
		return "kieServers";
	}
	@PostMapping()
	public String newKieServer(@RequestBody KieServer newKie,Model model) {
		model.addAttribute("server", broker.addServer(newKie.getUrl(),newKie.getUsu(), newKie.getPwd()));
		
		return "kieserver";
		
	}
	

}
