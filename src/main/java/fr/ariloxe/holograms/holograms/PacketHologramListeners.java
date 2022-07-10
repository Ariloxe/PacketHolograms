package fr.ariloxe.holograms.holograms;

import fr.ariloxe.holograms.injection.PacketModel;
import fr.ariloxe.holograms.injection.Reflection;
import fr.ariloxe.holograms.injection.listener.Listen;
import fr.ariloxe.holograms.injection.listener.PacketListener;
import fr.ariloxe.holograms.injection.manager.PacketManager;
import net.minecraft.server.v1_8_R3.EntitySlime;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ariloxe
 */
public class PacketHologramListeners implements Listener, PacketListener {

    private final Class<?> packetPlayInUseEntityClazz = Reflection.getMinecraftClass("PacketPlayInUseEntity");
    private final Reflection.FieldAccessor<Integer> entityIdField = Reflection.getField(this.packetPlayInUseEntityClazz, "a", int.class);
    private final Map<UUID, Long> uuidLongMap = new HashMap<>();

    public PacketHologramListeners(){
        PacketManager.getInstance().register(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent playerJoinEvent){
        PacketHologramManager.getInstance().getHologramMap().values().stream().filter(PacketHologram::isGlobal).forEach(packetHologram -> packetHologram.show(playerJoinEvent.getPlayer()));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent entityDamageEvent){
        if(entityDamageEvent.getEntity() instanceof Slime && entityDamageEvent.getEntity().hasMetadata("hologramName"))
            entityDamageEvent.setCancelled(true);

    }

    @Listen(packet = PacketPlayInUseEntity.class)
    public void onPacket(final PacketModel packetModel) {
        PacketPlayInUseEntity packetPlayInUseEntity = ((PacketPlayInUseEntity) packetModel.getPacket());
        Player player = packetModel.getPlayer();
        if(packetPlayInUseEntity.a(((CraftWorld) player.getWorld()).getHandle()) != null)
            return;

        UUID uuid = player.getUniqueId();

        if(!uuidLongMap.containsKey(uuid)){
            uuidLongMap.putIfAbsent(uuid, System.currentTimeMillis());
        } else {
            if(System.currentTimeMillis() - uuidLongMap.get(uuid) < 50){
                return;
            } else {
                uuidLongMap.put(uuid, System.currentTimeMillis());
            }
        }

        int packetEntityId = this.entityIdField.get(packetPlayInUseEntity);
        if(!PacketHologramManager.getInstance().getIdToSlimeMap().containsKey(packetEntityId))
            return;


        EntitySlime entity = PacketHologramManager.getInstance().getIdToSlimeMap().get(packetEntityId);

        if(entity.getBukkitEntity().hasMetadata("hologramName")){
            String holoName = (entity.getBukkitEntity().getMetadata("hologramName").get(0)).asString();
            int line = Integer.parseInt(holoName.split("@")[1]);
            PacketHologram holo = PacketHologramManager.getInstance().getHologramFromName(holoName.split("@")[0]);
            if(holo.getCallback(line) != null)
                holo.getCallback(line).accept(player, holo, line);
        }
    }

}
