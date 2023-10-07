package turniplabs.industry.recipes

import net.minecraft.core.item.Item
import net.minecraft.core.item.ItemStack
import turniplabs.industry.Industry2

object RecipesCutter {
    private val recipeList = HashMap<Any?, Any?>()

    init {
        addRecipe(Industry2.copperIngot.id, ItemStack(Industry2.itemCopperCable, 4))
        addRecipe(Industry2.tinIngot.id, ItemStack(Industry2.itemTinCable, 4))
        addRecipe(Item.ingotGold.id, ItemStack(Industry2.itemGoldCable, 4))
        addRecipe(Item.ingotSteel.id, ItemStack(Industry2.itemSteelCable, 4))
    }

    fun addRecipe(input: Int, output: ItemStack) {
        recipeList[input] = output
    }

    fun getResult(i: Int): ItemStack? {
        return recipeList[i] as ItemStack?
    }

    fun getRecipeList(): HashMap<Any?, Any?> {
        return recipeList
    }
}