package org.spooky.plotsigns;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import java.util.Set;


public final class PlotSigns extends JavaPlugin {

    @Override
    public void onEnable() {
        /*WorldGuardPlugin wgPlugin = getWorldGuard();
        if (wgPlugin == null) {
            getLogger().warning("WorldGuard not found, disabling plugin!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Obtain the world
        World world = this.getServer().getWorlds().get(0); // Default to the first world

        // Get the region manager for the world
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions != null) {
            // Get all region IDs in the world
            Set<String> allRegions = regions.getRegions().keySet();
            //regions.getRegions().get(0).getMaximumPoint()
            getLogger().info("Regions in the world: " + allRegions.toString());
        } else {
            getLogger().info("No regions found or WorldGuard not enabled for this world.");
        }*/
        this.getCommand("plotscan").setExecutor(new PlotScan(this.getServer().getWorld("world")));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        // WorldGuard may not be loaded
        if (plugin instanceof WorldGuardPlugin) {
            return null;
        }
        return (WorldGuardPlugin) plugin;
    }
}
