package org.springframework.aot.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.aot.build.context.BuildContext;
import org.springframework.aot.factories.fixtures.OtherFactory;
import org.springframework.aot.factories.fixtures.PublicFactory;
import org.springframework.aot.factories.fixtures.TestFactory;

/**
 * Tests for {@link SpringFactoriesContributor}.
 *
 * @author Tadaya Tsuyukubo
 */
class SpringFactoriesContributorTests {

    SpringFactoriesContributor contributor = new SpringFactoriesContributor();

    @Test
    void shouldRecognizeEntryWithSpace() throws Exception {
        BuildContext buildContext = mock(BuildContext.class);
        when(buildContext.getClassLoader()).thenReturn(this.getClass().getClassLoader());

        List<Class<?>> factories = this.contributor.loadSpringFactories(buildContext).stream()
                .filter(factory -> TestFactory.class.isAssignableFrom(factory.getFactoryType()))
                .map(SpringFactory::getFactory)
                .collect(Collectors.toList());

        assertThat(factories).containsExactlyInAnyOrder(PublicFactory.class, OtherFactory.class);
    }

}
