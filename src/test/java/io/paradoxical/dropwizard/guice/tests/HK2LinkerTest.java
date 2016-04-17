package io.paradoxical.dropwizard.guice.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import io.paradoxical.dropwizard.guice.jersey.JerseyModule;
import io.paradoxical.dropwizard.guice.tests.objects.ExplicitResource;
import io.paradoxical.dropwizard.guice.tests.objects.JitResource;
import io.paradoxical.dropwizard.guice.tests.objects.TestModule;
import org.glassfish.hk2.api.ServiceLocator;
import org.junit.AfterClass;
import org.junit.Test;

import javax.servlet.ServletException;

import static org.assertj.core.api.Assertions.assertThat;

public class HK2LinkerTest {

    final Injector injector = Guice.createInjector(new JerseyModule(), new TestModule());
    final ServiceLocator serviceLocator = JerseyModule.getLocator(injector);

    @AfterClass
    public static void tearDown() {
        JerseyGuiceUtils.reset();
    }

    @Test
    public void explicitGuiceBindingsAreBridgedToHk2() throws ServletException {
        // when
        ExplicitResource resource = serviceLocator.createAndInitialize(ExplicitResource.class);

        // then
        assertThat(resource).isNotNull();
        assertThat(resource.getDAO()).isNotNull();
    }

    @Test
    public void jitGuiceBindingsAreBridgedToHk2() throws ServletException {
        // when
        JitResource resource = serviceLocator.createAndInitialize(JitResource.class);

        // then
        assertThat(resource).isNotNull();
        assertThat(resource.getDAO()).isNotNull();
    }
}
