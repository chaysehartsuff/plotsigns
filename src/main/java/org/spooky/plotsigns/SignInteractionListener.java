package org.spooky.plotsigns;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.spooky.plotsigns.objects.GracePeriodObject;
import org.spooky.plotsigns.objects.SignTimedObject;
import org.spooky.plotsigns.storage.SignPlot;

import java.util.List;

public class SignInteractionListener implements Listener {

    List<SignPlot> signPlots;
    World world;
    List<SignTimedObject> sto;
    List<GracePeriodObject> gpo;
    private int confirm_clicks;
    private int confirmClicksConfig;

    public SignInteractionListener(List<SignPlot> signPlots, World world, List<SignTimedObject> sto, List<GracePeriodObject> gpo, int confirmClicksConfig) {
        this.signPlots = signPlots;
        this.world = world;
        this.sto = sto;
        this.gpo = gpo;
        this.confirm_clicks = 0;
        this.confirmClicksConfig = confirmClicksConfig;
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
                            event.setCancelled(true);
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
                                    int confirmClicks = this.confirmClicksConfig;
                                    if(confirmRelease(event.getPlayer(), region, 3, confirmClicks)){
                                        event.getPlayer().sendMessage(ChatColor.RED+"" + this.confirm_clicks + "/" + confirmClicks);
                                        event.getPlayer().sendMessage("You have released " + region.getId());
                                        event.getPlayer().sendMessage(ChatColor.YELLOW + "You have 5 minutes to reclaim before the plot will be cleared");
                                        region.getOwners().removeAll();
                                        setUnclaimedSign(sign, sp.getRegionName().replace("plot", ""));
                                        this.gpo.add(new GracePeriodObject(event.getPlayer(), region));

                                    } else if(this.confirm_clicks == 1) {
                                        event.getPlayer().sendMessage(ChatColor.YELLOW+"Click sign 3 times to release plot");
                                        event.getPlayer().sendMessage(ChatColor.RED+"" + this.confirm_clicks + "/" + confirmClicks);
                                    } else{
                                        event.getPlayer().sendMessage(ChatColor.RED+"" + this.confirm_clicks + "/" + confirmClicks);
                                    }
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
                                    setClaimedSign(sign, event.getPlayer().getName());
                                    event.getPlayer().sendMessage(ChatColor.GREEN + "You now own " + region.getId());
                                    sign.update();
                                    // only clear plot if user doesn't have a graceperiod claim
                                    if(!hasGracePeriod(event.getPlayer(), region, 300)){
                                        clearRegionExceptY(event, region, 74);
                                    }
                                } else {
                                    event.getPlayer().sendMessage(ChatColor.YELLOW + "This plot already has an owner.");
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setClaimedSign(Sign sign, String playerName){

        SignSide ss = sign.getSide(Side.FRONT);
        ss.setLine(0, "============");
        ss.setLine(1, "" + (playerName.length() > 13 ? playerName.substring(0, 13) : playerName) + "'s");
        ss.setLine(2, "Plot");
        ss.setLine(3, "============");
        ss.setGlowingText(true);
        ss.setColor(DyeColor.LIME);
        //applyNBTToSign(sign, true);
        sign.update(true, false);
    }

    private void setUnclaimedSign(Sign sign, String plotNum){
        // get claimed sign template

        // You were trying to figure out how to get the glowing ink sac affect on all text colors....
        SignSide ss = sign.getSide(Side.FRONT);
        ss.setLine(0,   ChatColor.BLACK+"===========");
        ss.setLine(1,  "Plot");
        ss.setLine(2, "#" + plotNum);
        ss.setLine(3,  "============");
        ss.setGlowingText(true);
        ss.setColor(DyeColor.BLACK);
        sign.update(true, false);
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

    private boolean confirmRelease(Player player, ProtectedRegion region, int seconds, int confirmClicks){
        boolean foundSTO = false;
        for (SignTimedObject _sto : sto) {
            if (_sto.regionName.equals(region.getId()) && _sto.count < confirmClicks && _sto.SecondsPassed() < seconds) {
                foundSTO = true;
                _sto.count++;
                this.confirm_clicks = _sto.count;
                if (_sto.count >= confirmClicks)
                    return true;
                break;
            }
        }
        if (!foundSTO) {
            sto.add(new SignTimedObject(region.getId()));
            this.confirm_clicks = 1;
        }

        return false;
    }

    private boolean hasGracePeriod(Player player, ProtectedRegion region, int seconds){

        for(GracePeriodObject _gpo : gpo){
            if(_gpo.player.equals(player) && region == _gpo.region && _gpo.SecondsPassed() < seconds){
                return true;
            }
        }
        return false;
    }

    public void clearRegionExceptY(PlayerEvent event, ProtectedRegion region, int yLevel) {
        org.bukkit.entity.Player bukkitPlayer = event.getPlayer();
        org.bukkit.World bukkitWorld = bukkitPlayer.getWorld();

        // Convert Bukkit world to WorldEdit world
        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(bukkitWorld);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();

            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                        BlockVector3 point = BlockVector3.at(x, y, z);
                        if (y != yLevel) {
                            if (region.contains(point)) {
                                editSession.setBlock(point, BlockTypes.AIR.getDefaultState());
                            }
                        } else if (region.contains(point)){
                            editSession.setBlock(point, BlockTypes.OAK_PLANKS.getDefaultState());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
