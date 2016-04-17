package io.paradoxical.dropwizard.guice.admin;

import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyContainerHolder;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.glassfish.jersey.servlet.ServletContainer;

@Getter
@Accessors(fluent = true)
public final class AdminResourceEnvironment {
    private final DropwizardResourceConfig adminResourceConfig;
    private final JerseyContainerHolder jerseyContainerHolder;
    private final JerseyEnvironment jerseyEnvironment;
    private final Environment environment;

    public AdminResourceEnvironment(@NonNull final Environment environment) {
        this.environment = environment;
        adminResourceConfig = new DropwizardResourceConfig(environment.metrics());
        jerseyContainerHolder = new JerseyContainerHolder(new ServletContainer(adminResourceConfig));
        jerseyEnvironment = new JerseyEnvironment(jerseyContainerHolder, adminResourceConfig);
    }
}