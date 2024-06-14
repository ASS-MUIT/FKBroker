package us.dit.fkbroker.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import us.dit.fkbroker.entities.NotificationEP;
@Service
@Repository
public interface NotificationEPRepository extends CrudRepository<NotificationEP, Long> {

}
