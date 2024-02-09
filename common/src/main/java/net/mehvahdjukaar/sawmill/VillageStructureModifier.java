package net.mehvahdjukaar.sawmill;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import java.util.ArrayList;
import java.util.List;

// Thanks to TelepathicGrunt
public class VillageStructureModifier {
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("empty"));
    private static final ResourceKey<StructureProcessorList> MOSSY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("mossify_10_percent"));

    private static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                          ResourceLocation poolRL,
                                          String nbtPieceRL,
                                          Holder<StructureProcessorList> processors,
                                          int weight) {

        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) {
            return;
        }

        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL, processors).apply(StructureTemplatePool.Projection.RIGID);

        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    public static void setup(RegistryAccess registryAccess) {
        SawmillMod.LOGGER.info("Injecting Carpenter Village Houses");

        Registry<StructureTemplatePool> templatePoolRegistry = registryAccess.registry(Registry.TEMPLATE_POOL_REGISTRY).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = registryAccess.registry(Registry.PROCESSOR_LIST_REGISTRY).orElseThrow();

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "plains", "sawmill:plains_small", true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "plains", "sawmill:plains_medium", true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "snowy", "sawmill:snowy_small", false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "snowy", "sawmill:snowy_medium", false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "savanna", "sawmill:savanna_small", false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "savanna", "sawmill:savanna_big", false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "taiga", "sawmill:taiga_small", true, 1);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "taiga", "sawmill:taiga_big", true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "taiga", "sawmill:taiga_medium", true, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "desert", "sawmill:desert_big", false, 2);

        addVillageHouse(templatePoolRegistry, processorListRegistry,
                "desert", "sawmill:desert_small", false, 2);
    }

    private static void addVillageHouse(Registry<StructureTemplatePool> templatePoolRegistry,
                                        Registry<StructureProcessorList> processorListRegistry,
                                        String villageName, String pieceName,
                                        boolean mossy, int weight) {

        Holder<StructureProcessorList> normalProcessor =
                mossy ? processorListRegistry.getHolderOrThrow(MOSSY_PROCESSOR_LIST_KEY) :
                        processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        Holder<StructureProcessorList> zombieProcessor = processorListRegistry.getHolderOrThrow(ResourceKey.create(
                Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("zombie_" + villageName)
        ));

        addBuildingToPool(templatePoolRegistry, new ResourceLocation("village/" + villageName + "/houses"),
                pieceName, normalProcessor, weight);

        addBuildingToPool(templatePoolRegistry, new ResourceLocation("village/" + villageName + "/zombie/houses"),
                pieceName, zombieProcessor, weight);
    }


}