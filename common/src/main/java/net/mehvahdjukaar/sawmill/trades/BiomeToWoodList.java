package net.mehvahdjukaar.sawmill.trades;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.set.wood.VanillaWoodTypes;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class BiomeToWoodList {
    private static final List<WoodType> DEFAULT_WOODS = List.of(VanillaWoodTypes.OAK);

    private final Map<ResourceKey<Biome>, List<WoodType>> byBiome = new HashMap<>();
    private final Map<VillagerType, List<WoodType>> byVillagerType = new HashMap<>();

    /**
     * Picks one of the woods assigned to the biome the trader is currently in,
     * falling back to the ones of its villager type and then to oak.
     */
    @Nullable
    public WoodType getRandomWood(Entity trader, RandomSource random) {
        if (!(trader instanceof VillagerDataHolder villager)) return null;
        List<WoodType> woods = null;
        ResourceKey<Biome> biome = trader.level().getBiome(trader.blockPosition()).unwrapKey().orElse(null);
        if (biome != null) woods = byBiome.get(biome);
        if (woods == null) woods = byVillagerType.get(villager.getVillagerData().getType());
        if (woods == null || woods.isEmpty()) woods = DEFAULT_WOODS;
        return woods.get(random.nextInt(woods.size()));
    }

    private record Fallback(String wood) {
        public static final Codec<Fallback> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("fallback").forGetter(Fallback::wood)
        ).apply(i, Fallback::new));
    }

    private static final Codec<Map<Holder<Biome>, List<Either<String, Fallback>>>> BIOME_TO_WOODS =
            Codec.unboundedMap(RegistryFixedCodec.create(Registries.BIOME),
                    Codec.list(Codec.either(Codec.STRING, Fallback.CODEC)));

    public static final Codec<BiomeToWoodList> CODEC = BIOME_TO_WOODS.xmap(
            BiomeToWoodList::new,
            list -> new HashMap<>()
    );

    public BiomeToWoodList(Map<Holder<Biome>, List<Either<String, Fallback>>> map) {
        for (var e : map.entrySet()) {
            ResourceKey<Biome> biome = e.getKey().unwrapKey().orElse(null);
            if (biome == null) continue;
            List<WoodType> woods = resolveWoods(e.getValue());
            if (woods.isEmpty()) continue;

            this.byBiome.put(biome, woods);

            // byBiome() falls back to PLAINS for every biome it doesn't know about, so only the plains biome
            // itself may define what a plains villager sells. Otherwise a beach or dark forest entry would
            // hijack all the villagers coming from ordinary biomes
            VillagerType type = VillagerType.byBiome(e.getKey());
            if (type != VillagerType.PLAINS || biome == Biomes.PLAINS) {
                List<WoodType> forType = this.byVillagerType.computeIfAbsent(type, t -> new ArrayList<>());
                for (WoodType w : woods) {
                    if (!forType.contains(w)) forType.add(w);
                }
            }
        }
    }

    private static List<WoodType> resolveWoods(List<Either<String, Fallback>> entries) {
        List<String> tryAdd = new ArrayList<>();
        List<Fallback> fallbacks = new ArrayList<>();
        for (var v : entries) {
            if (v.left().isPresent()) {
                tryAdd.add(v.left().get());
            } else if (v.right().isPresent()) {
                fallbacks.add(v.right().get());
            }
        }
        List<WoodType> woods = new ArrayList<>();
        for (String s : tryAdd) {
            WoodType t = null;
            ResourceLocation res = ResourceLocation.tryParse(s);
            if (res != null) {
                t = WoodTypeRegistry.INSTANCE.get(res);
            }
            if (t == null) {
                Pattern matcher = Pattern.compile(s);
                for (WoodType w : WoodTypeRegistry.INSTANCE) {
                    if (matcher.matcher(w.id.toString()).find()) {
                        t = w;
                        break;
                    }
                }
            }
            if (t != null && !woods.contains(t)) woods.add(t);
        }
        if (woods.isEmpty()) {
            for (Fallback f : fallbacks) {
                WoodType t = WoodTypeRegistry.INSTANCE.get(ResourceLocation.parse(f.wood));
                if (t != null) {
                    woods.add(t);
                    break;
                }
            }
        }
        return woods;
    }
}
