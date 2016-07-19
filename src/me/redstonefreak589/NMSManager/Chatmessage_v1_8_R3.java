package me.redstonefreak589.NMSManager;

import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Chatmessage_v1_8_R3 implements Chatmessage{

	@Override
	public void sendAlert(String message) {
		
		PacketPlayOutChat bar = new PacketPlayOutChat(ChatSerializer.a("[\"\",{\"text\":\"[\",\"color\":\"gray\"},{\"text\":\"Alert\",\"color\":\"red\"},{\"text\":\"] " + message +  " \",\"color\":\"gray\"}]"), (byte)0);
		
		for(Player p : Bukkit.getOnlinePlayers()){
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(bar);
		}
	}

}
