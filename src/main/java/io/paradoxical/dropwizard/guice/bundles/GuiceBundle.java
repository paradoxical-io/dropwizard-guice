package io.paradoxical.dropwizard.guice.bundles;

import com.google.inject.Injector;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.paradoxical.dropwizard.guice.EnvironmentData;
import io.paradoxical.dropwizard.guice.GuiceEnvironmentConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;


public class GuiceBundle<T extends Configuration> implements ConfiguredBundle<T> {

    final Logger logger = LoggerFactory.getLogger(GuiceBundle.class);

    @Getter(AccessLevel.PROTECTED)
    private final GuiceEnvironmentConfiguration guiceEnvironmentConfiguration;

    public GuiceBundle(@NonNull @Nonnull final GuiceEnvironmentConfiguration guiceEnvironmentConfiguration) {
        this.guiceEnvironmentConfiguration = guiceEnvironmentConfiguration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        guiceEnvironmentConfiguration.addBundles(bootstrap);
    }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception {
        final EnvironmentData app = EnvironmentData.app(environment);

        setupEnvironmentGuice(configuration, app);
    }

    protected void setupEnvironmentGuice(final Configuration configuration, final EnvironmentData app) {
        guiceEnvironmentConfiguration.configureEnvironment(configuration, app);
    }

    public Injector getInjector() {
        return guiceEnvironmentConfiguration.getInjector();
    }
}
