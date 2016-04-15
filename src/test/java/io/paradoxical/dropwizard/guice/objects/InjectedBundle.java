package io.paradoxical.dropwizard.guice.objects;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.inject.Singleton;

@Singleton
public class InjectedBundle implements Bundle {
    public static final String MARKER = "BUNDLE RAN";

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

    @Override
    public void run(Environment environment) {
        System.setProperty(MARKER, "true");
    }
}
