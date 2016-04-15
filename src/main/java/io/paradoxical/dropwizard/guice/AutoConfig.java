package io.paradoxical.dropwizard.guice;

import com.google.common.base.Preconditions;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvidedBy;
import io.dropwizard.Bundle;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import lombok.AccessLevel;
import lombok.Getter;
import org.glassfish.jersey.server.model.Resource;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;

public class AutoConfig {
    private static final Logger logger = LoggerFactory.getLogger(AutoConfig.class);

    @Getter(AccessLevel.PROTECTED)
    private final Reflections reflections;

    public AutoConfig(String... basePackages) {
        Preconditions.checkArgument(basePackages.length > 0);

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        FilterBuilder filterBuilder = new FilterBuilder();

        for (String basePkg : basePackages) {
            configurationBuilder.addUrls(ClasspathHelper.forPackage(basePkg));
            filterBuilder.include(FilterBuilder.prefix(basePkg));
        }

        configurationBuilder.filterInputsBy(filterBuilder)
                            .setScanners(new SubTypesScanner(),
                                         new TypeAnnotationsScanner());

        this.reflections = new Reflections(configurationBuilder);
    }

    public void run(Environment environment, Injector injector) {
        addHealthChecks(environment, injector);
        addProviders(environment);
        addResources(environment);
        addTasks(environment, injector);
        addManaged(environment, injector);
        addParamConverterProviders(environment);
    }

    public void initialize(Bootstrap<?> bootstrap, Injector injector) {
        addBundles(bootstrap, injector);
        addConfiguredBundles(bootstrap, injector);
    }

    protected void addManaged(Environment environment, Injector injector) {
        Iterable<Class<? extends Managed>> managedClasses = getSubTypesOf(Managed.class);

        for (Class<? extends Managed> managed : managedClasses) {
            Optional<? extends Managed> maybeManaged = getFromGuiceIfPossible(injector, managed);
            if (maybeManaged.isPresent()) {
                environment.lifecycle().manage(maybeManaged.get());
                logger.info("Added managed: {}", managed);
            }
        }
    }

    protected void addTasks(Environment environment, Injector injector) {
        Iterable<Class<? extends Task>> taskClasses = getSubTypesOf(Task.class);

        for (Class<? extends Task> task : taskClasses) {
            Optional<? extends Task> maybeTask = getFromGuiceIfPossible(injector, task);
            if (maybeTask.isPresent()) {
                environment.admin().addTask(maybeTask.get());
                logger.info("Added task: {}", task);
            }
        }
    }

    protected void addHealthChecks(Environment environment, Injector injector) {
        Iterable<Class<? extends InjectableHealthCheck>> healthCheckClasses = getSubTypesOf(InjectableHealthCheck.class);

        for (Class<? extends InjectableHealthCheck> healthCheck : healthCheckClasses) {
            Optional<? extends InjectableHealthCheck> maybeHealthCheck = getFromGuiceIfPossible(injector, healthCheck);
            if (maybeHealthCheck.isPresent()) {
                environment.healthChecks().register(maybeHealthCheck.get().getName(), maybeHealthCheck.get());
                logger.info("Added injectableHealthCheck: {}", healthCheck);
            }
        }
    }

    protected void addProviders(Environment environment) {
        Iterable<Class<?>> providerClasses = getTypesAnnotatedWith(Provider.class);

        for (Class<?> provider : providerClasses) {
            registerOnEnvironment(environment, provider, "Added provider class: {}");
        }
    }

    protected void addResources(Environment environment) {
        Iterable<Class<?>> resourceClasses = getTypesAnnotatedWith(Path.class);

        for (Class<?> resource : resourceClasses) {
            if (Resource.isAcceptable(resource)) {
                registerOnEnvironment(environment, resource, "Added resource class: {}");
            }
        }
    }

    protected Iterable<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotationType) {
        return reflections.getTypesAnnotatedWith(annotationType);
    }

    protected void registerOnEnvironment(
        final Environment environment,
        final Class<?> resource,
        final String logMessage) {

        environment.jersey().register(resource);
        logger.info(logMessage, resource);
    }

    protected void addBundles(Bootstrap<?> bootstrap, Injector injector) {
        Iterable<Class<? extends Bundle>> bundleClasses = getSubTypesOf(Bundle.class);

        for (Class<? extends Bundle> bundle : bundleClasses) {
            Optional<? extends Bundle> maybeBundle = getFromGuiceIfPossible(injector, bundle);
            if (maybeBundle.isPresent()) {
                bootstrap.addBundle(maybeBundle.get());
                logger.info("Added bundle class {} during bootstrap", bundle);
            }
        }
    }

    protected void addConfiguredBundles(Bootstrap<?> bootstrap, Injector injector) {
        Iterable<Class<? extends ConfiguredBundle>> configuredBundleClasses = getSubTypesOf(ConfiguredBundle.class);

        for (Class<? extends ConfiguredBundle> configuredBundle : configuredBundleClasses) {
            if (configuredBundle != GuiceBundle.class) {
                Optional<? extends ConfiguredBundle> maybeConfiguredBundle = getFromGuiceIfPossible(injector, configuredBundle);
                if (maybeConfiguredBundle.isPresent()) {
                    bootstrap.addBundle(maybeConfiguredBundle.get());
                    logger.info("Added configured bundle class {} during bootstrap", configuredBundle);
                }
            }
        }
    }

    protected void addParamConverterProviders(Environment environment) {
        Iterable<Class<? extends ParamConverterProvider>> providerClasses = getSubTypesOf(ParamConverterProvider.class);

        for (Class<?> provider : providerClasses) {
            registerOnEnvironment(environment, provider, "Added ParamConverterProvider class: {}");
        }
    }

    protected <T> Iterable<Class<? extends T>> getSubTypesOf(Class<T> baseType) {
        return reflections.getSubTypesOf(baseType);
    }

    private <T> Optional<T> getFromGuiceIfPossible(Injector injector, Class<T> type) {
        // if it's a concrete class get it from Guice
        if (concreteClass(type) || hasBinding(injector, type)) {
            return Optional.of(injector.getInstance(type));
        }
        else {
            logger.info("Not attempting to retrieve abstract class {} from injector", type);
            return Optional.empty();
        }
    }

    private static boolean concreteClass(Class<?> type) {
        return !type.isInterface() && !Modifier.isAbstract(type.getModifiers());
    }

    private static boolean hasBinding(Injector injector, Class<?> type) {
        return injector.getExistingBinding(Key.get(type)) != null || hasBindingAnnotation(type);
    }

    private static boolean hasBindingAnnotation(Class<?> type) {
        return type.isAnnotationPresent(ImplementedBy.class) || type.isAnnotationPresent(ProvidedBy.class);
    }
}
