// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.audio.nullAudio.NullAudioManager;
import org.terasology.engine.audio.nullAudio.NullSound;
import org.terasology.engine.audio.nullAudio.NullStreamingSound;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.core.module.ExternalApiWhitelist;
import org.terasology.engine.core.module.ModuleManagerImpl;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessMaterial;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessMesh;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessShader;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessSkeletalMesh;
import org.terasology.engine.core.subsystem.headless.assets.HeadlessTexture;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.internal.NetworkSystemImpl;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.persistence.internal.ReadWriteStorageManager;
import org.terasology.engine.persistence.typeHandling.extensionTypes.BlockFamilyTypeHandler;
import org.terasology.engine.persistence.typeHandling.extensionTypes.BlockTypeHandler;
import org.terasology.engine.persistence.typeHandling.extensionTypes.CollisionGroupTypeHandler;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.CollisionGroupManager;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.rendering.assets.animation.MeshAnimation;
import org.terasology.engine.rendering.assets.animation.MeshAnimationImpl;
import org.terasology.engine.rendering.assets.atlas.Atlas;
import org.terasology.engine.rendering.assets.font.Font;
import org.terasology.engine.rendering.assets.font.FontImpl;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.shader.Shader;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.engine.rendering.assets.texture.PNGTextureFormat;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.subtexture.Subtexture;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.family.BlockFamilyLibrary;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionFormat;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.engine.world.block.shapes.BlockShapeImpl;
import org.terasology.engine.world.block.sounds.BlockSounds;
import org.terasology.engine.world.block.tiles.BlockTile;
import org.terasology.engine.world.block.tiles.NullWorldAtlas;
import org.terasology.engine.world.block.tiles.WorldAtlas;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.sun.BasicCelestialModel;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.engine.world.sun.DefaultCelestialSystem;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.engine.world.time.WorldTimeImpl;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.ResolutionResult;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.skin.UISkin;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Setup a headless ( = no graphics ) environment.
 * Based on TerasologyTestingEnvironment code.
 *
 */
public class HeadlessEnvironment extends Environment {

    private static final WorldTime WORLD_TIME = new WorldTimeImpl();
    private static final Logger logger = LoggerFactory.getLogger(HeadlessEnvironment.class);

    /**
     * Setup a headless ( = no graphics ) environment
     *
     * @param modules a set of module names that should be loaded (latest version)
     */
    public HeadlessEnvironment(Name... modules) {
        super(modules);
    }

