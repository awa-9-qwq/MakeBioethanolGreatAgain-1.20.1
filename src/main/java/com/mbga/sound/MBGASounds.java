package com.mbga.sound;

import com.mbga.MBGA;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * MBGA 的自定义音效。
 *
 * <p>音频文件来自 {@code libs/}（已转换为 OGG Vorbis），放在
 * {@code assets/mbga/sounds/weapon/{tap,flick,drag}.ogg}，由 {@code assets/mbga/sounds.json} 声明。
 * 「Hold」与「Tap」共用同一个音效。
 */
public final class MBGASounds {
    public static SoundEvent WEAPON_TAP;
    public static SoundEvent WEAPON_FLICK;
    public static SoundEvent WEAPON_DRAG;

    private MBGASounds() {
    }

    public static void register() {
        WEAPON_TAP = register("weapon.tap");
        WEAPON_FLICK = register("weapon.flick");
        WEAPON_DRAG = register("weapon.drag");
    }

    private static SoundEvent register(String path) {
        Identifier id = new Identifier(MBGA.MOD_ID, path);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}
