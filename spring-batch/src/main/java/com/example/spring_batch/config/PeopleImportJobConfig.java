package com.example.spring_batch.config;

import javax.sql.DataSource;

import java.time.LocalDate;

import com.example.spring_batch.model.Person;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.MongoItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.support.CompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PeopleImportJobConfig {

	private static final int CHUNK_SIZE = 1000;

	@Bean
	public FlatFileItemReader<Person> peopleReader() {
		return new FlatFileItemReaderBuilder<Person>()
				.name("peopleReader")
				.resource(new ClassPathResource("people-10000-dummy-data.csv"))
				.delimited()
				.delimiter(",")
				.quoteCharacter('"')
				.names("id", "userId", "firstName", "lastName", "sex", "email", "phone", "dateOfBirth", "jobTitle")
				.linesToSkip(1)
				.fieldSetMapper(fieldSet -> {
					Person person = new Person();
					person.setId(fieldSet.readLong("id"));
					person.setUserId(fieldSet.readString("userId"));
					person.setFirstName(fieldSet.readString("firstName"));
					person.setLastName(fieldSet.readString("lastName"));
					person.setSex(fieldSet.readString("sex"));
					person.setEmail(fieldSet.readString("email"));
					person.setPhone(fieldSet.readString("phone"));
					person.setDateOfBirth(LocalDate.parse(fieldSet.readString("dateOfBirth")));
					person.setJobTitle(fieldSet.readString("jobTitle"));
					return person;
				})
				.build();
	}

	@Bean
	public JdbcBatchItemWriter<Person> mysqlPeopleWriter(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Person>()
				.dataSource(dataSource)
				.sql("""
						INSERT INTO People (
						    id, user_id, first_name, last_name, sex, email, phone, date_of_birth, job_title
						) VALUES (
						    :id, :userId, :firstName, :lastName, :sex, :email, :phone, :dateOfBirth, :jobTitle
						)
						""")
				.beanMapped()
				.build();
	}

	@Bean
	public MongoItemWriter<Person> mongoPeopleWriter(MongoTemplate mongoTemplate) {
		MongoItemWriter<Person> writer = new MongoItemWriter<>(mongoTemplate);
		writer.setCollection("People");
		return writer;
	}

	@Bean
	public ItemWriter<Person> peopleWriter(JdbcBatchItemWriter<Person> mysqlPeopleWriter,
			MongoItemWriter<Person> mongoPeopleWriter) {
		return new CompositeItemWriterBuilder<Person>()
				.delegates(mysqlPeopleWriter, mongoPeopleWriter)
				.build();
	}

	@Bean
	public Step importPeopleStep(JobRepository jobRepository,
			PlatformTransactionManager transactionManager,
			FlatFileItemReader<Person> peopleReader,
			ItemWriter<Person> peopleWriter) {
		return new StepBuilder("importPeopleStep", jobRepository)
				.<Person, Person>chunk(CHUNK_SIZE, transactionManager)
				.reader(peopleReader)
				.writer(peopleWriter)
				.build();
	}

	@Bean
	public Job importPeopleJob(JobRepository jobRepository, Step importPeopleStep) {
		return new JobBuilder("importPeopleJob", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(importPeopleStep)
				.build();
	}

}
