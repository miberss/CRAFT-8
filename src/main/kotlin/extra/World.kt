package me.mibers.Extra

import net.minestom.server.MinecraftServer
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.particle.Particle
import net.minestom.server.registry.DynamicRegistry
import net.minestom.server.world.biome.Biome
import net.minestom.server.world.biome.BiomeEffects
import net.minestom.server.world.biome.BiomeParticle

fun initInstance(instanceContainer: InstanceContainer) {
    val biome = Biome.builder()
        .effects(
            BiomeEffects.builder()
                .skyColor(0x000000)
                .fogColor(0x000000)
                .biomeParticle(
                    BiomeParticle(
                        0.001F,
                        Particle.BLOCK_CRUMBLE.withBlock(Block.BLACK_CONCRETE)
                    )
                )
                .build()
        )
        .build()
    val biomeRegistry = MinecraftServer.getBiomeRegistry()
    biomeRegistry.register("pink", biome)
    instanceContainer.setChunkSupplier(::LightingChunk)
    instanceContainer.timeRate = 0
    instanceContainer.time = 6000
    instanceContainer.setGenerator { unit ->
        unit.modifier().fillHeight(0, 39, Block.BLACK_CONCRETE)
        unit.modifier().fillHeight(39, 40, Block.BLACK_STAINED_GLASS)
        unit.modifier().fillBiome(DynamicRegistry.Key.of("pink"))
    }
}