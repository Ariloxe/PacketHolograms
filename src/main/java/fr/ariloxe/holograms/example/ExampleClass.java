package fr.ariloxe.holograms.example;

import fr.ariloxe.holograms.holograms.PacketHologram;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Ariloxe
 */
public class ExampleClass {

    public void exampleMethod(Player target){
        PacketHologram hologram = new PacketHologram(Bukkit.getWorld("world").getSpawnLocation(), "§8- §7Hologramme", "§7• §eAriloxe", "", "§8» §eCliquez pour changer §8«");
        hologram.addCallback(4, (player, packetHologram, integer) -> {

            if(packetHologram.getLine(2).equals("§7• §eAriloxe"))
                packetHologram.setLine(2, "§7• §eBlendman974");
            else
                packetHologram.setLine(2, "§7• §eAriloxe");

            packetHologram.updateLine(2, player);
            player.playSound(player.getLocation(), Sound.WOOD_CLICK, 1, 1);
        });

        hologram.build();
        hologram.show(target);

    }

}
