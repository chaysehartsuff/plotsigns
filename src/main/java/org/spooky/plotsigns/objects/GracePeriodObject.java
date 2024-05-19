package org.spooky.plotsigns.objects;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;

public class GracePeriodObject extends TimeObject {

    public Player player;
    public ProtectedRegion region;

    public GracePeriodObject(Player player, ProtectedRegion region) {
        this.player = player;
        this.region = region;
    }
}
