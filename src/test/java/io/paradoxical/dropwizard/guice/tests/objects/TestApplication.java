package io.paradoxical.dropwizard.guice.tests.objects;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.paradoxical.dropwizard.guice.GuiceEnvironmentConfiguration;
import io.paradoxical.dropwizard.guice.bundles.GuiceBundle;
import io.paradoxical.dropwizard.guice.AutoConfig;

public class TestApplication extends Application<Configuration> {

    @Override
    public void initialize(final Bootstrap<Configuration> bootstrap) {
        final GuiceBundle<Configuration> jersey2GuiceBundle = new GuiceBundle<Configuration>(
            GuiceEnvironmentConfiguration.builder()
                                         .module(new TestModule())
                                         .autoConfig(
                                             AutoConfig.builder()
                                                       .searchPackages(this.getClass().getPackage().getName())
                                                       .build())
                                         .build()
        );
        bootstrap.addBundle(jersey2GuiceBundle);
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {

    }
}
