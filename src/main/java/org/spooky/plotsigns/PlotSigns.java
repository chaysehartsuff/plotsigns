package org.spooky.plotsigns;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.spooky.plotsigns.commands.Plot;
import org.spooky.plotsigns.commands.PlotScan;
import org.spooky.plotsigns.storage.JsonUtil;
import org.spooky.plotsigns.storage.SignPlot;

import java.util.ArrayList;
import java.util.List;


public final class PlotSigns extends JavaPlugin {

    List<SignPlot> signPlots;

    @Override
    public void onEnable() {

        World world = this.getServer().getWorld("world");

        loadJSONData();

        // initiate empty signPlots if null
        if (this.signPlots == null)
            this.signPlots = new ArrayList<>();

        this.getCommand("plotscan").setExecutor(new PlotScan(world));
        this.getCommand("plot").setExecutor(new Plot(world, signPlots));

        // Registering the event listener
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new SignInteractionListener(this.signPlots, world), this);
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

    private void loadJSONData(){

        this.signPlots = JsonUtil.readFromJsonFile("./spooky/plotsigns/signplots.json", new TypeReference<List<SignPlot>>(){});

    }
}
