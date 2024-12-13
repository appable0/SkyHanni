package at.hannibal2.skyhanni.features.combat

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.model.SkyblockStat
import at.hannibal2.skyhanni.events.GuiRenderEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.RenderUtils.renderRenderable
import at.hannibal2.skyhanni.utils.renderables.RenderableString
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyHanniModule
object FerocityDisplay {

    private val config get() = SkyHanniMod.feature.combat.ferocityDisplay
    private val display = RenderableString()

    @SubscribeEvent
    fun onRenderOverlay(event: GuiRenderEvent) {
        if (!isEnabled()) return
        SkyblockStat.FEROCITY.displayValue?.let {
            display.text = it
            config.position.renderRenderable(display, posLabel = "Ferocity Display")
        }
    }

    fun isEnabled() = LorenzUtils.inSkyBlock && config.enabled
}
