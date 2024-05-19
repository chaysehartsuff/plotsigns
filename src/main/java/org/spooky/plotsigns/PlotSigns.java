package org.spooky.plotsigns;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.spooky.plotsigns.commands.Plot;
import org.spooky.plotsigns.commands.PlotScan;
import org.spooky.plotsigns.objects.GracePeriodObject;
import org.spooky.plotsigns.objects.SignTimedObject;
import org.spooky.plotsigns.storage.JsonUtil;
import org.spooky.plotsigns.storage.SignPlot;

import java.util.ArrayList;
import java.util.List;


public final class PlotSigns extends JavaPlugin {

    List<SignPlot> signPlots;
    List<SignTimedObject> sto;
    List<GracePeriodObject> gpo;

    @Override
    public void onEnable() {
        World world = this.getServer().getWorld("world");

        loadJSONData();
        setupJobs();

        // initiate empty signPlots if null
        if (this.signPlots == null)
            this.signPlots = new ArrayList<>();
        if (this.sto == null)
            this.sto = new ArrayList<>();
        if (this.gpo == null)
            this.gpo = new ArrayList<>();

        this.getCommand("plotscan").setExecutor(new PlotScan(world));
        this.getCommand("plot").setExecutor(new Plot(world, signPlots));

        // Registering the event listener
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new SignInteractionListener(this.signPlots, world, sto, gpo), this);
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

    private void setupJobs(){
        new BukkitRunnable() {
            @Override
            public void run() {
                // clear all sto entries 15 seconds or older
                cleanSTO(5);
                cleanGPO(5);
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    private void cleanSTO(int clearTime){
        ArrayList<Integer> delete = new ArrayList<>();
        for(int i = 0; i < sto.size(); i++){
            SignTimedObject _sto = sto.get(i);
            if(_sto.SecondsPassed() >= clearTime) {
                delete.add(i);
            }
        }
        for(int d : delete)
            sto.remove(d);
    }

    private void cleanGPO(int clearTime){
        ArrayList<Integer> delete = new ArrayList<>();
        for(int i = 0; i < gpo.size(); i++){
            GracePeriodObject _gpo = gpo.get(i);
            if(_gpo.SecondsPassed() >= clearTime) {
                delete.add(i);
                //Bukkit.broadcastMessage("graceperiod " + i + " removed");
            }
        }
        for(int d : delete)
            gpo.remove(d);
    }
}
