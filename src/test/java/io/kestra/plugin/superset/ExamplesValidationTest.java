package io.kestra.plugin.superset;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.flows.Flow;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.validations.ModelValidator;
import io.kestra.core.serializers.YamlParser;
import io.kestra.plugin.superset.charts.Data;
import io.kestra.plugin.superset.dashboards.Export;
import io.kestra.plugin.superset.dashboards.List;
import io.kestra.plugin.superset.dashboards.Trigger;
import io.kestra.plugin.superset.datasets.Refresh;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

// runContextFactory.of() skips bean validation, so constraints that have no validator
// for Property<...> only surface here, as they do when a flow is saved.
@KestraTest
class ExamplesValidationTest {

    @Inject
    private ModelValidator modelValidator;

    @Test
    void examplesAreValidFlows() {
        var types = java.util.List.of(
            Data.class,
            Export.class,
            List.class,
            Trigger.class,
            io.kestra.plugin.superset.datasets.List.class,
            Refresh.class
        );

        types.forEach(type -> {
            var examples = type.getAnnotation(Plugin.class).examples();
            assertFalse(examples.length == 0, type.getSimpleName() + " has no @Example");

            Arrays.stream(examples).forEach(example -> {
                var flow = YamlParser.parse(String.join("\n", example.code()), Flow.class);
                assertDoesNotThrow(() -> modelValidator.validate(flow), type.getSimpleName() + " example is invalid");
            });
        });
    }

    @Test
    void exportWithDashboardIdsIsValid() {
        var export = Export.builder()
            .id("export")
            .type(Export.class.getName())
            .url(Property.ofValue("http://localhost:8088/api/v1"))
            .username(Property.ofValue("admin"))
            .password(Property.ofValue("admin"))
            .dashboardIds(Property.ofValue(java.util.List.of(1, 2)))
            .build();

        assertDoesNotThrow(() -> modelValidator.validate(export));
    }
}
