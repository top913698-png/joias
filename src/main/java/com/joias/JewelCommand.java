package com.joias;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class JewelCommand implements CommandExecutor, TabCompleter {

    private final JoiasPlugin plugin;

    public JewelCommand(JoiasPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("joias.admin")) {
            sender.sendMessage(Component.text("Sem permissão.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("lista")) {
            sender.sendMessage(Component.text("Joias disponíveis:", NamedTextColor.YELLOW));
            for (Jewel jewel : Jewel.values()) {
                sender.sendMessage(Component.text(" - " + jewel.id() + " (" + jewel.displayName() + ")",
                        jewel.color()));
            }
            sender.sendMessage(Component.text("Uso: /joia dar <jogador> <joia> [quantidade]",
                    NamedTextColor.GRAY));
            return true;
        }

        if (args[0].equalsIgnoreCase("dar")) {
            if (args.length < 3) {
                sender.sendMessage(Component.text("Uso: /joia dar <jogador> <joia> [quantidade]",
                        NamedTextColor.RED));
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Jogador não encontrado.", NamedTextColor.RED));
                return true;
            }
            Jewel jewel = Jewel.fromId(args[2]);
            if (jewel == null) {
                sender.sendMessage(Component.text("Joia inválida. Use /joia lista.", NamedTextColor.RED));
                return true;
            }
            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Math.max(1, Math.min(64, Integer.parseInt(args[3])));
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Quantidade inválida.", NamedTextColor.RED));
                    return true;
                }
            }
            target.getInventory().addItem(plugin.createJewel(jewel, amount))
                    .values().forEach(left -> target.getWorld().dropItemNaturally(target.getLocation(), left));
            sender.sendMessage(Component.text("Você deu " + amount + "x " + jewel.displayName()
                    + " para " + target.getName() + ".", NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text("Uso: /joia <dar|lista>", NamedTextColor.RED));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission("joias.admin")) return out;

        if (args.length == 1) {
            out.add("dar");
            out.add("lista");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("dar")) {
            Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("dar")) {
            for (Jewel j : Jewel.values()) out.add(j.id());
        }
        String last = args[args.length - 1].toLowerCase();
        out.removeIf(s -> !s.toLowerCase().startsWith(last));
        return out;
    }
}
