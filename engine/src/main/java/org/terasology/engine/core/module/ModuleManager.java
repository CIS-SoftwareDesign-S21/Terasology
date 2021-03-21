// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.module;

import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.ModuleFactory;
import org.terasology.gestalt.module.ModuleMetadataJsonAdapter;
import org.terasology.gestalt.module.ModuleRegistry;
import org.terasology.gestalt.module.Module;

import java.util.Set;

public interface ModuleManager {

    ModuleRegistry getRegistry();

    ModuleInstallManager getInstallManager();

    ModuleEnvironment getEnvironment();

    ModuleEnvironment loadEnvironment(Set<Module> modules, boolean asPrimary);

    ModuleMetadataJsonAdapter getModuleMetadataReader();

    ModuleFactory getModuleFactory();
}
