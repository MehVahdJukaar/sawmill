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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class BiomeToWoodList {
    private final Map<VillagerType, List<WoodType>> map = new HashMap<>();

    public List<WoodType> getWoodsForType(VillagerType villagerType) {
        return map.getOrDefault(villagerType, List.of(VanillaWoodTypes.OAK));
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
            VillagerType type = VillagerType.byBiome(e.getKey());
            if (type == VillagerType.PLAINS && e.getKey().getRegisteredName().equals("minecraft:plains")) {
                continue;
            }
            List<String> tryAdd = new ArrayList<>();
            List<Fallback> fallbacks = new ArrayList<>();
            for (var v : e.getValue()) {
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
                    t = WoodTypeRegistry.INSTANCE.get(ResourceLocation.parse(s));
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
                if (t != null) woods.add(t);
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
            if (!woods.isEmpty()) {
                this.map.put(type, woods);
            }
        }
    }
}
