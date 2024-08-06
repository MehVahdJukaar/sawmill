package net.mehvahdjukaar.sawmill;

import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Blocks;

public class NetworkStuff {

    public static void init(){
        NetworkHelper.addNetworkRegistration(NetworkStuff::registerMessages, 1);
    }

    private static void registerMessages(NetworkHelper.RegisterMessagesEvent event) {
        event.registerClientBound(SyncRecipeOrder.SYNC_RECIPE_ORDER);
    }


    public record SyncRecipeOrder(IntList list) implements Message {

        public static final CustomPacketPayload.TypeAndCodec<RegistryFriendlyByteBuf, SyncRecipeOrder> SYNC_RECIPE_ORDER =
                Message.makeType(SawmillMod.res("sync_recipe_order"), SyncRecipeOrder::new);

        public SyncRecipeOrder(FriendlyByteBuf buf) {
            this(buf.readIntIdList());
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeIntIdList(list);
        }

        @Override
        public void handle(Context context) {
            RecipeSorter.acceptOrder(list);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SYNC_RECIPE_ORDER.type();
        }
    }
}