    @Override
    protected void setupStorageManager() throws IOException {
        ModuleManagerImpl moduleManager = context.get(ModuleManagerImpl.class);
        EngineEntityManager engineEntityManager = context.get(EngineEntityManager.class);
        BlockManager blockManager = context.get(BlockManager.class);
        RecordAndReplaySerializer recordAndReplaySerializer = context.get(RecordAndReplaySerializer.class);
        Path savePath = PathManager.getInstance().getSavePath("world1");
        RecordAndReplayUtils recordAndReplayUtils = new RecordAndReplayUtils();
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = context.get(RecordAndReplayCurrentStatus.class);

        ModuleEnvironment environment = context.get(ModuleManagerImpl.class).getEnvironment();
        context.put(BlockFamilyLibrary.class, new BlockFamilyLibrary(environment,context));

        ExtraBlockDataManager extraDataManager = context.get(ExtraBlockDataManager.class);

        context.put(StorageManager.class, new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(),
                engineEntityManager, blockManager, extraDataManager, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus));
    }

    @Override
    protected void setupNetwork() {
        EngineTime mockTime = mock(EngineTime.class);
        context.put(Time.class, mockTime);
        NetworkSystem networkSystem = new NetworkSystemImpl(mockTime, getContext());
        context.put(NetworkSystem.class, networkSystem);
    }

    @Override
    protected void setupEntitySystem() {
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
    }

    @Override
    protected void setupCollisionManager() {
        CollisionGroupManager collisionGroupManager = new CollisionGroupManager();
        context.put(CollisionGroupManager.class, collisionGroupManager);
        context.get(TypeHandlerLibrary.class).addTypeHandler(CollisionGroup.class, new CollisionGroupTypeHandler(collisionGroupManager));
    }

    @Override
    protected void setupBlockManager(AssetManager assetManager) {
        WorldAtlas worldAtlas = new NullWorldAtlas();
        BlockManagerImpl blockManager = new BlockManagerImpl(worldAtlas, assetManager);
        context.put(BlockManager.class, blockManager);
        TypeHandlerLibrary typeHandlerLibrary = context.get(TypeHandlerLibrary.class);
        typeHandlerLibrary.addTypeHandler(BlockFamily.class, new BlockFamilyTypeHandler(blockManager));
        typeHandlerLibrary.addTypeHandler(Block.class, new BlockTypeHandler(blockManager));
    }

    @Override
    protected void setupExtraDataManager(Context context) {
        context.put(ExtraBlockDataManager.class, new ExtraBlockDataManager(context));
    }

    @Override
    protected AssetManager setupEmptyAssetManager() {
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();
        assetTypeManager.switchEnvironment(context.get(ModuleManagerImpl.class).getEnvironment());

        context.put(ModuleAwareAssetTypeManager.class, assetTypeManager);
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
        return assetTypeManager.getAssetManager();
    }

    @Override
    protected AssetManager setupAssetManager() {
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();

        // cast lambdas explicitly to avoid inconsistent compiler behavior wrt. type inference
        assetTypeManager.registerCoreAssetType(Prefab.class,
                PojoPrefab::new, false, "prefabs");
        assetTypeManager.registerCoreAssetType(BlockShape.class,
                BlockShapeImpl::new, "shapes");
        assetTypeManager.registerCoreAssetType(BlockSounds.class,
                BlockSounds::new, "blockSounds");
        assetTypeManager.registerCoreAssetType(BlockTile.class,
                BlockTile::new, "blockTiles");
        assetTypeManager.registerCoreAssetType(BlockFamilyDefinition.class,
                BlockFamilyDefinition::new, "blocks");

        assetTypeManager.registerCoreAssetType(StaticSound.class, NullSound::new, "sounds");
        assetTypeManager.registerCoreAssetType(StreamingSound.class, NullStreamingSound::new, "music");

        assetTypeManager.registerCoreFormat(BlockFamilyDefinition.class,
                new BlockFamilyDefinitionFormat(assetTypeManager.getAssetManager()));

        assetTypeManager.registerCoreAssetType(UISkin.class,
                UISkin::new, "skins");
        assetTypeManager.registerCoreAssetType(BehaviorTree.class,
                BehaviorTree::new, false, "behaviors");
        assetTypeManager.registerCoreAssetType(UIElement.class,
                UIElement::new, "ui");
        assetTypeManager.registerCoreAssetType(Font.class,
                FontImpl::new, "fonts");
        assetTypeManager.registerCoreAssetType(Texture.class,
                HeadlessTexture::new, "textures", "fonts");
        assetTypeManager.registerCoreFormat(Texture.class,
                new PNGTextureFormat(Texture.FilterMode.NEAREST, path -> path.getName(2).toString().equals("textures")));
        assetTypeManager.registerCoreFormat(Texture.class,
                new PNGTextureFormat(Texture.FilterMode.LINEAR, path -> path.getName(2).toString().equals("fonts")));

        assetTypeManager.registerCoreAssetType(Shader.class,
                HeadlessShader::new, "shaders");
        assetTypeManager.registerCoreAssetType(Material.class,
                HeadlessMaterial::new, "materials");
        assetTypeManager.registerCoreAssetType(Mesh.class,
                HeadlessMesh::new, "mesh");
        assetTypeManager.registerCoreAssetType(SkeletalMesh.class,
                HeadlessSkeletalMesh::new, "skeletalMesh");
        assetTypeManager.registerCoreAssetType(MeshAnimation.class, MeshAnimationImpl::new, "animations");

        assetTypeManager.registerCoreAssetType(Atlas.class,
                Atlas::new, "atlas");
        assetTypeManager.registerCoreAssetType(Subtexture.class, Subtexture::new);

        assetTypeManager.switchEnvironment(context.get(ModuleManagerImpl.class).getEnvironment());

        context.put(ModuleAwareAssetTypeManager.class, assetTypeManager);
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
        return assetTypeManager.getAssetManager();
    }

    @Override
    protected void setupAudio() {
        NullAudioManager audioManager = new NullAudioManager();
        context.put(AudioManager.class, audioManager);
    }

    @Override
    protected void setupConfig() {
        Config config = new Config(context);
        config.loadDefaults();
        context.put(Config.class, config);
    }

    @Override
    protected void setupModuleManager(Set<Name> moduleNames) throws Exception {
        TypeRegistry typeRegistry = new TypeRegistry();
        TypeRegistry.WHITELISTED_CLASSES = ExternalApiWhitelist.CLASSES.stream().map(Class::getName).collect(Collectors.toSet());
        context.put(TypeRegistry.class, typeRegistry);

        ModuleManagerImpl moduleManager = ModuleManagerFactory.create(true);
        ModuleRegistry registry = moduleManager.getRegistry();

        DependencyResolver resolver = new DependencyResolver(registry);
        ResolutionResult result = resolver.resolve(moduleNames);

        if (result.isSuccess()) {
            ModuleEnvironment modEnv = moduleManager.loadEnvironment(result.getModules(), true);
            typeRegistry.reload(modEnv);
            logger.debug("Loaded modules: " + modEnv.getModuleIdsOrderedByDependencies());
        } else {
            logger.error("Could not resolve module dependencies for " + moduleNames);
        }

        context.put(ModuleManagerImpl.class, moduleManager);

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
    }

    @Override
    protected void setupPathManager() throws IOException {
        Path tempHome = Files.createTempDirectory("terasology-env");
        tempHome.toFile().deleteOnExit();
        PathManager.getInstance().useOverrideHomePath(tempHome);
    }

    @Override
    protected void setupComponentManager() {
        ComponentSystemManager componentSystemManager = new ComponentSystemManager(context);
        componentSystemManager.initialise();
        context.put(ComponentSystemManager.class, componentSystemManager);
    }

    @Override
    protected void setupWorldProvider() {
        WorldProvider worldProvider = mock(WorldProvider.class);
        when(worldProvider.getWorldInfo()).thenReturn(new WorldInfo());
        when(worldProvider.getTime()).thenReturn(WORLD_TIME);
        context.put(WorldProvider.class, worldProvider);
    }

    @Override
    protected void setupCelestialSystem() {
        DefaultCelestialSystem celestialSystem = new DefaultCelestialSystem(new BasicCelestialModel(), context);
        context.put(CelestialSystem.class, celestialSystem);
    }

    @Override
    protected void loadPrefabs() {

        LoadPrefabs prefabLoadStep = new LoadPrefabs(context);

        boolean complete = false;
        prefabLoadStep.begin();
        while (!complete) {
            complete = prefabLoadStep.step();
        }
    }

    @Override
    public void close() throws Exception {
        // it would be nice, if elements in the context implemented (Auto)Closeable

        // The StorageManager creates a thread pool (through TaskMaster)
        // which isn't closed automatically
        StorageManager storageManager = context.get(StorageManager.class);
        if (storageManager != null) {
            storageManager.finishSavingAndShutdown();
        }


        super.close();
    }

}
