package io.paradoxical.dropwizard.guice.objects;

import io.paradoxical.dropwizard.guice.InjectableHealthCheck;

public class InjectedHealthCheck extends InjectableHealthCheck {
    @Override
    public String getName() {
        return "healthcheck";
    }

    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
