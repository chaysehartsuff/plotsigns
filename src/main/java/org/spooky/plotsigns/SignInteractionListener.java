package org.spooky.plotsigns;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.event.block.Action;

public class SignInteractionListener implements Listener {

    public SignInteractionListener() {
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if(clickedBlock != null) {
                if (isMaterialSign(clickedBlock.getType())) {
                    // Check if the block's coordinates match
                    //if (clickedBlock.getX() == x && clickedBlock.getY() == y && clickedBlock.getZ() == z) {
                        Sign sign = (Sign) clickedBlock.getState();
                        // Handle the sign interaction
                        // For example, you could send the player a message with the sign's text
                        //event.getPlayer().sendMessage("You clicked the sign at (" + x + "," + y + "," + z + "): " + String.join(" ", sign.getLines()));
                    //}
                }
            }
        }
    }

    private boolean isMaterialSign(Material material) {
        String materialName = material.toString().toLowerCase();
        return materialName.contains("sign");
    }
}
