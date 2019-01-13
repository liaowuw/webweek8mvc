package repository;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Sex;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 *
 */
public class SexRepository {

	private final EbeanServer ebeanServer;
	private final DatabaseExecutionContext executionContext;

	@Inject
	public SexRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
		this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
		this.executionContext = executionContext;
	}

	public CompletionStage<Map<String, String>> options() {
		return supplyAsync(() -> ebeanServer.find(Sex.class).orderBy("name").findList(), executionContext)
				.thenApply(list -> {
					HashMap<String, String> options = new LinkedHashMap<String, String>();
					for (Sex c : list) {
						options.put(c.id.toString(), c.name);
					}
					return options;
				});
	}

}
