package fr.ariloxe.holograms.injection.listener;

import fr.ariloxe.holograms.injection.core.PacketHandler;
import fr.ariloxe.holograms.injection.core.PacketInjector;
import net.minecraft.server.v1_8_R3.Packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a method as a Packet Listener, it means this method will be called by the {@link PacketHandler } when appropriate conditions are meets
 *
 * @see PacketHandler
 * @see PacketInjector
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Listen {

    Class<? extends Packet> packet();

}
