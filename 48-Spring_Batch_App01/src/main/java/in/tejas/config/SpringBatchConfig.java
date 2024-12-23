package in.tejas.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import in.tejas.entity.Customer;
import in.tejas.repository.CustomerRepository;
import lombok.AllArgsConstructor;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {
	

	@Autowired
	private CustomerRepository customerRepository;
	
	

	// step1: reading data from csv file...
	@Bean
	public FlatFileItemReader<Customer> customerReader() {

		FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();

		itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
		itemReader.setName("csv-reader");
		itemReader.setLinesToSkip(1);
		itemReader.setLineMapper(lineMapper());

		return itemReader;
	}
	
	

	// it returns linemapper obj
	private LineMapper<Customer> lineMapper() {

		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();

		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

		BeanWrapperFieldSetMapper<Customer> fieldMapper = new BeanWrapperFieldSetMapper<>();
		fieldMapper.setTargetType(Customer.class);

		DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

		lineMapper.setFieldSetMapper(fieldMapper);
		lineMapper.setLineTokenizer(lineTokenizer);

		return lineMapper;
	}

	
	
	// step2: processing csv file data using CustomerProcess class
	@Bean
	public CustomerProcess customerProcessor() {
		return new CustomerProcess();
	}

	// step3: writing data in database using CustomerRepository object
	@Bean
	public RepositoryItemWriter<Customer> customerWriter() {

		RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
		writer.setRepository(customerRepository);
		writer.setMethodName("save");

		return writer;
	}

	
	
	@Bean
	public Step step() {

		return new StepBuilder("step-1")
				.<Customer, Customer>chunk(10)
				.reader(customerReader())
				.processor(customerProcessor())
				.writer(customerWriter())
				.taskExecutor(taskExecutor())
				.build();
	}
	
	

	@Bean
	public Job job() {

		return new JobBuilder("customer-import")
				.flow(step())
				.end()
				.build();
	}
	
	

	@Bean
	public TaskExecutor taskExecutor() {

		SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
		asyncTaskExecutor.setConcurrencyLimit(10);

		return asyncTaskExecutor;
	}

}