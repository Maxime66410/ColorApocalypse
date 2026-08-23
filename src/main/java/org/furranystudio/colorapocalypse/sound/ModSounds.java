package org.furranystudio.colorapocalypse.sound;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Vanilla sound events used by the mod, looked up by their resource location. */
public final class ModSounds {

    /** Played when the roulette HUD appears. */
    public static final Holder<SoundEvent> ROULETTE_APPEAR = byId("minecraft:event.mob_effect.trial_omen");
    /** Played on each color the roulette lands on while spinning; vary the pitch as it slows down. */
    public static final Holder<SoundEvent> ROULETTE_SPIN_TICK = byId("minecraft:entity.experience_orb.pickup");
    /** Played when the roulette stops on its final color. */
    public static final Holder<SoundEvent> ROULETTE_REVEAL = byId("minecraft:entity.ender_dragon.growl");
    /** Played on each second of the 10s countdown. */
    public static final Holder<SoundEvent> COUNTDOWN_TICK = byId("minecraft:block.note_block.pling");

    private ModSounds() {
    }

    private static Holder<SoundEvent> byId(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(Identifier.parse(id)).orElseThrow();
    }
}
