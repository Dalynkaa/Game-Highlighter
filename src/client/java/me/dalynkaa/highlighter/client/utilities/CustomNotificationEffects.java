package me.dalynkaa.highlighter.client.utilities;

import lombok.Getter;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public enum CustomNotificationEffects {

    NONE(Text.translatable("effects.none"),null),
    AMETHYST_SHARD(Text.translatable("effects.amethyst_shard"),SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK),
    BELL(Text.translatable("effects.bell"),SoundEvents.BLOCK_BELL_USE),
    ANVIL(Text.translatable("effects.anvil"),SoundEvents.BLOCK_ANVIL_LAND);

    final String name;
    @Getter
    final MutableText displayName;
    @Getter
    final SoundEvent soundEvent;

    CustomNotificationEffects(MutableText displayName, SoundEvent soundEvent) {
        this.name = this.name().toLowerCase();
        this.displayName = displayName;
        this.soundEvent = soundEvent;
    }

    public String getName() {
        return name;
    }

    public static CustomNotificationEffects getEffectByName(String name){
        for (CustomNotificationEffects effect: CustomNotificationEffects.values()){
            if (effect.getName().equalsIgnoreCase(name)){
                return effect;
            }
        }
        return null;
    }
    public static CustomNotificationEffects[] getEffects(){
        return CustomNotificationEffects.values();
    }
}
