package org.spooky.plotsigns;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.spooky.plotsigns.storage.SignPlot;

import java.util.List;

public class SignInteractionListener implements Listener {

    List<SignPlot> signPlots;
    World world;

    public SignInteractionListener(List<SignPlot> signPlots, World world) {
        this.signPlots = signPlots;
        this.world = world;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if(clickedBlock != null) {
                if (isMaterialSign(clickedBlock.getType())) {
                    Sign sign = (Sign) clickedBlock.getState();
                    // see if signPlots match sign
                    for (SignPlot sp : signPlots) {
                        // sign is signPlot
                        if (sp.getX() == sign.getX() && sp.getY() == sign.getY() && sp.getZ() == sign.getZ()) {

                            RegionManager regionManager = getRegionManager(world);
                            ProtectedRegion region = regionManager.getRegion(sp.getRegionName());

                            /// Player validation
                            // check if player owns the region
                            // check if player owns a region
                            /// Region validation
                            // check if region is available (has no owners)
                            //
                            if(region != null) {
                                // player validation
                                if(region.getOwners().contains(event.getPlayer().getUniqueId())) {
                                    event.getPlayer().sendMessage( ChatColor.YELLOW + "You already own this plot.");
                                    break; // <-- end interaction here
                                } else{
                                    String plotOwned = playerOwnsAPlot(event.getPlayer(), regionManager);
                                    if(!plotOwned.equals("")) {
                                        event.getPlayer().sendMessage(ChatColor.RED + "You already own " + plotOwned);
                                        event.getPlayer().sendMessage(ChatColor.YELLOW + "Remove your current plot before obtaining a new one.");
                                        break; // <-- end interaction here
                                    }

                                }
                                // region validation
                                if (!region.hasMembersOrOwners()) {
                                    // add player to region
                                    region.getOwners().addPlayer(event.getPlayer().getUniqueId());
                                    sign = setClaimedSign(sign, event.getPlayer().getName());
                                    event.getPlayer().sendMessage(ChatColor.GREEN + "You now own " + region.getId());
                                    sign.update();
                                } else {
                                    event.getPlayer().sendMessage(ChatColor.YELLOW + "This plot already has an owner.");
                                }
                            }

                            event.getPlayer().sendMessage("You found " + sp.getRegionName());
                            break;
                        }
                    }
                }
            }
        }
    }

    private Sign setClaimedSign(Sign sign, String playerName){

        SignSide ss = sign.getSide(Side.FRONT);
        ss.setLine(0, ChatColor.AQUA +  "============");
        ss.setLine(1, ChatColor.AQUA + (playerName.length() > 13 ? playerName.substring(0, 13) : playerName) + "'s");
        ss.setLine(2, ChatColor.AQUA +  "Plot");
        ss.setLine(3, ChatColor.AQUA +  "============");
        sign.update();
        return sign;
    }

    private String playerOwnsAPlot(Player player, RegionManager regionManager) {

        String plotidfound = "";
        // Iterate through all regions in the region manager
        for (ProtectedRegion region : regionManager.getRegions().values()) {
            // Check if the player is an owner of the region
            if (region.getOwners().contains(player.getUniqueId())) {
                plotidfound = region.getId();
            }
        }
        return plotidfound;
    }

    private boolean isMaterialSign(Material material) {
        String materialName = material.toString().toLowerCase();
        return materialName.contains("sign");
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
}
