// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.testUtil;

import com.google.common.collect.Sets;
import org.terasology.engine.core.module.ModuleManagerImpl;
import org.terasology.engine.entitySystem.stubs.StringComponent;
import org.terasology.gestalt.module.Module;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import static com.google.common.base.Verify.verify;
import static org.terasology.engine.core.TerasologyConstants.ENGINE_MODULE;

public final class ModuleManagerFactory {

    private ModuleManagerFactory() { }

    public static ModuleManagerImpl create() throws Exception {
        return create(false);
    }

    public static ModuleManagerImpl create(boolean loadModulesFromClasspath) throws Exception {
        ModuleManagerImpl moduleManager = new ModuleManagerImpl("", Collections.emptyList(), loadModulesFromClasspath);
        loadUnitTestModule(moduleManager);
        return moduleManager;
    }

    public static void loadUnitTestModule(ModuleManagerImpl manager) throws IOException, URISyntaxException {
        // using the StringComponent stub class as representative example of classes in the unittest module
        Module testModule = manager.loadClasspathModule(StringComponent.class);
        verify(testModule.getMetadata().getId().toString().equals("unittest"),
                "Intended to load the unittest module but ended up with this instead: %s", testModule);
        manager.loadEnvironment(
                Sets.newHashSet(
                        manager.getRegistry().getLatestModuleVersion(ENGINE_MODULE),
                        testModule
                ), true);
    }
}
