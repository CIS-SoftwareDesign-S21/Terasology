/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.config.flexible.setting;

import org.junit.Test;
import org.terasology.config.flexible.setting.constraints.Constraint;
import org.terasology.engine.SimpleUri;

import static org.junit.Assert.*;

public class SettingBuilderTest {
    @Test
    public void testBuild() {
        final SimpleUri id = new SimpleUri("engine-test:Setting");
        final Integer defaultValue = 15;
        final Constraint<Integer> constraint = null;
        final String humanReadableName = "Name";
        final String description = "Description";

        Setting<Integer> setting = SettingBuilder.ofType(Integer.class)
                .id(id)
                .defaultValue(defaultValue)
                .constraint(constraint)
                .humanReadableName(humanReadableName)
                .description(description)
                .build();

        assertEquals(id, setting.getId());
        assertEquals(defaultValue, setting.getDefaultValue());
        assertEquals(constraint, setting.getConstraint());
        assertEquals(humanReadableName, setting.getHumanReadableName());
        assertEquals(description, setting.getDescription());
    }
}