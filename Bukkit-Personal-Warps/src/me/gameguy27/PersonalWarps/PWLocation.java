package me.gameguy27.PersonalWarps;

import java.io.Serializable;

import org.bukkit.Location;

public class PWLocation implements Serializable {
	
	private static final long serialVersionUID = 1409675110156483037L;
	public double x;
	public double y;
	public double z;
	public float yaw;
	public float pitch;
	public String world;
	
	public PWLocation (Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		yaw = location.getYaw();
		pitch = location.getPitch();
		world = location.getWorld().getName();
	}
}
