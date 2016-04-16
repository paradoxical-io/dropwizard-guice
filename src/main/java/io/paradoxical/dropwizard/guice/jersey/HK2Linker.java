package io.paradoxical.dropwizard.guice.jersey;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.squarespace.jersey2.guice.JerseyGuiceModule;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extension.ServiceLocatorGenerator;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

//Inspired by gwizard-jersey - https://github.com/stickfigure/gwizard
/**
 * Binding this as an eager singleton provides the second step of linking Guice back into HK2.
 * (the first step was to install the HK2 BootstrapModule in the Guice module).
 *
 * This needs to happen before anything else related to Jersey starts.
 */
public class HK2Linker {
    @Inject
    public HK2Linker(Injector injector) {
//        JerseyGuiceUtils.link(locator, injector);
        JerseyGuiceUtils.install(new ServiceLocatorGenerator() {
            @Override
            public ServiceLocator create(String name, ServiceLocator parent) {
                if (!name.startsWith("__HK2_Generated_")) {
                    return null;
                }

//                return locator.get();
//
                List<Module> modules = new ArrayList<>();

                modules.add(new JerseyGuiceModule(name));

                return injector.createChildInjector(modules)
                                     .getInstance(ServiceLocator.class);
            }
        });
    }

}
