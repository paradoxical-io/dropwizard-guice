package io.paradoxical.dropwizard.guice.bundles;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.paradoxical.dropwizard.guice.admin.AdminEnvironmentConfigurator;
import io.paradoxical.dropwizard.guice.EnvironmentData;
import io.paradoxical.dropwizard.guice.GuiceEnvironmentConfiguration;
import io.paradoxical.dropwizard.guice.admin.AdminResourceEnvironment;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import javax.annotation.Nonnull;

public class AdminGuiceBundle<T extends Configuration> extends GuiceBundle<T> {
    private static final CharMatcher wildcardMatcher = CharMatcher.anyOf("/*");

    @Nonnull
    private final ImmutableList<AdminEnvironmentConfigurator> environmentConfigurators;
    private final String adminRootPath;

    @Builder
    public AdminGuiceBundle(
        @NonNull @Nonnull final GuiceEnvironmentConfiguration guiceEnvironmentConfiguration,
        @Singular("configureEnvironment")
        final ImmutableList<AdminEnvironmentConfigurator> environmentConfigurators,
        final String adminRootPath) {
        super(guiceEnvironmentConfiguration);

        this.environmentConfigurators = environmentConfigurators == null ? ImmutableList.of() : environmentConfigurators;
        this.adminRootPath = adminRootPath == null ? "/admin" : wildcardMatcher.trimTrailingFrom(adminRootPath);
    }

    @Override
    public void run(final T configuration, final Environment environment) {
        final AdminResourceEnvironment adminResourceEnvironment = new AdminResourceEnvironment(environment);

        environment.admin()
                   .addServlet(AdminGuiceBundle.class.getCanonicalName(),
                               adminResourceEnvironment.jerseyContainerHolder().getContainer())
                   .addMapping(adminRootPath + "/*");

        setupEnvironmentGuice(configuration, EnvironmentData.admin(adminResourceEnvironment));
        environmentConfigurators.forEach(configure -> configure.configure(configuration, adminResourceEnvironment));

        adminResourceEnvironment.adminResourceConfig()
                                .logComponents();

    }
}
