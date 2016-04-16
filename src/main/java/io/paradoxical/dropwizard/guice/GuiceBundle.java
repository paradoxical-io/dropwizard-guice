package io.paradoxical.dropwizard.guice;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.paradoxical.dropwizard.guice.jersey.JerseyModule;
import io.paradoxical.dropwizard.guice.jersey.JerseyUtil;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class GuiceBundle<T extends Configuration> implements ConfiguredBundle<T> {

    final Logger logger = LoggerFactory.getLogger(GuiceBundle.class);

    private final AutoConfig autoConfig;
    private final List<Module> modules;
    private final InjectorFactory injectorFactory;
    private Injector injector;
    private DropwizardEnvironmentModule dropwizardEnvironmentModule;
    private Optional<Class<T>> configurationClass;
    private Stage stage;

    public static class Builder<T extends Configuration> {
        private AutoConfig autoConfig;
        private List<Module> modules = Lists.newArrayList();
        private Optional<Class<T>> configurationClass = Optional.empty();
        private InjectorFactory injectorFactory = new DefaultInjectorFactory();

        public Builder<T> addModule(Module module) {
            Preconditions.checkNotNull(module);
            modules.add(module);
            return this;
        }

        public Builder<T> setConfigClass(Class<T> clazz) {
            configurationClass = Optional.of(clazz);
            return this;
        }

        public Builder<T> setInjectorFactory(InjectorFactory factory) {
            Preconditions.checkNotNull(factory);
            injectorFactory = factory;
            return this;
        }

        public Builder<T> enableAutoConfig(String... basePackages) {
            Preconditions.checkArgument(basePackages.length > 0);
            Preconditions.checkArgument(autoConfig == null, "autoConfig already enabled!");
            return enableAutoConfig(
                AutoConfig.builder()
                          .searchPackages(basePackages)
                          .build());
        }

        public Builder<T> enableAutoConfig(@Nonnull @NonNull final AutoConfig autoConfig) {
            Preconditions.checkArgument(this.autoConfig == null, "autoConfig already enabled!");
            this.autoConfig = autoConfig;
            return this;
        }

        public GuiceBundle<T> build() {
            return build(Stage.PRODUCTION);
        }

        public GuiceBundle<T> build(Stage s) {
            return new GuiceBundle<>(s, autoConfig, modules, configurationClass, injectorFactory);
        }
    }

    public static <T extends Configuration> Builder<T> newBuilder() {
        return new Builder<>();
    }

    private GuiceBundle(
        final Stage stage,
        final AutoConfig autoConfig,
        final List<Module> modules,
        final Optional<Class<T>> configurationClass,
        final InjectorFactory injectorFactory) {

        Preconditions.checkNotNull(modules);
        Preconditions.checkArgument(!modules.isEmpty());
        Preconditions.checkNotNull(stage);
        this.modules = modules;
        this.autoConfig = autoConfig;
        this.configurationClass = configurationClass;
        this.injectorFactory = injectorFactory;
        this.stage = stage;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        if (configurationClass.isPresent()) {
            dropwizardEnvironmentModule = new DropwizardEnvironmentModule<>(configurationClass.get());
        }
        else {
            dropwizardEnvironmentModule = new DropwizardEnvironmentModule<>(Configuration.class);
        }
        modules.add(new JerseyModule());
        modules.add(dropwizardEnvironmentModule);

        initInjector();

        if (autoConfig != null) {
            autoConfig.initialize(bootstrap, injector);
        }
    }

    @SuppressFBWarnings("DM_EXIT")
    private void initInjector() {
        try {
            injector = injectorFactory.create(this.stage, ImmutableList.copyOf(this.modules));
        }
        catch (Exception ie) {
            logger.error("Exception occurred when creating Guice Injector - exiting", ie);
            System.exit(1);
        }
    }

    @Override
    public void run(final T configuration, final Environment environment) {
        JerseyUtil.registerGuiceBound(injector, environment.jersey());
        JerseyUtil.registerGuiceFilter(environment);
        setEnvironment(configuration, environment);

        if (autoConfig != null) {
            autoConfig.run(environment, injector);
        }
    }

    @SuppressWarnings("unchecked")
    private void setEnvironment(final T configuration, final Environment environment) {
        dropwizardEnvironmentModule.setEnvironmentData(configuration, environment);
    }

    public Injector getInjector() {
        return injector;
    }
}
