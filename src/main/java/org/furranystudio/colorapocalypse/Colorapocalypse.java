package org.furranystudio.colorapocalypse;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.furranystudio.colorapocalypse.client.ClientRouletteState;
import org.furranystudio.colorapocalypse.client.RouletteBarLayer;
import org.furranystudio.colorapocalypse.color.ColorBlockRegistry;
import org.furranystudio.colorapocalypse.color.DestructionQueue;
import org.furranystudio.colorapocalypse.color.ItemColorReport;
import org.furranystudio.colorapocalypse.command.ColorApocalypseCommand;
import org.furranystudio.colorapocalypse.network.ModNetworking;
import org.furranystudio.colorapocalypse.timer.AutoTrigger;
import org.furranystudio.colorapocalypse.timer.RouletteSequence;
import org.slf4j.Logger;

import java.util.List;

@Mod(Colorapocalypse.MODID)
public class Colorapocalypse {

    public static final String MODID = "colorapocalypse";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Colorapocalypse(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        Config.registerSettings();

        ColorApocalypseCommand.register();
        DestructionQueue.register();
        AutoTrigger.register();
        RouletteSequence.register();
        ModNetworking.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientRouletteState.register();
            RouletteBarLayer.register();
        }

        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ColorBlockRegistry.build();

        LOGGER.info("[ColorApocalypse] Block color classification:");
        for (DyeColor color : DyeColor.values()) {
            List<Block> blocks = ColorBlockRegistry.getBlocksFor(color);
            LOGGER.info(" - {} ({} blocks): {}", color.getName(), blocks.size(), blocks);
        }

        ItemColorReport.write();
    }
}
