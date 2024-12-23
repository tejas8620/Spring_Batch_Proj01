package in.tejas.config;

import org.springframework.batch.item.ItemProcessor;

import in.tejas.entity.Customer;

public class CustomerProcess implements ItemProcessor<Customer, Customer>{

	@Override
	public Customer process(Customer item) throws Exception {
		
		
		//logic to sort...
		
		return item;
	}

}
