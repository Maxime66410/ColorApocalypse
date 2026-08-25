/**
 * File: ModSounds.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.sound;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

// Vanilla sound events used by the mod, looked up by their resource location.
public final class ModSounds {

    public static final Holder<SoundEvent> ROULETTE_APPEAR = byId("minecraft:event.mob_effect.trial_omen");
    public static final Holder<SoundEvent> ROULETTE_SPIN_TICK = byId("minecraft:entity.experience_orb.pickup");
    public static final Holder<SoundEvent> ROULETTE_REVEAL = byId("minecraft:entity.ender_dragon.growl");
    public static final Holder<SoundEvent> COUNTDOWN_TICK = byId("minecraft:block.note_block.pling");

    private ModSounds() {
    }

    private static Holder<SoundEvent> byId(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(Identifier.parse(id)).orElseThrow();
    }
}
