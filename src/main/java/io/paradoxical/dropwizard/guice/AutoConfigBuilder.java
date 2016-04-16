package io.paradoxical.dropwizard.guice;

import com.google.common.collect.ImmutableSet;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

public class AutoConfigBuilder {

    private Predicate<Class<?>> typePredicate = null;
    private ImmutableSet<String> searchPackages = ImmutableSet.of();

    public AutoConfig build() {
        if(typePredicate != null) {
            return new FilteredAutoConfig(typePredicate, searchPackages);
        }

        return new AutoConfig(searchPackages);
    }

    public AutoConfigBuilder searchPackages(String... packages) {

        searchPackages = ImmutableSet.<String>builder()
            .add(packages)
            .addAll(searchPackages)
            .build();

        return this;
    }

    public AutoConfigBuilder addTypeFilter(Predicate<Class<?>> typeFilter) {
        if (typePredicate == null) {
            typePredicate = typeFilter;
        }
        else {
            typePredicate = this.typePredicate.and(typeFilter);
        }

        return this;
    }

    public AutoConfigBuilder withAnnotation(Class<? extends Annotation> annotation) {
        return addTypeFilter(type -> type.isAnnotationPresent(annotation));
    }

    public AutoConfigBuilder withoutAnnotation(Class<? extends Annotation> annotation) {
        return addTypeFilter(type -> !type.isAnnotationPresent(annotation));
    }
}
