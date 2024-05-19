package org.spooky.plotsigns.objects;

import org.graalvm.nativebridge.In;

import java.time.Duration;
import java.time.Instant;

public class TimeObject {

    public Instant timestamp;

    public TimeObject(){
        timestamp = Instant.now();
    }

    public int SecondsPassed(){
        Duration duration = Duration.between(this.timestamp, Instant.now());
        return (int)duration.getSeconds();
    }
}
