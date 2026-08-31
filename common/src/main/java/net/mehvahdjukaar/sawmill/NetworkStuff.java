package net.mehvahdjukaar.sawmill;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NetworkStuff {

    private static final int CHUNK_SIZE = 512;

    public static void init() {
        NetworkHelper.addNetworkRegistration(NetworkStuff::registerMessages, 2);
    }

    private static void registerMessages(NetworkHelper.RegisterMessagesEvent event) {
        event.registerClientBound(SyncWoodcuttingRecipes.TYPE);
    }

    public static void sendRecipesToClient(@Nullable ServerPlayer player) {
        List<WoodcuttingEntry> all = WoodcuttingRecipes.all();
        boolean first = true;
        for (int from = 0; from < all.size() || first; from += CHUNK_SIZE) {
            List<WoodcuttingEntry> chunk = all.subList(from, Math.min(from + CHUNK_SIZE, all.size()));
            send(player, new SyncWoodcuttingRecipes(first, chunk));
            first = false;
        }
    }

    private static void send(@Nullable ServerPlayer player, SyncWoodcuttingRecipes message) {
        if (player != null) {
            NetworkHelper.sendToClientPlayer(player, message);
        } else {
            NetworkHelper.sendToAllClientPlayers(message);
        }
    }

    public record SyncWoodcuttingRecipes(boolean replace, List<WoodcuttingEntry> entries) implements Message {

        private static final StreamCodec<RegistryFriendlyByteBuf, List<WoodcuttingEntry>> ENTRIES_CODEC =
                WoodcuttingEntry.STREAM_CODEC.apply(ByteBufCodecs.list());

        public static final CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, SyncWoodcuttingRecipes> TYPE =
                Message.makeType(SawmillMod.res("sync_woodcutting_recipes"), SyncWoodcuttingRecipes::new);

        public SyncWoodcuttingRecipes(RegistryFriendlyByteBuf buf) {
            this(buf.readBoolean(), ENTRIES_CODEC.decode(buf));
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(replace);
            ENTRIES_CODEC.encode(buf, entries);
        }

        @Override
        public void handle(Context context) {
            if (replace) {
                WoodcuttingRecipes.set(entries);
            } else {
                WoodcuttingRecipes.append(entries);
            }
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE.type();
        }
    }
}
