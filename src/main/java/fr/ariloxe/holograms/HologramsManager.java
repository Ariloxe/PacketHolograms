package fr.ariloxe.holograms;

import fr.ariloxe.holograms.holograms.PacketHologramListeners;
import fr.ariloxe.holograms.holograms.PacketHologramManager;
import fr.ariloxe.holograms.injection.core.PacketInjector;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Ariloxe
 */
public class HologramsManager {

    public static void init(JavaPlugin javaPlugin){
        javaPlugin.getServer().getPluginManager().registerEvents(new PacketInjector(), javaPlugin);
        PacketHologramManager.getInstance().init(javaPlugin);
        new PacketHologramListeners();
    }

}
