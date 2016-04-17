package io.paradoxical.dropwizard.guice.jersey;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.setup.ServletEnvironment;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Functionality used to help link Guice with Jersey
 */
public class JerseyUtil {

    private static final Logger logger = LoggerFactory.getLogger(JerseyUtil.class);

    public interface RegisterFunction extends Consumer<Class<?>> {
    }

    /**
     * Registers any Guice-bound providers or root resources.
     */
    public static void registerGuiceBound(Injector injector, final RegisterFunction registerFunction) {
        while (injector != null) {
            for (Key<?> key : injector.getBindings().keySet()) {
                Type type = key.getTypeLiteral().getType();
                if (type instanceof Class) {
                    Class<?> c = (Class) type;
                    if (isProviderClass(c)) {
                        logger.info("Registering {} as a provider class", c.getName());
                        registerFunction.accept(c);
                    }
                    else if (isRootResourceClass(c)) {
                        // Jersey rejects resources that it doesn't think are acceptable
                        // Including abstract classes and interfaces, even if there is a valid Guice binding.
                        if (Resource.isAcceptable(c)) {
                            logger.info("Registering {} as a root resource class", c.getName());
                            registerFunction.accept(c);
                        }
                        else {
                            logger.warn("Class {} was not registered as a resource. Bind a concrete implementation instead.", c.getName());
                        }
                    }

                }
            }
            injector = injector.getParent();
        }
    }

    private static boolean isProviderClass(Class<?> c) {
        return c != null && c.isAnnotationPresent(javax.ws.rs.ext.Provider.class);
    }

    private static boolean isRootResourceClass(Class<?> c) {
        if (c == null) {
            return false;
        }

        if (c.isAnnotationPresent(Path.class)) {
            return true;
        }

        for (Class i : c.getInterfaces()) {
            if (i.isAnnotationPresent(Path.class)) {
                return true;
            }
        }

        return false;
    }

    public static void registerGuiceFilter(final ServletEnvironment environment, final MutableServletContextHandler appContext) {
        environment.addFilter("Guice Filter", GuiceFilter.class)
                   .addMappingForUrlPatterns(null, false, appContext.getContextPath() + "*");
    }
}
