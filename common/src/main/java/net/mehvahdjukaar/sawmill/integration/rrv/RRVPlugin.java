package net.mehvahdjukaar.sawmill.integration.rrv;

import cc.cassian.rrv.api.ReliableRecipeViewerClientPlugin;
import cc.cassian.rrv.api.recipe.ItemView;
import net.mehvahdjukaar.sawmill.WoodcuttingEntry;
import net.mehvahdjukaar.sawmill.WoodcuttingRecipes;

public class RRVPlugin implements ReliableRecipeViewerClientPlugin {

    @Override
    public void onIntegrationInitialize() {
        ItemView.addClientRecipeProvider(recipes -> {
            for (WoodcuttingEntry entry : WoodcuttingRecipes.onClient()) {
                recipes.add(new WoodcuttingClientRecipe(entry));
            }
        });
    }
}
