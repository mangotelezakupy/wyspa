/*
 * WYSPAAC for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2021 client
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.client.WYSPAAC;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.client.WYSPAAC.command.CommandHandler;
import com.client.WYSPAAC.config.Configuration;
import com.client.WYSPAAC.event.BlockListener;
import com.client.WYSPAAC.event.EntityListener;
import com.client.WYSPAAC.event.InventoryListener;
import com.client.WYSPAAC.event.PlayerListener;
import com.client.WYSPAAC.event.VehicleListener;
import com.client.WYSPAAC.manage.AntiCheatManager;
import com.client.WYSPAAC.metrics.Metrics;
import com.client.WYSPAAC.util.PacketListener;
import com.client.WYSPAAC.util.UpdateManager;
import com.client.WYSPAAC.util.User;
import com.client.WYSPAAC.util.VersionUtil;

public class WYSPAAC extends JavaPlugin {

	public static final List<UUID> MUTE_ENABLED_MODS = new ArrayList<UUID>();
	
	private static AntiCheatManager manager;
	private static WYSPAAC plugin;
	private static List<Listener> eventList = new ArrayList<Listener>();
	private static Configuration config;
	private static boolean verbose;
	private static ProtocolManager protocolManager;
	private static SecureRandom random;
	private static Long loadTime;
	private static UpdateManager updateManager;
	
	private double tps = -1;
	private String symbiosisMetric = "None";

	@Override
	public void onEnable() {
		plugin = this;
		random = new SecureRandom();
		loadTime = System.currentTimeMillis();
		manager = new AntiCheatManager(this, getLogger());
		eventList.add(new PlayerListener());
		eventList.add(new BlockListener());
		eventList.add(new EntityListener());
		eventList.add(new VehicleListener());
		eventList.add(new InventoryListener());
		this.setupConfig();
		this.setupEvents();
		this.setupCommands();
		this.setupEnterprise();
		this.restoreLevels();
		
		if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
			this.setupProtocol();
		} else {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "WYSPAAC " + ChatColor.DARK_GRAY + "> " + ChatColor.RED
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		Bukkit.getConsoleSender()
				.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "WYSPAAC " + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY

		PacketListener.listenMovementPackets();
		PacketListener.listenKeepAlivePackets();
		PacketListener.listenUseItemPackets();
		
		updateManager = new UpdateManager();
		
		getServer().getScheduler().runTaskLater(this, new Runnable() {
			@Override
			public void run() {
				checkForSymbiosis();
				try {
					Metrics metrics = new Metrics(WYSPAAC.this, 202);
					metrics.addCustomChart(new Metrics.SingleLineChart("cheater_wyrzucono", new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {
							int wyrzucono = playerswyrzucono;
							playerswyrzucono = 0;
							return wyrzucono;
						}
					}));
					metrics.addCustomChart(new Metrics.SimplePie("protocollib_version", new Callable<String>() {
						@Override
						public String call() throws Exception {
							return Bukkit.getPluginManager().getPlugin("ProtocolLib").getDescription().getVersion();
						}
					}));
					metrics.addCustomChart(new Metrics.SimplePie("nms_version", new Callable<String>() {
						@Override
						public String call() throws Exception {
							return VersionUtil.getVersion();
						}
					}));
					metrics.addCustomChart(new Metrics.SimplePie("symbiosis", new Callable<String>() {
						@Override
						public String call() throws Exception {
							return symbiosisMetric;
						}
					}));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 90L);
		
		new BukkitRunnable() {
            long sec;
            long currentSec;
            int ticks;

            public void run() {
                sec = (System.currentTimeMillis() / 1000L);
                if (currentSec == sec) {
                    ticks += 1;
                } else {
                    currentSec = sec;
                    tps = (tps == 0.0D ? ticks : (tps + ticks) / 2.0D);
                    ticks = 1;
                }
                
                if (ticks % 864000 == 0)
                	updateManager.update();
            }
        }.runTaskTimer(this, 40L, 1L);
	}

	private void setupEnterprise() {
		if (config.getConfig().enterprise.getValue()) {
			if (config.getEnterprise().loggingEnabled.getValue()) {
				config.getEnterprise().database.cleanEvents();
			}
		}
	}

	private void restoreLevels() {
		for (Player player : getServer().getOnlinePlayers()) {
			UUID uuid = player.getUniqueId();

			User user = new User(uuid);
			user.setIsWaitingOnLevelSync(true);
			config.getLevels().loadLevelToUser(user);

			manager.getUserManager().addUser(user);
			verboseLog("Data for " + uuid + " loaded");
		}
	}

	public static WYSPAAC getPlugin() {
		return plugin;
	}

	public static AntiCheatManager getManager() {
		return manager;
	}

	public static SecureRandom getRandom() {
		return random;
	}
	
	public static String getVersion() {
		return manager.getPlugin().getDescription().getVersion();
	}

	private void cleanup() {
		eventList = null;
		manager = null;
		config = null;
	}
	
	public static void debugLog(final String string) {
		Bukkit.getScheduler().runTask(getPlugin(), new Runnable() {
			public void run() {
				if (getManager().getConfiguration().getConfig().debugMode.getValue()) {
					manager.debugLog("[DEBUG] " + string);
				}
			}
		});
	}

	public void verboseLog(final String string) {
		if (verbose) {
			getLogger().info(string);
		}
	}

	public void setVerbose(boolean b) {
		verbose = b;
	}

	public Long getLoadTime() {
		return loadTime;
	}

	public static ProtocolManager getProtocolManager() {
		return protocolManager;
	}

	private int playerswyrzucono = 0;

	public void onPlayerwyrzucono() {
		this.playerswyrzucono++;
	}

	protected void checkForSymbiosis() {
		if (Bukkit.getPluginManager().getPlugin("NoCheatPlus") != null) {
			if (this.symbiosisMetric.equals("None")) {
				this.symbiosisMetric = "NoCheatPlus";
			} else {
				this.symbiosisMetric += ", NoCheatPlus";
			}
		}
		if (Bukkit.getPluginManager().getPlugin("WyspWyspaAC") != null) {
			if (this.symbiosisMetric.equals("None")) {
				this.symbiosisMetric = "WyspWyspaAC";
			} else {
				this.symbiosisMetric += ", WyspWyspaAC";
			}
		}
		if (Bukkit.getPluginManager().getPlugin("WyspaAC") != null) {
			if (this.symbiosisMetric.equals("None")) {
				this.symbiosisMetric = "WyspaAC";
			} else {
				this.symbiosisMetric += ", WyspaAC";
			}
		}
		if (Bukkit.getPluginManager().getPlugin("WyspWyspaAC") != null) {
			if (this.symbiosisMetric.equals("None")) {
				this.symbiosisMetric = "WyspWyspaAC";
			} else {
				this.symbiosisMetric += ", WyspWyspaAC";
			}
		}
		if (Bukkit.getPluginManager().getPlugin("Negativity") != null) {
			if (this.symbiosisMetric.equals("None")) {
				this.symbiosisMetric = "Negativity";
			} else {
				this.symbiosisMetric += ", Negativity";
			}
		}
	}

	public static void sendToMainThread(Runnable runnable) {
		Bukkit.getScheduler().runTask(WYSPAAC.getPlugin(), runnable);
	}
	
	public void sendToStaff(String message) {
		Bukkit.getOnlinePlayers().forEach(player -> {
			if (player.hasPermission("anticheat.system.alert")) {
				if (!MUTE_ENABLED_MODS.contains(player.getUniqueId())) {
					player.sendMessage(message);
				}
			}
		});
	}
	
	public static UpdateManager getUpdateManager() {
		return updateManager;
	}
	
	public double getTPS() {
		if (this.tps < 0 || this.tps > 20) {
			return 20;
		}
		return this.tps;
	}
	
}