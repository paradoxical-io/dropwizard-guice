package io.paradoxical.dropwizard.guice;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FilteredAutoConfig extends AutoConfig {
    private final Predicate<Class<?>> typePredicate;

    public FilteredAutoConfig withAnnotation(Class<? extends Annotation> annotation) {
        return new FilteredAutoConfig(type -> type.isAnnotationPresent(annotation));
    }

    public FilteredAutoConfig withoutAnnotation(Class<? extends Annotation> annotation) {
        return new FilteredAutoConfig(type -> !type.isAnnotationPresent(annotation));
    }

    private FilteredAutoConfig(Predicate<Class<?>> typePredicate) {
        this.typePredicate = typePredicate;
    }

    @Override
    protected Iterable<Class<?>> getTypesAnnotatedWith(final Class<? extends Annotation> annotationType) {
        final Iterable<Class<?>> typesAnnotatedWith = super.getTypesAnnotatedWith(annotationType);

        return StreamSupport.stream(typesAnnotatedWith.spliterator(), false)
                            .filter(typePredicate)
                            .collect(Collectors.toSet());
    }

    @Override
    protected <T> Iterable<Class<? extends T>> getSubTypesOf(final Class<T> baseType) {
        final Iterable<Class<? extends T>> subTypesOf = super.getSubTypesOf(baseType);

        return StreamSupport.stream(subTypesOf.spliterator(), false)
                            .filter(typePredicate)
                            .collect(Collectors.toSet());
    }
}
