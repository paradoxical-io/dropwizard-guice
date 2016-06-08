package io.paradoxical.dropwizard.guice.bundles;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.paradoxical.dropwizard.bundles.admin.AdminBundle;
import io.paradoxical.dropwizard.bundles.admin.AdminEnvironmentConfigurator;
import io.paradoxical.dropwizard.bundles.admin.AdminResourceEnvironment;
import io.paradoxical.dropwizard.guice.EnvironmentData;
import io.paradoxical.dropwizard.guice.GuiceEnvironmentConfiguration;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public class AdminGuiceBundle<T extends Configuration> extends GuiceBundle<T> implements AdminEnvironmentConfigurator {
    private static final CharMatcher wildcardMatcher = CharMatcher.anyOf("/*");
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminGuiceBundle.class);
    private final AdminBundle adminBundle;
    private final ImmutableList<AdminEnvironmentConfigurator> environmentConfigurators;

    @Builder
    public AdminGuiceBundle(
        @NonNull @Nonnull final GuiceEnvironmentConfiguration guiceEnvironmentConfiguration,
        @Singular("configureEnvironment")
        final ImmutableList<AdminEnvironmentConfigurator> environmentConfigurators,
        final String adminRootPath) {
        super(guiceEnvironmentConfiguration);
        this.environmentConfigurators = environmentConfigurators;

        adminBundle = new AdminBundle(ImmutableList.of(this), adminRootPath);
    }

    @Override
    public void run(final T configuration, final Environment environment) throws Exception {
        adminBundle.run(configuration, environment);
    }

    @Override
    public void configure(final Configuration configuration, final AdminResourceEnvironment adminResourceEnvironment) {
        setupEnvironmentGuice(configuration, EnvironmentData.admin(adminResourceEnvironment));
        environmentConfigurators.forEach(config -> config.configure(configuration, adminResourceEnvironment));
    }
}
