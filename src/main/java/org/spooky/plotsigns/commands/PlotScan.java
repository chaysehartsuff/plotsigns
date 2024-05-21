package org.spooky.plotsigns.commands;

import java.io.File;
import java.util.*;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.World;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.spooky.plotsigns.storage.JsonUtil;
import org.spooky.plotsigns.storage.SignPlot;

public class PlotScan implements CommandExecutor {

    private World world;
    private File pluginFile;
    private int plotAmount;

    public PlotScan(World world, File pluginFile, int plotAmount){
        this.pluginFile = pluginFile;
        this.world = world;
        this.plotAmount = plotAmount;
    }

    private List<String> getRegionNames() {

        ArrayList<String> regionNames = new ArrayList<String>();
        for(int i = 1; i < this.plotAmount; i++){
            regionNames.add("plot".concat(Integer.toString(i)));
        }
        return regionNames;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        World world = commandSender.getServer().getWorld("world"); // Replace "world_name" with your actual world name
        RegionManager regionManager = getRegionManager(world);

        if (regionManager == null) {
            commandSender.sendMessage("Region manager not found, cannot proceed.");
            return true;
        }

        List<String> regionNames = getRegionNames();
        //Map<String, Vector3> signCoords = new HashMap<>();
        ArrayList<SignPlot> signPlots = new ArrayList<SignPlot>();
        for (String regionName : regionNames) {
            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region != null) {
                //commandSender.sendMessage("Region found: " + regionName);
                // if user is specifying plot
                if(args.length > 0) {
                    if (region.getId().equals(args[0])) {
                        Vector3 coords = getSignCords(region.getMaximumPoint(), region.getMinimumPoint(), commandSender);
                        if(coords != null) {
                            commandSender.sendMessage("Sign found at: " + coords.getX() + ", " + coords.getY() + ", " + coords.getZ());
                            return true; // debug command
                        } else {
                            commandSender.sendMessage("No sign found for region perimeter");
                        }
                    }
                } else{
                    Vector3 coords = getSignCords(region.getMaximumPoint(), region.getMinimumPoint(), commandSender);
                    //signCoords.put(region.getId(), coords);
                    signPlots.add(new SignPlot((int)coords.getX(), (int)coords.getY(), (int)coords.getZ(), regionName));
                }

            } else {
                commandSender.sendMessage("No region found with the name: " + regionName);
            }
        }


        // Save to plots.json
        JsonUtil.writeToJsonFile(signPlots, "signplots.json", this.pluginFile);
        commandSender.sendMessage("plots.json saved to spooky/plotsigns/signplots");
        commandSender.sendMessage(ChatColor.YELLOW + "Please refresh for changes to take affect");

        return true;
    }

    private Vector3 getSignCords(BlockVector3 maxPoint, BlockVector3 minPoint, CommandSender cs){

        int y = minPoint.getBlockY() + 1;
        int x = Math.max(maxPoint.getBlockX(), minPoint.getBlockX()) + 1;
        int z = Math.max(maxPoint.getBlockZ(), minPoint.getBlockZ()) + 1;

        int xWidth = Math.abs(minPoint.getBlockX() - maxPoint.getBlockX()) + 3;
        int zLength = Math.abs(minPoint.getBlockZ() - maxPoint.getBlockZ());

        ArrayList<Vector3> outerBlocks = new ArrayList<Vector3>();

        for(int i = 0; i < xWidth; i++){
            // top row
            outerBlocks.add(Vector3.at(x - i, y, z));
            // bottom row
            outerBlocks.add(Vector3.at(x - i, y, z - zLength - 2));
        }

        for(int i = 0; i < zLength + 1; i++){
            // left column
            outerBlocks.add(Vector3.at(x, y, z - i));
            // right column
            outerBlocks.add(Vector3.at(x - xWidth + 1, y, z - i - 1));
        }

        // check outerBlocks for a sign
        for(int i =0; i < outerBlocks.toArray().length; i++) {
            Block block = world.getBlockAt((int)outerBlocks.get(i).getX(), (int)outerBlocks.get(i).getY(), (int)outerBlocks.get(i).getZ());
            // if block is a sign, we found the coordinates
            //cs.sendMessage("xyz: " + block.getX() + ", " + block.getY() + ", " + block.getZ());
            if (isMaterialSign(block.getType(), cs)){
                return Vector3.at(block.getX(), block.getY(), block.getZ());
            }
        }

        return null;
    }

    private RegionManager getRegionManager(World world) {
        // Ensure you handle nulls and valid world instances appropriately
        if (world == null) {
            return null;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
        //return WorldGuard.getInstance().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    private boolean isMaterialSign(Material material, CommandSender cs) {
        //cs.sendMessage("type: " + material.toString());
        String materialName = material.toString().toLowerCase();
        return materialName.contains("sign");
    }
}

