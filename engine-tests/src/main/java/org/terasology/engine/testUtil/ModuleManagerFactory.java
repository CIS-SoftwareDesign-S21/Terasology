// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.testUtil;

import com.google.common.collect.Sets;
import org.terasology.engine.core.module.ModuleManagerImpl;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.naming.Name;

public final class ModuleManagerFactory {

    private ModuleManagerFactory() {
    }

    public static ModuleManagerImpl create() throws Exception {
        ModuleManagerImpl moduleManager = new ModuleManagerImpl("");
        Module unittestModule = moduleManager.getModuleFactory().createPackageModule("org.terasology.unittest");
        moduleManager.getRegistry().add(unittestModule);

        moduleManager.loadEnvironment(Sets.newHashSet(moduleManager.getRegistry().getLatestModuleVersion(new Name(
                "engine")), unittestModule), true);
        return moduleManager;
    }
}
