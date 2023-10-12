package pt.bayonne.sensei.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.bayonne.sensei.customer.domain.OutboxMessage;

import java.util.List;

@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    List<OutboxMessage> findTop10BySentOrderByIdAsc(boolean sent);
}
