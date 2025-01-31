/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.network;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.nui.Color;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.chunks.ChunkRegionListener;

/**
 * A client is the connection between a player (local or remote) and the game.
 *
 */
public interface Client extends ChunkRegionListener {

    String getName();

    String getId();

    Color getColor();

    void disconnect();

    void update(boolean netTick);

    EntityRef getEntity();

    void send(Event event, EntityRef target);

    ViewDistance getViewDistance();

    boolean isLocal();

    void setViewDistanceMode(ViewDistance viewDistance);
}
