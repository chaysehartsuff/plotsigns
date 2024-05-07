package org.spooky.plotsigns.storage;

public class SignPlot {
    private int x;
    private int y;
    private int z;
    private String region_name;

    // Jackson needs a no-argument constructor for deserialization
    public SignPlot() {}

    public SignPlot(int x, int y, int z, String region_name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.region_name = region_name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getRegionName() {
        return region_name;
    }

    public void setRegionName(String region_name) {
        this.region_name = region_name;
    }
}
