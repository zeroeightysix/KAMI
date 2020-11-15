package me.zeroeightsix.kami.feature.module

import io.github.fablabsmc.fablabs.api.fiber.v1.annotation.Setting
import me.zeroeightsix.kami.Colour
import me.zeroeightsix.kami.target.ItemCategory
import me.zeroeightsix.kami.target.ItemSupplier

@Module.Info(
    name = "ItemHighlight",
    category = Module.Category.RENDER,
    description = "Highlights certain Item Slots"
)
object ItemHighlight: Module() {
    @Setting
    var highlightedItems = ItemSupplier(
        mapOf(
            ItemCategory.FOOD to Colour(1f, 1f, 1f, 0f)
        ),
        mapOf()
    )
}