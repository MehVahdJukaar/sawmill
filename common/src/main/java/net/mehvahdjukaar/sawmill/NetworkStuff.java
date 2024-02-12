package net.mehvahdjukaar.sawmill;

import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkDir;
import net.minecraft.network.FriendlyByteBuf;

public class NetworkStuff {

    public static void init(){}

    public static final ChannelHandler CHANNEL = ChannelHandler.builder(SawmillMod.MOD_ID)
            .register(NetworkDir.PLAY_TO_CLIENT, SyncRecipeOrder.class, SyncRecipeOrder::new)
            .build();

    public record SyncRecipeOrder(IntList list) implements Message {

        public SyncRecipeOrder(FriendlyByteBuf buf) {
            this(buf.readIntIdList());
        }

        @Override
        public void writeToBuffer(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeIntIdList(list);
        }

        @Override
        public void handle(ChannelHandler.Context context) {
            RecipeSorter.acceptOrder(list);
        }
    }
}
