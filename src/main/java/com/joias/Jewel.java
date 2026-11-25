package com.joias;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.potion.PotionEffectType;

public enum Jewel {
    RESISTENCIA("resistencia", "Joia da Resistência", TextColor.color(0x2F6BFF),
            PotionEffectType.RESISTANCE, 5, 2, true, "Concede o efeito de Resistência."),
    FORCA("forca", "Joia da Força", TextColor.color(0xFF8A1F),
            PotionEffectType.STRENGTH, 5, 2, true, "Concede o efeito de Força."),
    VELOCIDADE("velocidade", "Joia da Velocidade", TextColor.color(0x2FE0E8),
            PotionEffectType.SPEED, 5, 2, true, "Concede o efeito de Velocidade."),
    REGENERACAO("regeneracao", "Joia da Regeneração", TextColor.color(0xFF6FB5),
            PotionEffectType.REGENERATION, 5, 2, true, "Concede o efeito de Regeneração."),
    VIDA("vida", "Joia da Vida", TextColor.color(0xE02A2A),
            PotionEffectType.HEALTH_BOOST, 5, 3, true, "Concede o efeito de Vida Extra."),
    PULO("pulo", "Joia do Pulo", TextColor.color(0x3FD04A),
            PotionEffectType.JUMP_BOOST, 5, 5, true, "Concede um super pulo."),
    TITA("tita", "Joia do Titã", TextColor.color(0x8B5A2B),
            null, 20, 1, false, "Você fica gigante e com vida extra."),
    LADRAO("ladrao", "Joia do Ladrão", TextColor.color(0x8C8C94),
            PotionEffectType.INVISIBILITY, 45, 1, false, "Você fica invisível, sem partículas."),
    LEVITACAO("levitacao", "Joia da Levitação", TextColor.color(0xA84CFF),
            PotionEffectType.LEVITATION, 2, 35, true, "Você flutua para o alto por instantes."),
    REVELACAO("revelacao", "Joia da Revelação", TextColor.color(0xFFE01A),
            null, 60, 1, false, "Revela quem está por perto com brilho.");

    private final String id;
    private final String displayName;
    private final TextColor color;
    private final PotionEffectType effect;
    private final int defaultSeconds;
    private final int defaultLevel;
    private final boolean particles;
    private final String description;

    Jewel(String id, String displayName, TextColor color, PotionEffectType effect,
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
    public TextColor color() { return color; }
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
