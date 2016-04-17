package io.paradoxical.dropwizard.guice.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Bundle;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.AdminEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.paradoxical.dropwizard.guice.AutoConfig;
import io.paradoxical.dropwizard.guice.tests.objects.ExplicitResource;
import io.paradoxical.dropwizard.guice.tests.objects.InjectedBundle;
import io.paradoxical.dropwizard.guice.tests.objects.InjectedHealthCheck;
import io.paradoxical.dropwizard.guice.tests.objects.InjectedManaged;
import io.paradoxical.dropwizard.guice.tests.objects.InjectedProvider;
import io.paradoxical.dropwizard.guice.tests.objects.InjectedTask;
import io.paradoxical.dropwizard.guice.tests.objects.ResourceInterface;
import io.paradoxical.dropwizard.guice.tests.objects.TestModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.SortedSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutoConfigTest {

    private final Injector injector = Guice.createInjector(new TestModule());

    @Spy
    private Environment environment = new Environment("test env", Jackson.newObjectMapper(), null, null, null);
    public final JerseyEnvironment jerseyEnvironment = environment.jersey();

    private AutoConfig autoConfig;

    @Before
    public void setUp() {
        //when
        autoConfig = AutoConfig.builder()
                               .searchPackages(getClass().getPackage().getName())
                               .build();
    }

    @Test
    public void addBundlesDuringBootStrap() {
        //given
        final Bootstrap bootstrap = mock(Bootstrap.class);
        Bundle singletonBundle = injector.getInstance(InjectedBundle.class);

        //when
        autoConfig.addDiscoveredBundles(bootstrap, injector);

        verify(bootstrap).addBundle(singletonBundle);
    }

    @Test
    public void addInjectableHealthChecks() {
        //when
        autoConfig.run(environment, jerseyEnvironment, injector);

        // then
        SortedSet<String> healthChecks = environment.healthChecks().getNames();
        assertThat(healthChecks).contains(new InjectedHealthCheck().getName());
    }

    @Test
    public void addProviders() {
        // when
        autoConfig.run(environment, jerseyEnvironment, injector);

        //then
        Set<Class<?>> components = environment.jersey().getResourceConfig().getClasses();
        assertThat(components).containsOnlyOnce(InjectedProvider.class);
    }

    @Test
    public void addResources() {
        //when
        autoConfig.run(environment, jerseyEnvironment, injector);

        //then
        Set<Class<?>> components = environment.jersey().getResourceConfig().getClasses();
        assertThat(components).containsOnlyOnce(ExplicitResource.class);
    }

    @Test
    public void interfaceResourcesNotAdded() {
        //when
        autoConfig.run(environment, jerseyEnvironment, injector);

        //then
        Set<Class<?>> components = environment.jersey().getResourceConfig().getClasses();
        assertThat(components).doesNotContain(ResourceInterface.class);
    }

    @Test
    public void addTasks() throws Exception {
        //given
        when(environment.admin()).thenReturn(mock(AdminEnvironment.class));

        //when
        autoConfig.run(environment, jerseyEnvironment, injector);

        //then
        Task task = injector.getInstance(InjectedTask.class);
        assertThat(task.getName()).isEqualTo("test task");
        verify(environment.admin()).addTask(task);

    }

    @Test
    public void addManaged() {
        //given
        Managed managed = injector.getInstance(InjectedManaged.class);
        when(environment.lifecycle()).thenReturn(mock(LifecycleEnvironment.class));

        //when
        autoConfig.run(environment, jerseyEnvironment, injector);

        //then
        verify(environment.lifecycle()).manage(managed);
    }
}
