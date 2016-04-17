package io.paradoxical.dropwizard.guice.admin;

import io.dropwizard.Configuration;

@FunctionalInterface
public interface AdminEnvironmentConfigurator {
    void configure(Configuration config, AdminResourceEnvironment adminResourceEnvironment);
}
