/**
 * File: SpinTiming.java
 * Author: Maxime66410
 * Created: 2026-08-23
 * Last Modified: 2026-08-24
 */
package org.furranystudio.colorapocalypse.timer;

/**
 * The spin-tick schedule shared by the server (sound) and the client (color/title), so they can't drift apart.
 * Ticks between changes grow over time (deceleration), once the next scheduled change would land on or after {@code durationTicks},
 * that change is the landing itself, shown early by the natural slowdown, not by a separate artificial cutoff.
 */
public final class SpinTiming {

    private SpinTiming() {
    }

    public static int nextChangeAt(int ticksElapsed, int durationTicks) {
        float progress = (float) ticksElapsed / durationTicks;
        return ticksElapsed + 2 + Math.round(progress * 13);
    }

    public static boolean isFinalChange(int ticksElapsed, int durationTicks) {
        return nextChangeAt(ticksElapsed, durationTicks) >= durationTicks;
    }
}
