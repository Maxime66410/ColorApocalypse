/**
 * File: Platform.java
 * Author: Maxime66410
 * Created: 2026-08-27
 * Last Modified: 2026-08-27
 */
package org.furranystudio.colorapocalypse;

import java.nio.file.Path;

// Holds the one thing common code needs from the loader that isn't already vanilla API:
// the game directory. Each loader's bootstrap class calls init() before anything else runs.
public final class Platform {

    private static Path gameDir;

    private Platform() {
    }

    public static void init(Path gameDir) {
        Platform.gameDir = gameDir;
    }

    public static Path getGameDir() {
        return gameDir;
    }
}
