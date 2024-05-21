package org.spooky.plotsigns.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.spooky.plotsigns.objects.SignTimedObject;
import org.spooky.plotsigns.storage.SignPlot;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;

public class PlotRefresh implements CommandExecutor {

    private List<SignPlot> signplots;
    private World world;

    public PlotRefresh(List<SignPlot> signplots, World world){
        this.signplots = signplots;
        this.world = world;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        int claimed = 0;
        int unclaimed = 0;
        for (SignPlot sp : signplots) {
            Block block = world.getBlockAt(sp.getX(), sp.getY(), sp.getZ());
            RegionManager rm = getRegionManager(world);

            // Check if the block is a sign
            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                ProtectedRegion region = rm.getRegion(sp.getRegionName());
                if(region != null) {
                    String playerName = null;
                    if (!region.getOwners().getUniqueIds().isEmpty()) {
                        UUID firstOwnerId = region.getOwners().getUniqueIds().iterator().next();
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(firstOwnerId);
                        playerName = offlinePlayer.getName();

                        if (playerName == null){
                            playerName = fetchUsernameFromUUID(firstOwnerId.toString());
                        }
                    }

                    if (playerName != null) {
                        setClaimedSign(sign, playerName);
                        claimed++;
                    }
                    else {
                        String plotNum = sp.getRegionName().replaceAll("[^0-9]", "").isEmpty() ? "<Invalid Region Name>" : sp.getRegionName().replaceAll("[^0-9]", "");
                        setUnclaimedSign(sign, plotNum);
                        unclaimed++;
                    }

                } else {
                    commandSender.sendMessage("Region " + sp.getRegionName() + " is no longer available");
                }
            }
        }
        commandSender.sendMessage("Refreshed " + claimed + " claimed and " + unclaimed + " unclaimed signs!");
        return true;
    }

    private void setClaimedSign(Sign sign, String playerName){
        SignSide ss = sign.getSide(Side.FRONT);
        ss.setLine(0, "============");
        ss.setLine(1, "" + (playerName.length() > 13 ? playerName.substring(0, 13) : playerName) + "'s");
        ss.setLine(2, "Plot");
        ss.setLine(3, "============");
        ss.setGlowingText(true);
        ss.setColor(DyeColor.LIME);
        sign.update(true, false);
    }

    private void setUnclaimedSign(Sign sign, String plotNum){
        SignSide ss = sign.getSide(Side.FRONT);
        ss.setLine(0,   ChatColor.BLACK+"===========");
        ss.setLine(1,  "Plot");
        ss.setLine(2, "#" + plotNum);
        ss.setLine(3,  "============");
        ss.setGlowingText(true);
        ss.setColor(DyeColor.BLACK);
        sign.update(true, false);
    }

    private RegionManager getRegionManager(World world) {
        if (world == null) {
            return null;
        }
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
    }

    private String fetchUsernameFromUUID(String uuid) {
        String requestUrl = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();

                // Parse the JSON response to extract the username
                JSONObject jsonResponse = new JSONObject(response.toString());
                return jsonResponse.getString("name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
