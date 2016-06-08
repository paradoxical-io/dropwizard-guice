package io.paradoxical.dropwizard.guice.tests;

import com.google.common.io.Resources;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.paradoxical.dropwizard.guice.tests.objects.InjectedBundle;
import io.paradoxical.dropwizard.guice.tests.objects.TestApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class InjectedIntegrationTest {

    @Rule
    public final DropwizardAppRule<Configuration> RULE =
        new DropwizardAppRule<>(TestApplication.class, resourceFilePath("test-config.yml"));

    protected Client client;

    @Before
    public void setUp() {
        client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");
    }

    @After
    public void tearDown() {
        JerseyGuiceUtils.reset();
    }

    public static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void shouldGetExplicitMessage() {

        // when
        final String message = client.target(
            String.format("http://localhost:%d//explicit/message", RULE.getLocalPort()))
                                     .request()
                                     .get(String.class);

        // then
        assertThat(message).isEqualTo("this DAO was bound explicitly");
    }

    @Test
    @Ignore
    public void shouldGetJitMessage() {

        // when
        final String message = client.target(
            String.format("http://localhost:%d//jit/message", RULE.getLocalPort()))
                                     .request()
                                     .get(String.class);

        // then
        assertThat(message).isEqualTo("this DAO was bound just-in-time");
    }

    @Test
    @Ignore
    public void shouldRunInjectedBundle() {
        assertThat(Boolean.getBoolean(InjectedBundle.MARKER)).isTrue();
    }
}
