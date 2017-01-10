package io.paradoxical.dropwizard.guice.jersey;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.JerseyGuiceModule;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extension.ServiceLocatorGenerator;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

//Inspired by gwizard-jersey - https://github.com/stickfigure/gwizard
public class JerseyModule extends ServletModule {
    private static final String HK2ModulePrefix = "__HK2_Generated_";

    @Override
    protected void configureServlets() {
        bind(HK2ServiceLocatorGeneratorInstaller.class).asEagerSingleton();
    }

    private static class HK2ServiceLocatorGeneratorInstaller {
        @Inject
        public HK2ServiceLocatorGeneratorInstaller(
                final ServiceLocatorGenerator serviceLocatorGenerator) {

            JerseyGuiceUtils.install(serviceLocatorGenerator);
        }
    }

    @Provides
    @Singleton
    public ServiceLocatorGenerator getLocatorGenerator(final Injector injector) {
        return (name, parent) -> {
            if (!name.startsWith(HK2ModulePrefix)) {
                return null;
            }

            List<Module> modules = new ArrayList<>();

            modules.add(new JerseyGuiceModule(name));

            return injector.createChildInjector(modules)
                           .getInstance(ServiceLocator.class);
        };
    }

    public static ServiceLocator getLocator(Injector injector) {
        return injector.getInstance(ServiceLocatorGenerator.class)
                       .create(HK2ModulePrefix, null);
    }
}
