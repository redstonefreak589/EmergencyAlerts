package me.redstonefreak589.NMSManager;

import net.minecraft.server.v1_9_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_9_R2.PacketPlayOutChat;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Chatmessage_v1_9_R2 implements Chatmessage{

	@Override
	public void sendAlert(String message) {
		
		PacketPlayOutChat bar = new PacketPlayOutChat(ChatSerializer.a("[\"\",{\"text\":\"[\",\"color\":\"gray\"},{\"text\":\"Alert\",\"color\":\"red\"},{\"text\":\"] " + message +  " \",\"color\":\"gray\"}]"), (byte)0);
		
		for(Player p : Bukkit.getOnlinePlayers()){
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(bar);
		}
		
	}

}
