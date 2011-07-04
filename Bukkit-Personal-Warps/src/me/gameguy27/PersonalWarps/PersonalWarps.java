package me.gameguy27.PersonalWarps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PersonalWarps extends JavaPlugin {
	
	Logger logger = Logger.getLogger("Minecraft");
	static String directory = "plugins/Personal Warps";
	static File config_source = new File(directory + File.separator + "config.properties");
	static File permissions_source = new File(directory + File.separator + "permissions.yml");
	static Properties config = new Properties();
	static Configuration permissions;
	int warp_count = 5;
	int ops_warp_count = 10;
	String warp_invalid = "Warp {IND} is invalid.";
	String warp_set = "Warp {IND} set.";
	String warp_went = "Teleported to warp {IND}.";
	String warp_not_exist = "Warp {IND} does not exist.";
	String version;
	static Map<String, Map<Integer, PWLocation>> warps = new HashMap<String, Map<Integer, PWLocation>>();
	public static PermissionHandler permissionHandler;
	
	public void onEnable() {
		setupPermissions();
		PluginDescriptionFile pdf = this.getDescription();
		version = pdf.getVersion();
		logger.info("[Personal Warps] " + version + " enabled!");
		new File(directory).mkdir();
		if (config_source.exists()) {
			logger.info("[Personal Warps] " + version + " loaded config file.");
			loadConfig();
		} else {
			try {
				logger.info("[Personal Warps] " + version + " created config file.");
				config_source.createNewFile();
				FileOutputStream out = new FileOutputStream(config_source);
				config.put("Warps", "5");
				config.put("Ops-Warps", "10");
				config.put("Warp-Invalid-Message", "Warp {IND} is invalid.");
				config.put("Warp-Set-Message", "Warp {IND} set.");
				config.put("Warp-Goto-Message", "Teleported to warp {IND}.");
				config.put("Warp-Not-Exist", "Warp {IND} does not exist.");
				config.store(out, "Generated config");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (permissions_source.exists()) {
			permissions = new Configuration(permissions_source);
			permissions.load();
		} else {
			try {
				permissions_source.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setupPermissions() {
		if (permissionHandler != null)
			return;
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
		    
		if (permissionsPlugin == null) {
		    logger.info("[Personal Warps] Permissions not detected.");
		    return;
		}
		    
		permissionHandler = ((Permissions) permissionsPlugin).getHandler();
		logger.info("[Personal Warps] found "+((Permissions)permissionsPlugin).getDescription().getFullName());
	}
	
	@SuppressWarnings("unchecked")
	public void loadConfig() {
		try {
			FileInputStream in = new FileInputStream(config_source);
			config.load(in);
			warp_count = Integer.parseInt(config.getProperty("Warps"));
			ops_warp_count = Integer.parseInt(config.getProperty("Ops-Warps"));
			warp_invalid = config.getProperty("Warp-Invalid-Message").toString();
			warp_set = config.getProperty("Warp-Set-Message").toString();
			warp_went = config.getProperty("Warp-Goto-Message").toString();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			File file = new File(directory + File.separator + "warps.dat");
			if (file.exists()) {
				warps = (HashMap<String, Map<Integer, PWLocation>>) load(directory + File.separator + "warps.dat");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void onDisable() {
		logger.info("Personal Warps disabled!");
	}
	
	public void setWarp(CommandSender sender, int index) {
		Player player = (Player) sender;
		Map<Integer, PWLocation> player_warps = new HashMap<Integer, PWLocation>();
		if (warps.get(player.getName()) != null) {
			player_warps = warps.get(player.getName());
		}
		player_warps.put(index, new PWLocation(player.getLocation()));
		warps.put(player.getName(), player_warps);
		try {
			save(warps, directory + File.separator + "warps.dat");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void gotoWarp(CommandSender sender, int index) {
		String went = warp_went.replace("{IND}", Integer.toString(index));
		String not_exist = warp_not_exist.replace("{IND}", Integer.toString(index));
		Player player = (Player) sender;
		Map<Integer, PWLocation> player_warps = new HashMap<Integer, PWLocation>();
		if (warps.get(player.getName()) != null) {
			player_warps = warps.get(player.getName());
		}
		if (player_warps.get(index) != null) {
			PWLocation old = player_warps.get(index);
			Location location = new Location(this.getServer().getWorld(old.world), old.x, old.y, old.z, old.yaw, old.pitch);
			player.teleport(location);
			sender.sendMessage(went);
		} else {
			sender.sendMessage(not_exist);
		}
	}
	
	public static void save(Object obj,String path) throws Exception
	{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
		oos.writeObject(obj);
		oos.flush();
		oos.close();
	}
	
	public static Object load(String path) throws Exception
	{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
		Object result = ois.readObject();
		ois.close();
		return result;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("setpwarp")) {
			String invalid = warp_invalid.replace("{IND}", args[0]);
			String set = warp_set.replace("{IND}", args[0]);
			if (permissionHandler != null && !permissionHandler.has((Player) sender, "personal.warps.use"))
			    return true;
			Player player = (Player) sender;
			try {
				int index = Integer.parseInt(args[0]);
				int max_index = sender.isOp() ? ops_warp_count : warp_count;
				max_index = permissions.getInt("users." + player.getDisplayName(), max_index);
				if (permissionHandler != null) {
					@SuppressWarnings("deprecation")
					String group = permissionHandler.getGroup(player.getWorld().getName(), player.getDisplayName());
					max_index = permissions.getInt("groups." + group, max_index);
				}
				if (index >= max_index || index < 0) {
					sender.sendMessage(invalid);
				} else {
					sender.sendMessage(set);
					setWarp(sender, index);
				}
			} catch (NumberFormatException e) {
				sender.sendMessage(invalid);
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("pwarp")) {
			String invalid = warp_invalid.replace("{IND}", args[0]);
			if (permissionHandler != null && !permissionHandler.has((Player) sender, "personal.warps.use"))
			    return true;
			try {
				int index = Integer.parseInt(args[0]);
				gotoWarp(sender,  index);
			} catch (NumberFormatException e) {
				sender.sendMessage(invalid);
			}
			return true;
		}
		return false;
	}
}
