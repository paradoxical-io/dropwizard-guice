package io.paradoxical.dropwizard.guice.tests;

import com.google.inject.Injector;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import io.dropwizard.Configuration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.paradoxical.dropwizard.guice.GuiceEnvironmentConfiguration;
import io.paradoxical.dropwizard.guice.bundles.GuiceBundle;
import io.paradoxical.dropwizard.guice.jersey.JerseyModule;
import io.paradoxical.dropwizard.guice.tests.objects.TestModule;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.ServletException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class GuiceBundleTest {

    @Mock
    Environment environment;

    private GuiceBundle<Configuration> guiceBundle;

    @After
    public void tearDown() {
        JerseyGuiceUtils.reset();
    }

    @Before
    public void setUp() {
        //given
        environment = new Environment("test env", Jackson.newObjectMapper(), null, null, null);

        guiceBundle = new GuiceBundle<>(
            GuiceEnvironmentConfiguration.builder()
                                         .module(new TestModule())
                                         .build()
        );

        Bootstrap bootstrap = mock(Bootstrap.class);
        guiceBundle.initialize(bootstrap);
        guiceBundle.run(new Configuration(), environment);
    }

    @Test
    public void createsInjectorWhenInit() throws ServletException {
        //then
        Injector injector = guiceBundle.getInjector();
        assertThat(injector).isNotNull();
    }

    @Test
    public void serviceLocatorIsAvailable() throws ServletException {
        ServiceLocator serviceLocator = JerseyModule.getLocator(guiceBundle.getInjector());
        assertThat(serviceLocator).isNotNull();
    }
}
