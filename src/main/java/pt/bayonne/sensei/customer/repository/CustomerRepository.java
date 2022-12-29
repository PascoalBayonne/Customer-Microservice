package pt.bayonne.sensei.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.bayonne.sensei.customer.domain.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer,Long> {
}
