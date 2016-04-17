package io.paradoxical.dropwizard.guice;

import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Environment;
import io.paradoxical.dropwizard.guice.admin.AdminResourceEnvironment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
public final class EnvironmentData {
    private final Environment environment;
    private final JerseyEnvironment jerseyEnvironment;
    private final MutableServletContextHandler serverContext;
    private final ServletEnvironment servletEnvironment;

    public static EnvironmentData admin(
        final AdminResourceEnvironment adminResourceEnvironment) {
        return new EnvironmentData(
            adminResourceEnvironment.environment(),
            adminResourceEnvironment.jerseyEnvironment(),
            adminResourceEnvironment.environment().getAdminContext(),
            adminResourceEnvironment.environment().admin());
    }

    public static EnvironmentData app(
        final Environment environment) {
        return new EnvironmentData(
            environment,
            environment.jersey(),
            environment.getApplicationContext(),
            environment.servlets());
    }
}
