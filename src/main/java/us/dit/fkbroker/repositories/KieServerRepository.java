/**
 * https://spring.io/guides/gs/accessing-data-rest
 * 
 */
package us.dit.fkbroker.repositories;




import us.dit.fkbroker.entities.KieServer;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.data.repository.CrudRepository;

@Service
@Repository
public interface KieServerRepository extends CrudRepository<KieServer,Long> {
	KieServer findByUrl(String url);

}
