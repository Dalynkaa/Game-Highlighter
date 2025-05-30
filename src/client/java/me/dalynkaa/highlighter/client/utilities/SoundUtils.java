package me.dalynkaa.highlighter.client.utilities;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Утилитарный класс для воспроизведения звуков в клиентской части
 */
public class SoundUtils {

    /**
     * Воспроизводит звук с заданными параметрами
     *
     * @param soundEvent звуковое событие, которое нужно воспроизвести
     * @param volume громкость (от 0.0 до 1.0)
     * @param pitch высота тона (от 0.5 до 2.0, где 1.0 - нормальная высота)
     */
    public static void playSound(SoundEvent soundEvent, float volume, float pitch) {
        if (soundEvent == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        client.getSoundManager().play(
            PositionedSoundInstance.master(soundEvent, pitch, volume)
        );
    }

    /**
     * Воспроизводит звук с нормальной громкостью и высотой тона
     *
     * @param soundEvent звуковое событие, которое нужно воспроизвести
     */
    public static void playSound(SoundEvent soundEvent) {
        playSound(soundEvent, 1.0f, 1.0f);
    }

    /**
     * Воспроизводит звук из CustomNotificationEffects по имени эффекта
     *
     * @param effectName имя эффекта из CustomNotificationEffects
     * @param volume громкость (от 0.0 до 1.0)
     * @param pitch высота тона (от 0.5 до 2.0, где 1.0 - нормальная высота)
     */
    public static void playEffectSound(String effectName, float volume, float pitch) {
        if (effectName == null) return;

        CustomNotificationEffects effect = CustomNotificationEffects.getEffectByName(effectName);
        if (effect != null && effect.getSoundEvent() != null) {
            playSound(effect.getSoundEvent(), volume, pitch);
        }
    }

    /**
     * Воспроизводит звук из CustomNotificationEffects по имени эффекта
     * с нормальной громкостью и высотой тона
     *
     * @param effectName имя эффекта из CustomNotificationEffects
     */
    public static void playEffectSound(String effectName) {
        playEffectSound(effectName, 1.0f, 1.0f);
    }
}
