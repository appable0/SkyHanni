package at.hannibal2.skyhanni.data

import at.hannibal2.skyhanni.events.LorenzTickEvent
import at.hannibal2.skyhanni.events.PacketEvent
import at.hannibal2.skyhanni.utils.LorenzLogger
import io.netty.buffer.Unpooled
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.handler.ClientboundPacketHandler
import net.hypixel.modapi.packet.HypixelPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.hypixel.modapi.serializer.PacketSerializer
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


class ModAPI {
    private val hypixelModAPI = HypixelModAPI.getInstance()
    private val logger = LorenzLogger("packet/modapi")
    private val receivedPacketQueue = ArrayDeque<S3FPacketCustomPayload>()
    private val clientBoundIdentifiers = hypixelModAPI.registry.clientboundIdentifiers

    constructor() {
        val setPacketSender = runCatching { hypixelModAPI.setPacketSender(this::send) }.isSuccess
        registerHandler()
        hypixelModAPI.subscribeToEventPacket(ClientboundLocationPacket::class.java)
        logger.log("Initialized Mod API. Set packet sender: $setPacketSender")
    }

    private fun send(hypixelPacket: HypixelPacket): Boolean {
        val handler = Minecraft.getMinecraft().netHandler ?: return false
        val packetBuffer = PacketBuffer(Unpooled.buffer())
        val packetSerializer = PacketSerializer(packetBuffer)
        hypixelPacket.write(packetSerializer)
        val packet = C17PacketCustomPayload(hypixelPacket.identifier, packetBuffer)
        handler.addToSendQueue(packet)
        return true
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        val packet = event.packet as? S3FPacketCustomPayload ?: return
        if (!clientBoundIdentifiers.contains(packet.channelName)) return
        // queueing logic: we don't want to receive packets when we aren't ready to send,
        // so packets are queued until they can be handled
        packet.bufferData.retain()
        if (canHandlePackets()) {
            handlePacket(packet)
        } else {
            receivedPacketQueue.addFirst(packet)
        }
    }

    @SubscribeEvent
    fun onTick(event: LorenzTickEvent) {
        while (canHandlePackets() && receivedPacketQueue.isNotEmpty()) {
            val packet = receivedPacketQueue.removeLast()
            handlePacket(packet)
        }
    }

    private fun canHandlePackets() = Minecraft.getMinecraft().netHandler != null

    private fun handlePacket(packet: S3FPacketCustomPayload) {
        hypixelModAPI.handle(packet.channelName, PacketSerializer(packet.bufferData))
        packet.bufferData.release()
    }

    private fun registerHandler() {
        hypixelModAPI.registerHandler(object : ClientboundPacketHandler {
            override fun onLocationEvent(packet: ClientboundLocationPacket) {
                logger.log("Received $packet")
            }
        })
    }
}

