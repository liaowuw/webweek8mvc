package repository;

import io.ebean.*;
import models.Person;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class PersonRepository {

	private final EbeanServer ebeanServer;
	private final DatabaseExecutionContext executionContext;

	@Inject
	public PersonRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
		this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
		this.executionContext = executionContext;
	}

	/**
	 * Return a paged list of Person
	 *
	 * @param page     Page to display
	 * @param pageSize Number of Persons per page
	 * @param sortBy   Person property used for sorting
	 * @param order    Sort order (either or asc or desc)
	 * @param filter   Filter applied on the name column
	 */
	public CompletionStage<PagedList<Person>> page(int page, int pageSize, String sortBy, String order, String filter) {
		return supplyAsync(() ->
				ebeanServer.find(Person.class).where()
					.ilike("name", "%" + filter + "%")
					.orderBy(sortBy + " " + order)
					.fetch("sex")
					.setFirstRow(page * pageSize)
					.setMaxRows(pageSize)
					.findPagedList(), executionContext);
	}

	public CompletionStage<Optional<Person>> lookup(Long id) {
		return supplyAsync(() -> Optional.ofNullable(ebeanServer.find(Person.class).setId(id).findOne()), executionContext);
	}

	public CompletionStage<Optional<Long>> update(Long id, Person newPersonData) {
		return supplyAsync(() -> {
			Transaction txn = ebeanServer.beginTransaction();
			Optional<Long> value = Optional.empty();
			try {
				Person savedPerson = ebeanServer.find(Person.class).setId(id).findOne();
				if (savedPerson != null) {
					savedPerson.sex = newPersonData.sex;
					savedPerson.age = newPersonData.age;
					savedPerson.email = newPersonData.email;
					savedPerson.name = newPersonData.name;

					savedPerson.update();
					txn.commit();
					value = Optional.of(id);
				}
			} finally {
				txn.end();
			}
			return value;
		}, executionContext);
	}

	public CompletionStage<Optional<Long>>  delete(Long id) {
		return supplyAsync(() -> {
			try {
				final Optional<Person> personOptional = Optional.ofNullable(ebeanServer.find(Person.class).setId(id).findOne());
				personOptional.ifPresent(Model::delete);
				return personOptional.map(c -> c.id);
			} catch (Exception e) {
				return Optional.empty();
			}
		}, executionContext);
	}

	public CompletionStage<Long> insert(Person person) {
		return supplyAsync(() -> {
			 person.id = System.currentTimeMillis(); // not ideal, but it works
			 ebeanServer.insert(person);
			 return person.id;
		}, executionContext);
	}
}
