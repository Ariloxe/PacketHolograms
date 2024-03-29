package fr.ariloxe.holograms.holograms;

import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang.BooleanUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

/**
 * @author Ariloxe
 */
public class PacketHologram {

    private final String name;
    private final List<String> hologramText;
    private final Map<Integer, EntityArmorStand> armorStandMap = new HashMap<>();
    private final Map<Integer, EntitySlime> slimeMap = new HashMap<>();
    private final Location hologramLocation;
    private int count;
    private boolean global;
    private final Map<Integer, TriConsumer<Player, PacketHologram, Integer>> integerTriConsumerMap = new HashMap<>();

    /**
     * Init the hologram
     * @param strings the differents text on the Hologram
     * @param location the location where the Hologram appears
     */
    public PacketHologram(Location location, String... strings) {
        this.hologramText = Arrays.asList(strings);
        this.hologramLocation = location.add(0, 0.40D, 0);
        this.name = "hologram_" + new Random().nextInt(9999);
    }


    /**
     * Build the hologram
     * @return the hologram
     */
    public PacketHologram build(){
        for (String text : this.hologramText) {
            this.count++;
            this.hologramLocation.subtract(0.0D, 0.40D, 0.0D);



            EntityArmorStand entity = new EntityArmorStand(((CraftWorld)this.hologramLocation.getWorld()).getHandle(), this.hologramLocation.getX(), this.hologramLocation.getY(), this.hologramLocation.getZ());
            entity.setCustomName(text);
            entity.setCustomNameVisible(true);
            entity.n(true); //SetMarket
            entity.setSmall(true);
            entity.setInvisible(true);
            entity.setGravity(false);

            armorStandMap.put(count, entity);

            if(integerTriConsumerMap.isEmpty() || !integerTriConsumerMap.containsKey(this.count))
                continue;

            EntitySlime slime = new EntitySlime(((CraftWorld)this.hologramLocation.getWorld()).getHandle());
            slime.setLocation(this.hologramLocation.getX(), this.hologramLocation.getY(), this.hologramLocation.getZ(), 0, 0);
            slime.setSize(0);
            slime.setInvisible(true);
            slime.setCustomNameVisible(false);
            slime.setCustomName(text);
            slime.getBukkitEntity().setMetadata("hologramName", new FixedMetadataValue(PacketHologramManager.getInstance().getBukkit(), this.name + "@" + this.count));
            setIA(slime, false);

            PacketHologramManager.getInstance().getIdToSlimeMap().put(slime.getId(), slime);
            slimeMap.put(count, slime);

        }

        for (int i = 0; i < this.count; i++) {
            this.hologramLocation.add(0.0D, 0.40D, 0.0D);
        }

        PacketHologramManager.getInstance().getHologramMap().put(this.name, this);

        return this;
    }

    /**
     * Permit to set a hologram state to global (every player automatically load it and see it on join)
     * @param bool if the hologram is global or no. (true = global)
     * @return the current hologram
     */
    public PacketHologram setGlobal(boolean bool){
        this.global = bool;

        return this;
    }

    /**
     * Permit to get a hologram state to global (every player automatically load it and see it on join)
     * @return the state
     */
    public boolean isGlobal(){ return this.global; }

    /**
     * Permit to show this hologram to a specific player
     * @param player the player who'll see it
     */
    public void show(Player player){
        for (EntityArmorStand entityArmorStand : this.armorStandMap.values()) {
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(entityArmorStand);
            (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(packet);
        }

        for (EntitySlime entitySlime : this.slimeMap.values()) {
            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(entitySlime);
            (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(packet);
        }
    }

    /**
     * Permit to hide this hologram to a specific player
     * @param player the player who'll hide it
     */
    public void hide(Player player) {
        for (EntityArmorStand entityArmorStand : this.armorStandMap.values()) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityArmorStand.getId());
            (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(packet);
        }

        for (EntitySlime entitySlime : this.slimeMap.values()) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entitySlime.getId());
            (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(packet);
        }
    }

    /**
     * Permit to update a specific line, just for one player
     * @param line the line's number you want to update.
     * @param player the player who'll change it.
     */
    public void updateLine(int line, Player player){
        EntityArmorStand entityArmorStand = armorStandMap.get(line);

        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(metadata);
    }

    /**
     * Permit to update a specific line for every players
     * @param line the line's number you want to update.
     */
    public void updateLine(int line){
        EntityArmorStand entityArmorStand = armorStandMap.get(line);

        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityArmorStand.getId(), entityArmorStand.getDataWatcher(), true);
        Bukkit.getOnlinePlayers().forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(metadata));
    }

    /**
     * Permit to rebuild the entiere hologram (destroy entities and spawn newers), just for one player
     * @param player the player to send the modifications
     */
    public void updateHologram(Player player) {
        hide(player);
        show(player);
    }

    /**
     * Permit to rebuild the entiere hologram (destroy entities and spawn newers), for every players
     */
    public void updateHologram() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
            show(player);
        }
    }

    /**
     * Permit to get the name you specified on the armorstand init process.
     * @return the hologram's name
     */
    public String getName() {
        return name;
    }


    /**
     * Get the callback of this hologram, if null there isn't callback
     * @param line the line you want to get the callback.
     * @return the {@link TriConsumer} for a specific line
     */
    public TriConsumer<Player, PacketHologram, Integer> getCallback(int line) {
        return this.integerTriConsumerMap.get(line);
    }

    /**
     * Add callback to hologram
     * @param line the line you want to add a callback
     * @param playerConsumer consumer
     *                       player = the player who rightclick on it
     *                       hologram = the hologram who is clicked (this)
     *                       integer = the line's number (from the top)
     */
    public void addCallback(int line, TriConsumer<Player, PacketHologram, Integer> playerConsumer) {
        this.integerTriConsumerMap.put(line, playerConsumer);
    }

    /**
     * Get the ArmorStand of a specific line
     * @param numberLine the line that you want to get the armorstand
     * @return the entity armorstand (nms) of this line.
     */
    public EntityArmorStand getArmorStand(int numberLine){
        return this.armorStandMap.get(numberLine);
    }

    /**
     * Change a line's value
     * @param numberLine the line that you want to change it
     * @param text the new text of this line
     */
    public void setLine(int numberLine, String text){
        this.armorStandMap.get(numberLine).setCustomName(text);
    }

    /**
     * Get a line's value
     * @param numberLine the line that you want to recuip it.
     * @return the text on this specific line.
     */
    public String getLine(int numberLine){
        return this.armorStandMap.get(numberLine).getCustomName();
    }

    private void setIA(EntitySlime bukkitEntity, boolean bool) {
        Entity nmsEntity = (bukkitEntity.getBukkitEntity()).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag();
        if (tag == null)
            tag = new NBTTagCompound();

        nmsEntity.c(tag);

        tag.setInt("NoAI", BooleanUtils.toInteger(!bool));
        nmsEntity.f(tag);
    }
}
