package com.joias;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class JoiasPlugin extends JavaPlugin {

    private NamespacedKey jewelKey;
    private JewelListener listener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        jewelKey = new NamespacedKey(this, "joia");

        listener = new JewelListener(this);
        getServer().getPluginManager().registerEvents(listener, this);

        JewelCommand command = new JewelCommand(this);
        PluginCommand pluginCommand = getCommand("joia");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    @Override
    public void onDisable() {
        if (listener != null) {
            listener.cleanupAll();
        }
    }

    /** Cria o item da joia (fragmento de ametista com nome, lore e marca interna). */
    public ItemStack createJewel(Jewel jewel, int amount) {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD, amount);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(jewel.displayName(), jewel.color())
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
                Component.text(jewel.description(), NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Clique com o botão direito para usar.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)));
        meta.setItemModel(new NamespacedKey(this, jewel.id()));
        meta.getPersistentDataContainer().set(jewelKey, PersistentDataType.STRING, jewel.id());

        item.setItemMeta(meta);
        return item;
    }

    /** Retorna a joia representada pelo item, ou null se não for uma joia. */
    public Jewel identify(ItemStack item) {
        if (item == null || item.getType() != Material.AMETHYST_SHARD || !item.hasItemMeta()) {
            return null;
        }
        String id = item.getItemMeta().getPersistentDataContainer()
                .get(jewelKey, PersistentDataType.STRING);
        return Jewel.fromId(id);
    }
}
