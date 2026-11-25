package com.joias;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class JewelListener implements Listener {

    private final JoiasPlugin plugin;
    private final NamespacedKey scaleKey;
    private final NamespacedKey healthKey;
    private final Map<UUID, BukkitTask> titanTasks = new HashMap<>();
    // Quando (em milissegundos) cada joia fica pronta de novo, por jogador.
    private final Map<UUID, Map<Jewel, Long>> cooldowns = new HashMap<>();

    public JewelListener(JoiasPlugin plugin) {
        this.plugin = plugin;
        this.scaleKey = new NamespacedKey(plugin, "tita_escala");
        this.healthKey = new NamespacedKey(plugin, "tita_vida");
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() == null) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Jewel jewel = plugin.identify(event.getItem());
        if (jewel == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (!player.hasPermission("joias.usar")) {
            player.sendActionBar(Component.text("Você não pode usar joias.", NamedTextColor.RED));
            return;
        }

        long now = System.currentTimeMillis();
        Map<Jewel, Long> playerCooldowns =
                cooldowns.computeIfAbsent(player.getUniqueId(), k -> new EnumMap<>(Jewel.class));
        Long readyAt = playerCooldowns.get(jewel);
        if (readyAt != null && readyAt > now) {
            long secondsLeft = (readyAt - now + 999) / 1000;
            player.sendActionBar(Component.text(
                    jewel.displayName() + " em recarga: " + formatTime(secondsLeft), NamedTextColor.RED));
            return;
        }

        boolean used;
        if (jewel == Jewel.TITA) {
            used = applyTitan(player);
        } else if (jewel == Jewel.REVELACAO) {
            used = applyReveal(player);
        } else {
            used = applyPotion(player, jewel);
        }
        if (!used) return;

        int cooldownSeconds = plugin.getConfig().getInt("joias." + jewel.id() + ".cooldown",
                plugin.getConfig().getInt("cooldown-segundos", 90));
        if (cooldownSeconds > 0) {
            playerCooldowns.put(jewel, now + cooldownSeconds * 1000L);
        }

        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
        player.sendActionBar(Component.text(jewel.displayName() + " ativada!", jewel.color()));

        if (plugin.getConfig().getBoolean("consumir-ao-usar", false)) {
            ItemStack inHand = player.getInventory().getItem(event.getHand());
            inHand.subtract();
        }
    }

    private static String formatTime(long seconds) {
        if (seconds >= 60) {
            long m = seconds / 60;
            long r = seconds % 60;
            return r == 0 ? m + "m" : m + "m" + r + "s";
        }
        return seconds + "s";
    }

    private boolean applyPotion(Player player, Jewel jewel) {
        String path = "joias." + jewel.id();
        int seconds = plugin.getConfig().getInt(path + ".duracao", jewel.defaultSeconds());
        int level = plugin.getConfig().getInt(path + ".nivel", jewel.defaultLevel());

        // ambient = false, particles = (false para o ladrão), icon = true
        player.addPotionEffect(new PotionEffect(
                jewel.effect(), seconds * 20, Math.max(0, level - 1),
                false, jewel.particles(), true));
        return true;
    }

    private boolean applyReveal(Player player) {
        String path = "joias.revelacao";
        int seconds = plugin.getConfig().getInt(path + ".duracao", 60);
        double radius = plugin.getConfig().getDouble(path + ".raio", 100.0);
        boolean includeMobs = plugin.getConfig().getBoolean(path + ".incluir-mobs", false);
        double radiusSquared = radius * radius;

        int revealed = 0;
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
            if (entity.equals(player) || !(entity instanceof LivingEntity target)) continue;
            if (!includeMobs && !(entity instanceof Player)) continue;
            if (entity instanceof Player other && other.getGameMode() == GameMode.SPECTATOR) continue;
            if (entity.getLocation().distanceSquared(player.getLocation()) > radiusSquared) continue;

            // Brilho sem partículas e sem ícone; aparece mesmo em quem está invisível.
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, seconds * 20, 0,
                    false, false, false));
            revealed++;
        }

        if (revealed == 0) {
            player.sendActionBar(Component.text("Ninguém por perto para revelar.", NamedTextColor.RED));
            return false;
        }
        return true;
    }

    private boolean applyTitan(Player player) {
        UUID uuid = player.getUniqueId();
        if (titanTasks.containsKey(uuid)) {
            player.sendActionBar(Component.text("A Joia do Titã já está ativa.", NamedTextColor.RED));
            return false;
        }

        AttributeInstance scaleAttr = player.getAttribute(Attribute.SCALE);
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (scaleAttr == null || healthAttr == null) return false;

        int seconds = plugin.getConfig().getInt("joias.tita.duracao", 20);
        double scale = plugin.getConfig().getDouble("joias.tita.escala", 3.0);
        double extraHealth = plugin.getConfig().getDouble("joias.tita.vida-extra", 20.0);

        removeTitanModifiers(player);
        // A escala base é 1.0, então somamos (escala - 1) para chegar no valor final.
        scaleAttr.addModifier(new AttributeModifier(scaleKey, scale - 1.0,
                AttributeModifier.Operation.ADD_NUMBER));
        healthAttr.addModifier(new AttributeModifier(healthKey, extraHealth,
                AttributeModifier.Operation.ADD_NUMBER));

        // Enche os corações novos.
        player.setHealth(Math.min(healthAttr.getValue(), player.getHealth() + extraHealth));

        // Velocidade extra enquanto o Titã está ativo (0 desliga).
        int speedLevel = plugin.getConfig().getInt("joias.tita.velocidade-nivel", 3);
        if (speedLevel > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, seconds * 20,
                    speedLevel - 1, false, false, true));
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> endTitan(uuid), seconds * 20L);
        titanTasks.put(uuid, task);
        return true;
    }

    private void endTitan(UUID uuid) {
        BukkitTask task = titanTasks.remove(uuid);
        if (task != null) task.cancel();

        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        removeTitanModifiers(player);
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttr != null && player.getHealth() > healthAttr.getValue()) {
            player.setHealth(healthAttr.getValue());
        }
    }

    private void removeTitanModifiers(Player player) {
        AttributeInstance scaleAttr = player.getAttribute(Attribute.SCALE);
        AttributeInstance healthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (scaleAttr != null) scaleAttr.removeModifier(scaleKey);
        if (healthAttr != null) healthAttr.removeModifier(healthKey);
    }

    public void cleanupAll() {
        for (UUID uuid : new java.util.ArrayList<>(titanTasks.keySet())) {
            endTitan(uuid);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        endTitan(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        endTitan(event.getEntity().getUniqueId());
    }

    /** Limpa restos do Titã caso o servidor tenha caído com o efeito ativo. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        removeTitanModifiers(event.getPlayer());
    }
}
