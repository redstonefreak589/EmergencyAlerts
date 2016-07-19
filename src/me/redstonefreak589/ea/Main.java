package me.redstonefreak589.ea;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import me.redstonefreak589.NMSManager.Chatmessage;
import me.redstonefreak589.NMSManager.Chatmessage_v1_10_R1;
import me.redstonefreak589.NMSManager.Chatmessage_v1_8_R1;
import me.redstonefreak589.NMSManager.Chatmessage_v1_8_R2;
import me.redstonefreak589.NMSManager.Chatmessage_v1_8_R3;
import me.redstonefreak589.NMSManager.Chatmessage_v1_9_R1;
import me.redstonefreak589.NMSManager.Chatmessage_v1_9_R2;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public static Main plugin;
	int secondsToDelay = 0;
	boolean running = false;
	boolean mute = false;
	private Chatmessage chatmessage;
	
	File configFile;
	FileConfiguration config;
	BukkitTask runnable;
	
	String playerWhoMuted = null;
	String messageToBroadcast = null;
	HashMap<String, String> msgMap = new HashMap<String,String>();
	
	@Override
	public void onEnable(){
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion()
				+ " has been enabled!");
		PluginManager manager = getServer().getPluginManager();
		manager.registerEvents(this, this);
		
		configFile = new File(getDataFolder(), "config.yml");
		
		try {
			firstRun();
		} catch (Exception e) {
			e.printStackTrace();
		}

		config = new YamlConfiguration();
		loadYamls();
		
		config.addDefault("message", "");
		config.addDefault("secondsToDelay", 30);
		config.options().copyDefaults(true);
		saveYamls();
		
		parseSeconds();
		setupChatPacket();
	}
	
	private boolean setupChatPacket(){
		String version;
		
		try {
			version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		} catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException){
			return false;
		}
		getLogger().info("Your server is running version " + version);
		
		if (version.equals("v1_8_R1")){
			
			chatmessage = new Chatmessage_v1_8_R1();
			
		} else if (version.equals("v1_8_R2")){
			
			chatmessage = new Chatmessage_v1_8_R2();
			
		} else if (version.equals("v1_8_R3")){
		
			chatmessage = new Chatmessage_v1_8_R3();
			
		} else if(version.equals("v1_9_R1")){
			
			chatmessage = new Chatmessage_v1_9_R1();
			
		} else if(version.equals("v1_9_R2")){
			
			chatmessage = new Chatmessage_v1_9_R2();
			
		} else if(version.equals("v1_10_R1")){
			
			chatmessage = new Chatmessage_v1_10_R1();
			
		}
		
		return chatmessage != null;
	}
	
	public void saveYamls(){
		try {
			config.save(configFile);
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public void loadYamls(){
		try {
			config.load(configFile);
			parseSeconds();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void firstRun() throws Exception {
		if(!configFile.exists()){
			configFile.getParentFile().mkdirs();
			//saveYamls();
			copy(getResource("config.yml"), configFile);
		}
	}
	
	private void copy(InputStream in, File file) {
	    try {
	        OutputStream out = new FileOutputStream(file);
	        byte[] buf = new byte[1024];
	        int len;
	        while((len=in.read(buf))>0){
	            out.write(buf,0,len);
	        }
	        out.close();
	        in.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public void parseSeconds(){
		int tempInt = config.getInt("secondsToDelay");
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
				if(running){ return; }
				event.setCancelled(true);
				
				if(!(config.getString("message").equals("") || config.getString("message") == null)){
					running = true;
					messageToBroadcast = config.getString("message");
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Broadcaster has been started.");
					runnable = new BukkitRunnable(){

						@Override
						public void run() {
		                    	chatmessage.sendAlert(messageToBroadcast);
						}

					}.runTaskTimer(this, 0, secondsToDelay);
					
				}else{
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Message is 'null'. Try doing '!set' to set a message.");
					if(running){
						runnable.cancel();
					}
					running = false;
					event.setCancelled(true);
				}
				
				
			}else if(event.getMessage().equalsIgnoreCase("!stop")){
				if(!(player.hasPermission("ea.stop"))){ return; }
				if(running){
					runnable.cancel();
				}
				running = false;
				event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Broadcaster has been stopped.");
				event.setCancelled(true);
			}else if(event.getMessage().equalsIgnoreCase("!set")){
				if(!(player.hasPermission("ea.set"))){ return; }
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Type the new message in chat.");
				msgMap.put(event.getPlayer().getName().toString(), event.getPlayer().getName().toString());
			}else if(event.getMessage().equalsIgnoreCase("!delete")){
				if(!(player.hasPermission("ea.delete"))){ return; }
				config.set("message", "");
				saveYamls();
				event.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Message removed. Stopping any running broadcasters.");
				if(running){
					runnable.cancel();
				}
				running = false;
				event.setCancelled(true);
			}else if(event.getMessage().equalsIgnoreCase("!mute")){
				event.setCancelled(true);
				if(!(player.hasPermission("ea.mute"))){return;}
				if(mute == true){
					event.setCancelled(true);
					mute = false;
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Mute toggled off.");
					Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.RED + "Player " + ChatColor.GOLD + playerWhoMuted + ChatColor.RED + " has ended the mute. You may all now start chatting.");
					playerWhoMuted = null;
				}else if(mute == false){
					event.setCancelled(true);
					mute = true;
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Mute toggled on. Only OP's can chat until toggled off.");
					playerWhoMuted = player.getName().toString();
					Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.RED + "Player " + ChatColor.GOLD + playerWhoMuted + ChatColor.RED + " has initiated a server-wide mute. You will NOT be able to chat until this is turned off. You may still use commands.");
				}else{
					event.setCancelled(true);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.RED + "Sorry, I was unable to process your command. Doing !eareload should fix the problem, but will turn off the broadcaster.");
				}
			}else if(event.getMessage().equalsIgnoreCase("!eareload")){
				if(!(player.hasPermission("ea.reload"))){return;}
				event.setCancelled(true);
				mute = false;
				if(running){
					runnable.cancel();
				}
				running = false;
				loadYamls();
				player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Plugin has been reset. Please use !start if you were running a broadcast, as it has now stopped.");
			}
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		Player target = e.getPlayer();
		if(msgMap.containsKey(target.getName().toString())){
			if(!(e.getMessage().equalsIgnoreCase("!set"))){
				e.setCancelled(true);
				if(running){
					runnable.cancel();
				}
				running = false;
				messageToBroadcast = config.getString("message");
				config.set("message", e.getMessage());
				saveYamls();
				e.getPlayer().sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.GREEN + "Alert set. Please restart the broadcaster using !start, as it is now stopped.");
				msgMap.remove(target.getName().toString());
			}
		}
	}
	
	@EventHandler
	public void onMuted(AsyncPlayerChatEvent e){
		Player player = e.getPlayer();
		if(mute == true){
			if(!(player.isOp() || player.hasPermission("ea.cantalkwhilemuted"))){
				if(!(e.getMessage().startsWith("/") || e.getMessage().startsWith("!"))){
					if(e.getMessage().startsWith("/msg") || e.getMessage().startsWith("/me") || e.getMessage().startsWith("/tell") || e.getMessage().startsWith("/reply") || e.getMessage().startsWith("/m") || e.getMessage().startsWith("/t") || e.getMessage().startsWith("/r") || e.getMessage().startsWith("/sh") || e.getMessage().startsWith("/shout")){
						player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.RED + "Even though you can use commands, you are still not aloud to chat, including chatting using commands.");
						e.setCancelled(true);
						return;
					}
					e.setCancelled(true);
					player.sendMessage(ChatColor.GRAY + "[" + ChatColor.RED + "AlertPlugin" + ChatColor.GRAY + "] " + ChatColor.RED + "We're sorry, but player " + ChatColor.GOLD + playerWhoMuted + ChatColor.RED + " has activated a server-wide mute. Until this is disabled, you can not chat.");
				}
			}
		}
	}
	
}
