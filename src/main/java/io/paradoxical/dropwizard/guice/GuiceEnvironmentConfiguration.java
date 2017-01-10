package io.paradoxical.dropwizard.guice;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.paradoxical.dropwizard.bundles.admin.AdminEnvironmentConfigurator;
import io.paradoxical.dropwizard.bundles.admin.AdminResourceEnvironment;
import io.paradoxical.dropwizard.guice.jersey.JerseyModule;
import io.paradoxical.dropwizard.guice.jersey.JerseyUtil;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

@Value
public class GuiceEnvironmentConfiguration implements AdminEnvironmentConfigurator {
    private static final Logger logger = LoggerFactory.getLogger(GuiceEnvironmentConfiguration.class);

    private final AutoConfig autoConfig;
    private final ImmutableList<Module> modules;

    private final Supplier<Injector> injectorSupplier;

    private final DropwizardEnvironmentModule<Configuration> environmentModule;

    public Injector getInjector() {
        return injectorSupplier.get();
    }

    @Builder(builderClassName = "GuiceEnvironmentConfigurationBuilder")
    private GuiceEnvironmentConfiguration(
        final AutoConfig autoConfig,
        @Singular
        final ImmutableList<Module> modules,
        final Class<? extends Configuration> configurationClass,
        final InjectorFactory injectorFactory,
        final Stage guiceStage) {

        this.autoConfig = autoConfig;

        if (configurationClass == null) {
            environmentModule = new DropwizardEnvironmentModule<>(Configuration.class);
        }
        else {
            environmentModule = new DropwizardEnvironmentModule<>((Class<Configuration>) configurationClass);
        }

        this.modules = ImmutableList.<Module>builder()
            .add(new JerseyModule())
            .add(environmentModule)
            .addAll(modules)
            .build();   

        final Stage stage = Optional.ofNullable(guiceStage).orElse(Stage.PRODUCTION);

        final com.google.common.base.Supplier<Injector> lazySupplier =
            Suppliers.memoize(() -> initInjector(stage,
                                                 Optional.ofNullable(injectorFactory)
                                                         .orElseGet(DefaultInjectorFactory::new),
                                                 this.modules));

        this.injectorSupplier = lazySupplier::get;
    }

    @Override
    public void configure(final Configuration config, final AdminResourceEnvironment adminResourceEnvironment) {
        configureEnvironment(config, EnvironmentData.admin(adminResourceEnvironment));
    }

    private static Injector initInjector(
        final Stage stage,
        final InjectorFactory injectorFactory,
        final ImmutableList<Module> modules) {

        try {
            return injectorFactory.create(stage, ImmutableList.copyOf(modules));
        }
        catch (Exception ie) {
            logger.error("Exception occurred when creating Guice Injector", ie);
            throw new RuntimeException("Exception occurred when creating Guice Injector", ie);
        }
    }

    class SetEnvBundle implements Bundle {
        @Override
        public void initialize(final Bootstrap<?> bootstrap) {
        }

        @Override
        public void run(final Environment environment) {
            environmentModule.setEnvironmentData(null, environment);
        }
    }

    class SetConfigBundle implements ConfiguredBundle<Configuration> {
        @Override
        public void run(final Configuration configuration, final Environment environment) throws Exception {
            environmentModule.setEnvironmentData(configuration, environment);
        }

        @Override
        public void initialize(final Bootstrap<?> bootstrap) {
        }
    }

    public void addBundles(final Bootstrap<? extends Configuration> bootstrap) {

        bootstrap.addBundle(new SetEnvBundle());
        bootstrap.addBundle(new SetConfigBundle());

        if (autoConfig != null) {
            autoConfig.addDiscoveredBundles(bootstrap, getInjector());
        }
    }

    public void configureEnvironment(
        @NonNull @Nonnull final Configuration configuration,
        @NonNull @Nonnull final EnvironmentData environmentData) {

        final Injector injector = getInjector();

        JerseyUtil.registerGuiceBound(injector, environmentData.jerseyEnvironment()::register);
        JerseyUtil.registerGuiceFilter(environmentData.servletEnvironment(), environmentData.serverContext());

        setEnvironment(configuration, environmentData.environment());

        if (autoConfig != null) {
            autoConfig.run(environmentData.environment(), environmentData.jerseyEnvironment(), injector);
        }
    }

    private void setEnvironment(final Configuration configuration, final Environment environment) {
        environmentModule.setEnvironmentData(configuration, environment);
    }
}
