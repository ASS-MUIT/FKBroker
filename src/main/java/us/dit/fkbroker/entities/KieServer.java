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
public class KieServer {
	public KieServer() {
		super();
	}	
	public KieServer(String url, String usu, String pwd) {
		super();
		this.url = url;
		this.usu = usu;
		this.pwd = pwd;
	}
	@Id
	private String url;
	private String usu;
	private String pwd;

	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsu() {
		return usu;
	}
	public void setUsu(String usu) {
		this.usu = usu;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
}
