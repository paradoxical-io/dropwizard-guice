package io.paradoxical.dropwizard.guice;

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public class AutoConfigBuilder {

    private static final Predicate<Class<?>> DefaultTypePredicate = c -> true;

    private Predicate<Class<?>> typePredicate = DefaultTypePredicate;
    private ImmutableSet<String> searchPackages = ImmutableSet.of();

    public AutoConfig build() {
        if (typePredicate != DefaultTypePredicate) {
            return new FilteredAutoConfig(searchPackages, typePredicate);
        }

        return new AutoConfig(searchPackages);
    }

    public AutoConfigBuilder searchPackages(@NonNull String... packages) {

        searchPackages = ImmutableSet.<String>builder()
            .add(packages)
            .addAll(searchPackages)
            .build();

        return this;
    }

    public AutoConfigBuilder addTypeFilter(@NonNull final Predicate<Class<?>> typeFilter) {
        typePredicate = this.typePredicate.and(typeFilter);
        return this;
    }

    public AutoConfigBuilder withAnnotation(@NonNull final Class<? extends Annotation> annotation) {
        return addTypeFilter(type -> type.isAnnotationPresent(annotation));
    }

    public AutoConfigBuilder withoutAnnotation(@NonNull final Class<? extends Annotation> annotation) {
        return addTypeFilter(type -> !type.isAnnotationPresent(annotation));
    }
}
