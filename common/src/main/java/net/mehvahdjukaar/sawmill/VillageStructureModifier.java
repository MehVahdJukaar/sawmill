package net.mehvahdjukaar.sawmill;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// Thanks to TelepathicGrunt
public class VillageStructureModifier {
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace("empty"));
    private static final ResourceKey<StructureProcessorList> MOSSY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace("mossify_10_percent"));

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                          ResourceLocation poolRL,
                                          ResourceLocation nbtPieceRL,
                                          Holder<StructureProcessorList> processors,
                                          int weight) {

        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) {
            return;
        }

        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL.toString(), processors).apply(StructureTemplatePool.Projection.RIGID);

        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    public static void setup(RegistryAccess registryAccess) {
        SawmillMod.LOGGER.info("Injecting Carpenter Village Houses");

        Registry<StructureTemplatePool> templatePoolRegistry = registryAccess.registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = registryAccess.registry(Registries.PROCESSOR_LIST).orElseThrow();

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("plains"),
                SawmillMod.res("plains_small"), true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("plains"),
                SawmillMod.res("plains_medium"), true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("snowy"),
                SawmillMod.res("snowy_small"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("snowy"),
                SawmillMod.res("snowy_medium"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("savanna"),
                SawmillMod.res("savanna_small"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("savanna"),
                SawmillMod.res("savanna_big"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("taiga"),
                SawmillMod.res("taiga_small"), true, 1);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("taiga"),
                SawmillMod.res("taiga_big"), true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("taiga"),
                SawmillMod.res("taiga_medium"), true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("desert"),
                SawmillMod.res("desert_big"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.withDefaultNamespace("desert"),
                SawmillMod.res("desert_small"), false, 2);


        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.fromNamespaceAndPath("atmospheric", "shrubland"),
                SawmillMod.res("atmospheric/shrubland_small"), false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                ResourceLocation.fromNamespaceAndPath("atmospheric","shrubland"),
                SawmillMod.res( "atmospheric/shrubland_medium"), false, 2);
    }

    private static void addVillageHouse(Registry<StructureTemplatePool> templatePoolRegistry,
                                        Registry<StructureProcessorList> processorListRegistry,
                                        ResourceLocation villageRes, ResourceLocation pieceName,
                                        boolean mossy, int weight) {

        Holder<StructureProcessorList> normalProcessor =
                mossy ? processorListRegistry.getHolderOrThrow(MOSSY_PROCESSOR_LIST_KEY) :
                        processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        String modId = villageRes.getNamespace();
        String villageName = villageRes.getPath();

        addBuildingToPool(templatePoolRegistry, ResourceLocation.fromNamespaceAndPath(modId, "village/" + villageName + "/houses"),
                pieceName, normalProcessor, weight);

        // The zombie variant processor list may not exist (e.g. some modded villages don't define one),
        // so only inject the zombie house if its processor list is actually present.
        Optional<Holder.Reference<StructureProcessorList>> zombieProcessor = processorListRegistry.getHolder(ResourceKey.create(
                Registries.PROCESSOR_LIST, ResourceLocation.fromNamespaceAndPath(modId, "zombie_" + villageName)
        ));

        zombieProcessor.ifPresent(processor -> addBuildingToPool(templatePoolRegistry,
                ResourceLocation.fromNamespaceAndPath(modId, "village/" + villageName + "/zombie/houses"),
                pieceName, processor, weight));
    }


}