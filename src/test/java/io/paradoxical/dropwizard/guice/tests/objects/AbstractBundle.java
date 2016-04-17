package io.paradoxical.dropwizard.guice.tests.objects;

import io.dropwizard.Bundle;

/**
 * Verifies that AutoConfigTest doesn't retrieve abstract classes from Guice
 */
public abstract class AbstractBundle implements Bundle {}
