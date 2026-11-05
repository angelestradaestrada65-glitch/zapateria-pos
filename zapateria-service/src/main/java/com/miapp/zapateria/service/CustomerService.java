package com.miapp.zapateria.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.miapp.zapateria.dto.CustomerFormDTO;
import com.miapp.zapateria.model.Customer;
import com.miapp.zapateria.repository.CustomerRepository;

@Service
public class CustomerService {

	private final CustomerRepository customerRepository;

	public CustomerService(CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	public List<Customer> listAll() {
		return customerRepository.findAll();
	}

	public Customer findById(Long id) {
		return customerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("El cliente no existe"));
	}

	public Customer createCustomer(CustomerFormDTO form) {
		validatePhone(form.getPhone(), null);

		Customer customer = new Customer();
		customer.setName(form.getName());
		customer.setPhone(form.getPhone());
		customer.setCredit(form.isCredit());

		return customerRepository.save(customer);
	}

	public Customer updateCustomer(Long id, CustomerFormDTO form) {
		Customer customer = findById(id);
		validatePhone(form.getPhone(), id);

		customer.setName(form.getName());
		customer.setPhone(form.getPhone());
		customer.setCredit(form.isCredit());

		return customerRepository.save(customer);
	}

	public void deleteCustomer(Long id) {
		try {
			customerRepository.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new IllegalArgumentException("No se puede borrar: el cliente ya tiene ventas registradas");
		}
	}

	private void validatePhone(String phone, Long currentId) {
		customerRepository.findByPhone(phone).ifPresent(existing -> {
			if (!existing.getId().equals(currentId)) {
				throw new IllegalArgumentException("Ya existe un cliente con ese telefono");
			}
		});
	}
}
