package io.paradoxical.dropwizard.guice;

import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

public class DefaultInjectorFactory implements InjectorFactory {
    @Override
    public Injector create(final Stage stage, final List<Module> modules) {
        return Guice.createInjector(stage,modules);
    }
}
