package io.paradoxical.dropwizard.guice;

import com.google.inject.servlet.ServletModule;
import com.squarespace.jersey2.guice.JerseyGuiceModule;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.glassfish.hk2.api.ServiceLocator;

//Inspired by gwizard-jersey - https://github.com/stickfigure/gwizard
public class JerseyModule extends ServletModule {

    @Override
    protected void configureServlets() {
        // The order these operations (including the steps in the linker) are important
        ServiceLocator locator = new ServiceLocatorDecorator(JerseyGuiceUtils.newServiceLocator()) {

            @Override
            public void shutdown() {
                // don't shutdown, see issue #67. Remove once jersey2-guice supports Jersey 2.21
            }
        };
        install(new JerseyGuiceModule(locator));

        bind(HK2Linker.class).asEagerSingleton();
    }
}
