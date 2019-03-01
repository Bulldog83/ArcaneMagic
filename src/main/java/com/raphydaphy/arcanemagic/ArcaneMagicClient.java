package com.raphydaphy.arcanemagic;

import com.raphydaphy.arcanemagic.block.entity.*;
import com.raphydaphy.arcanemagic.client.ClientEvents;
import com.raphydaphy.arcanemagic.client.particle.ParticleRenderer;
import com.raphydaphy.arcanemagic.client.render.*;
import com.raphydaphy.arcanemagic.init.ArcaneMagicConstants;
import com.raphydaphy.arcanemagic.network.ClientBlockEntityUpdatePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.world.WorldTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

public class ArcaneMagicClient implements ClientModInitializer
{

	@Override
	public void onInitializeClient()
	{
		BlockEntityRendererRegistry.INSTANCE.register(AltarBlockEntity.class, new AltarRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(AnalyzerBlockEntity.class, new AnalyzerRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(CrystalInfuserBlockEntity.class, new CrystalInfuserRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(MixerBlockEntity.class, new MixerRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(PipeBlockEntity.class, new PipeRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(SmelterBlockEntity.class, new SmelterRenderer());
		BlockEntityRendererRegistry.INSTANCE.register(TransfigurationTableBlockEntity.class, new TransfigurationTableRenderer());

		ClientSidePacketRegistry.INSTANCE.register(ClientBlockEntityUpdatePacket.ID, new ClientBlockEntityUpdatePacket.Handler());

		ClientSpriteRegistryCallback.EVENT.register((atlaxTexture, registry) -> {
			registry.register(ArcaneMagicConstants.GLOW_PARTICLE_TEXTURE);
			registry.register(ArcaneMagicConstants.SMOKE_PARTICLE_TEXTURE);
		});

		ClientTickCallback.EVENT.register((client) -> {
			ParticleRenderer.INSTANCE.update();
		});
	}
}
