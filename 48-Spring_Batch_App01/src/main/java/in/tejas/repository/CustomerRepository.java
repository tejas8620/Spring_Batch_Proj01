package in.tejas.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import in.tejas.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Serializable>{

}
