package controllers;

import models.Person;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import repository.SexRepository;
import repository.PersonRepository;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Manage a database of Persons
 */
public class HomeController extends Controller {

		private final PersonRepository personRepository;
		private final SexRepository sexRepository;
		private final FormFactory formFactory;
		private final HttpExecutionContext httpExecutionContext;

		@Inject
		public HomeController(FormFactory formFactory,
													PersonRepository personRepository,
													SexRepository sexRepository,
													HttpExecutionContext httpExecutionContext) {
				this.personRepository = personRepository;
				this.formFactory = formFactory;
				this.sexRepository = sexRepository;
				this.httpExecutionContext = httpExecutionContext;
		}

		/**
		 * This result directly redirect to application home.
		 */
		private Result GO_HOME = Results.redirect(
						routes.HomeController.list(0, "name", "asc", "")
		);

		/**
		 * Handle default path requests, redirect to Persons list
		 */
		public Result index() {
				return GO_HOME;
		}

		/**
		 * Display the paginated list of Persons.
		 *
		 * @param page   Current page number (starts from 0)
		 * @param sortBy Column to be sorted
		 * @param order  Sort order (either asc or desc)
		 * @param filter Filter applied on Person names
		 */
		public CompletionStage<Result> list(int page, String sortBy, String order, String filter) {
				// Run a db operation in another thread (using DatabaseExecutionContext)
				return personRepository.page(page, 10, sortBy, order, filter).thenApplyAsync(list -> {
						// This is the HTTP rendering thread context
						return ok(views.html.list.render(list, sortBy, order, filter));
				}, httpExecutionContext.current());
		}

		/**
		 * Display the 'edit form' of a existing Person.
		 *
		 * @param id Id of the Person to edit
		 */
		public CompletionStage<Result> edit(Long id) {

				// Run a db operation in another thread (using DatabaseExecutionContext)
				CompletionStage<Map<String, String>> sexFuture = sexRepository.options();

				// Run the lookup also in another thread, then combine the results:
				return personRepository.lookup(id).thenCombineAsync(sexFuture, (personOptional, sex) -> {
						// This is the HTTP rendering thread context
						Person c = personOptional.get();
						Form<Person> personForm = formFactory.form(Person.class).fill(c);
						return ok(views.html.editForm.render(id, personForm, sex));
				}, httpExecutionContext.current());
		}

		/**
		 * Handle the 'edit form' submission
		 *
		 * @param id Id of the Person to edit
		 */
		public CompletionStage<Result> update(Long id) throws PersistenceException {
				Form<Person> personForm = formFactory.form(Person.class).bindFromRequest();
				if (personForm.hasErrors()) {
						// Run sex db operation and then render the failure case
						return sexRepository.options().thenApplyAsync(sex -> {
								// This is the HTTP rendering thread context
								return badRequest(views.html.editForm.render(id, personForm, sex));
						}, httpExecutionContext.current());
				} else {
						Person newPersonData = personForm.get();
						// Run update operation and then flash and then redirect
						return personRepository.update(id, newPersonData).thenApplyAsync(data -> {
								// This is the HTTP rendering thread context
								flash("success", "Person " + newPersonData.name + " has been updated");
								return GO_HOME;
						}, httpExecutionContext.current());
				}
		}

		/**
		 * Display the 'new Person form'.
		 */
		public CompletionStage<Result> create() {
				Form<Person> personForm = formFactory.form(Person.class);
				// Run sex db operation and then render the form
				return sexRepository.options().thenApplyAsync((Map<String, String> sex) -> {
						// This is the HTTP rendering thread context
						return ok(views.html.createForm.render(personForm, sex));
				}, httpExecutionContext.current());
		}

		/**
		 * Handle the 'new Person form' submission
		 */
		public CompletionStage<Result> save() {
				Form<Person> personForm = formFactory.form(Person.class).bindFromRequest();
				if (personForm.hasErrors()) {
						// Run sex db operation and then render the form
						return sexRepository.options().thenApplyAsync(sex -> {
								// This is the HTTP rendering thread context
								return badRequest(views.html.createForm.render(personForm, sex));
						}, httpExecutionContext.current());
				}

				Person person = personForm.get();
				// Run insert db operation, then redirect
				return personRepository.insert(person).thenApplyAsync(data -> {
						// This is the HTTP rendering thread context
						flash("success", "Person " + person.name + " has been created");
						return GO_HOME;
				}, httpExecutionContext.current());
		}

		/**
		 * Handle Person deletion
		 */
		public CompletionStage<Result> delete(Long id) {
				// Run delete db operation, then redirect
				return personRepository.delete(id).thenApplyAsync(v -> {
						// This is the HTTP rendering thread context
						flash("success", "Person has been deleted");
						return GO_HOME;
				}, httpExecutionContext.current());
		}

}
						
