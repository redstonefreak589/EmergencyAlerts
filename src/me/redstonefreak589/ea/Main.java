package me.redstonefreak589.ea;

import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Main plugin;
	int task = 0;
	int secondsToDelay = 0;
	boolean firstRun = true;
	String messageToBroadcast = null;
	HashMap<String, String> msgMap = new HashMap<String,String>();
	
	@Override
	public void onEnable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion()
				+ " has been enabled!");
		PluginManager manager = getServer().getPluginManager();
		manager.registerEvents(this, this);
		parseSeconds();
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	public void parseSeconds(){
		int tempInt = getConfig().getInt("secondsToDelay");
		secondsToDelay = tempInt * 20;
	}
	
	@Override
	public void onDisable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " has been disabled!");
	}
	
	@EventHandler
	public void onChatCommand(AsyncPlayerChatEvent event){
		if(event.getPlayer().isOp() || event.getPlayer().hasPermission("ea.command")){
			final Player player = (Player) event.getPlayer();
			if(event.getMessage().equalsIgnoreCase("!start")){
				if(!(player.hasPermission("ea.start"))){ return; }
				event.setCancelled(true);
				task = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
	                public void run() {
	                    if(firstRun == true){
	                    	if(!(getConfig().getString("message").equals("") || getConfig().getString("message").equals(null))){
	                    		messageToBroadcast = getConfig().getString("message");
		                    	firstRun = false;
		                    	player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Broadcaster has been started.");
	                    	}else{
	                    		player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Message is 'null'. Try doing '!set' to set a message.");
	                    		firstRun = true;
	                    		Bukkit.getScheduler().cancelTask(task);
	                    	}
	                    }
	                    if(!(firstRun == true)){
	                    	Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + "Alert" + ChatColor.GRAY + "] " + ChatColor.RED + messageToBroadcast);
	                    }
	                }
	            }, 0, secondsToDelay);
			}else if(event.getMessage().equalsIgnoreCase("!stop")){
				if(!(player.hasPermission("ea.stop"))){ return; }
				Bukkit.getScheduler().cancelTask(task);
				task = 0;
				event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Broadcaster has been stopped.");
				event.setCancelled(true);
			}else if(event.getMessage().equalsIgnoreCase("!set")){
				if(!(player.hasPermission("ea.set"))){ return; }
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Type the new message in chat.");
				msgMap.put(event.getPlayer().getName().toString(), event.getPlayer().getName().toString());
			}else if(event.getMessage().equalsIgnoreCase("!delete")){
				if(!(player.hasPermission("ea.delete"))){ return; }
				getConfig().set("message", null);
				saveConfig();
				event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Message removed. Stopping broadcaster.");
				Bukkit.getScheduler().cancelTask(task);
				task = 0;
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		Player target = e.getPlayer();
		if(msgMap.containsKey(target.getName().toString())){
			if(!(e.getMessage().equalsIgnoreCase("!set"))){
				e.setCancelled(true);
				getConfig().set("message", e.getMessage().toString());
				saveConfig();
				e.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Alert set.");
				msgMap.remove(target.getName().toString());
			}
		}
	}
	
}
