package org.spooky.plotsigns.objects;

import java.time.Duration;
import java.time.Instant;

public class SignTimedObject extends TimeObject {
    public final String regionName;
    public int count;

    public SignTimedObject(String regionName) {
        this.regionName = regionName;
        this.count = 1;
    }
}
