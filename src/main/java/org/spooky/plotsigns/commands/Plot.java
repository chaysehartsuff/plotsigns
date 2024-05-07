package org.spooky.plotsigns.commands;


import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.spooky.plotsigns.storage.SignPlot;

import java.util.List;

public class Plot implements CommandExecutor {

    private World world;

   public Plot(World world, List<SignPlot> signPlot){
        this.world = world;
   }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (args.length == 0) {

            commandSender.sendMessage("command missing <release>");
            return false;
        }

        switch (args[0]){
            // release and reset plot that player owns
            case "release":



                break;
        }

        return true;
    }

    private void findRegion(){

    }

    private RegionManager getRegionManager() {
        // Ensure you handle nulls and valid world instances appropriately
        if (world == null) {
            return null;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
        //return WorldGuard.getInstance().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    private Sign setUnclaimedSign(Sign sign, String plotNum){

        SignSide ss = sign.getSide(Side.FRONT);
        ss.setLine(0, ChatColor.YELLOW +  "============");
        ss.setLine(1, ChatColor.YELLOW + "Plot");
        ss.setLine(2, ChatColor.YELLOW +  "#" + plotNum);
        ss.setLine(3, ChatColor.YELLOW +  "============");
        sign.update();
        return sign;
    }
}

