/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.engine.particles.functions;

import org.terasology.engine.particles.ParticleDataMask;

/**
 * Base class for GeneratorFunction and AffectorFunction. A particle system function is called on a particle to update its fields.
 */
public abstract class ParticleSystemFunction<T> {
    private final int rawDataMask;

    public ParticleSystemFunction(ParticleDataMask dataMask, ParticleDataMask... dataMasks) {
        this.rawDataMask = ParticleDataMask.toInt(dataMask, dataMasks);
    }

    public final int getDataMask() {
        return rawDataMask;
    }
}
