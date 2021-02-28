package org.mcnative.runtime.bungeecord;

import net.pretronic.libraries.utility.SystemInfo;
import org.mcnative.runtime.api.ServerPerformance;

public class BungeecordServerPerformance implements ServerPerformance {

    private static final float[] EMPTY_TPS = new float[]{0,0,0};

    @Override
    public float[] getRecentTps() {
        return EMPTY_TPS;
    }

    @Override
    public int getUsedMemory() {
        return (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576L);
    }

    @Override
    public float getCpuUsage() {
        return (float) SystemInfo.getPercentProcessCpuLoad(2);
    }
}
