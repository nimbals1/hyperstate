package au.com.mountainpass.hyperstate.server;

import java.util.concurrent.CompletableFuture;

import au.com.mountainpass.hyperstate.client.RepositoryResolver;
import au.com.mountainpass.hyperstate.core.NavigationalRelationship;
import au.com.mountainpass.hyperstate.core.entities.CreatedEntity;
import au.com.mountainpass.hyperstate.core.entities.VanillaEntity;
import au.com.mountainpass.hyperstate.server.entities.HyperstateRootEntity;

public class HyperstateTestRootEntity extends HyperstateRootEntity {

    private RepositoryResolver resolver;

    protected HyperstateTestRootEntity() {
    }

    public HyperstateTestRootEntity(RepositoryResolver resolver,
            Class<? extends HyperstateController> controllerClass) {
        super(resolver, controllerClass);
        this.resolver = resolver;
    }

    public CompletableFuture<CreatedEntity> create(Class<?> type, String path,
            String title, String natures) {
        final VanillaEntity accounts = new VanillaEntity(resolver,
                this.getId() + path, title, "Accounts");
        return resolver.getRepository().save(accounts)
                .thenApplyAsync(entity -> {
                    accounts.setRepository(resolver.getRepository());

                    this.add(
                            new NavigationalRelationship(accounts, "accounts"));
                    return new CreatedEntity(entity);
                });

    }
}
