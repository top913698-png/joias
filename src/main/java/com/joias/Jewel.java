package com.joias;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.potion.PotionEffectType;

public enum Jewel {
    RESISTENCIA("resistencia", "Joia da Resistência", NamedTextColor.GRAY,
            PotionEffectType.RESISTANCE, 5, 2, true, "Concede o efeito de Resistência."),
    FORCA("forca", "Joia da Força", NamedTextColor.DARK_RED,
            PotionEffectType.STRENGTH, 5, 2, true, "Concede o efeito de Força."),
    VELOCIDADE("velocidade", "Joia da Velocidade", NamedTextColor.AQUA,
            PotionEffectType.SPEED, 5, 2, true, "Concede o efeito de Velocidade."),
    REGENERACAO("regeneracao", "Joia da Regeneração", NamedTextColor.LIGHT_PURPLE,
            PotionEffectType.REGENERATION, 5, 2, true, "Concede o efeito de Regeneração."),
    VIDA("vida", "Joia da Vida", NamedTextColor.RED,
            PotionEffectType.HEALTH_BOOST, 5, 3, true, "Concede o efeito de Vida Extra."),
    PULO("pulo", "Joia do Pulo", NamedTextColor.GREEN,
            PotionEffectType.JUMP_BOOST, 5, 5, true, "Concede um super pulo."),
    TITA("tita", "Joia do Titã", NamedTextColor.GOLD,
            null, 20, 1, false, "Você fica gigante e com vida extra."),
    LADRAO("ladrao", "Joia do Ladrão", NamedTextColor.DARK_GRAY,
            PotionEffectType.INVISIBILITY, 45, 1, false, "Você fica invisível, sem partículas.");

    private final String id;
    private final String displayName;
    private final NamedTextColor color;
    private final PotionEffectType effect;
    private final int defaultSeconds;
    private final int defaultLevel;
    private final boolean particles;
    private final String description;

    Jewel(String id, String displayName, NamedTextColor color, PotionEffectType effect,
          int defaultSeconds, int defaultLevel, boolean particles, String description) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.effect = effect;
        this.defaultSeconds = defaultSeconds;
        this.defaultLevel = defaultLevel;
        this.particles = particles;
        this.description = description;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public NamedTextColor color() { return color; }
    /** Null para a Joia do Titã (que usa atributos em vez de poção). */
    public PotionEffectType effect() { return effect; }
    public int defaultSeconds() { return defaultSeconds; }
    public int defaultLevel() { return defaultLevel; }
    public boolean particles() { return particles; }
    public String description() { return description; }

    public static Jewel fromId(String id) {
        if (id == null) return null;
        String normalized = id.toLowerCase()
                .replace("ã", "a").replace("ç", "c").replace("ê", "e").replace("é", "e");
        for (Jewel j : values()) {
            if (j.id.equals(normalized)) return j;
        }
        return null;
    }
}
