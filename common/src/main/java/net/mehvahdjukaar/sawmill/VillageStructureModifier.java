package net.mehvahdjukaar.sawmill;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Thanks to TelepathicGrunt
public class VillageStructureModifier {
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registries.PROCESSOR_LIST, Identifier.withDefaultNamespace("empty"));
    private static final ResourceKey<StructureProcessorList> MOSSY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registries.PROCESSOR_LIST, Identifier.withDefaultNamespace("mossify_10_percent"));

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                          Identifier poolId,
                                          Identifier nbtPieceId,
                                          Holder<StructureProcessorList> processors,
                                          int weight) {

        StructureTemplatePool pool = templatePoolRegistry.getValue(poolId);
        if (pool == null) {
            return;
        }

        StructurePoolElement piece = StructurePoolElement.legacy(nbtPieceId.toString(), processors)
                .apply(StructureTemplatePool.Projection.RIGID);

        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    public static void setup(RegistryAccess registryAccess) {
        if (CommonConfigs.CARPENTER_HOUSE_SPAWN_RATE.get() <= 0) return;
        SawmillMod.LOGGER.info("Injecting Carpenter Village Houses");

        Registry<StructureTemplatePool> templatePoolRegistry = registryAccess.lookupOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processorListRegistry = registryAccess.lookupOrThrow(Registries.PROCESSOR_LIST);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("plains"),
                SawmillMod.res("plains_small"), true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("plains"),
                SawmillMod.res("plains_medium"), true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("snowy"),
                SawmillMod.res("snowy_small"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("snowy"),
                SawmillMod.res("snowy_medium"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("savanna"),
                SawmillMod.res("savanna_small"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("savanna"),
                SawmillMod.res("savanna_big"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("taiga"),
                SawmillMod.res("taiga_small"), true, 1);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("taiga"),
                SawmillMod.res("taiga_big"), true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("taiga"),
                SawmillMod.res("taiga_medium"), true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("desert"),
                SawmillMod.res("desert_big"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.withDefaultNamespace("desert"),
                SawmillMod.res("desert_small"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.fromNamespaceAndPath("atmospheric", "shrubland"),
                SawmillMod.res("atmospheric/shrubland_small"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                Identifier.fromNamespaceAndPath("atmospheric", "shrubland"),
                SawmillMod.res("atmospheric/shrubland_medium"), false, 2);
    }

    private static void addVillageHouse(Registry<StructureTemplatePool> templatePoolRegistry,
                                        Registry<StructureProcessorList> processorListRegistry,
                                        Identifier villageId, Identifier pieceName,
                                        boolean mossy, int baseWeight) {

        int weight = (int) Math.round(baseWeight * CommonConfigs.CARPENTER_HOUSE_SPAWN_RATE.get());
        if (weight <= 0) return;

        Holder<StructureProcessorList> normalProcessor = processorListRegistry.getOrThrow(
                mossy ? MOSSY_PROCESSOR_LIST_KEY : EMPTY_PROCESSOR_LIST_KEY);

        String modId = villageId.getNamespace();
        String villageName = villageId.getPath();

        addBuildingToPool(templatePoolRegistry, Identifier.fromNamespaceAndPath(modId, "village/" + villageName + "/houses"),
                pieceName, normalProcessor, weight);

        // The zombie variant processor list may not exist (e.g. some modded villages don't define one),
        // so only inject the zombie house if its processor list is actually present.
        Optional<Holder.Reference<StructureProcessorList>> zombieProcessor = processorListRegistry.get(ResourceKey.create(
                Registries.PROCESSOR_LIST, Identifier.fromNamespaceAndPath(modId, "zombie_" + villageName)
        ));

        zombieProcessor.ifPresent(processor -> addBuildingToPool(templatePoolRegistry,
                Identifier.fromNamespaceAndPath(modId, "village/" + villageName + "/zombie/houses"),
                pieceName, processor, weight));
    }
}
