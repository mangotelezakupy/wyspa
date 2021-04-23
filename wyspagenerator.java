package com.dfsek.wyspa.world;

import com.wyspa.api.wyspaPlugin;
import com.wyspa.api.event.events.world.wyspaWorldLoadEvent;
import com.wyspa.api.math.vector.Location;
import com.wyspa.api.math.vector.Vector3;
import com.wyspa.api.platform.block.BlockData;
import com.wyspa.api.platform.world.World;
import com.wyspa.api.platform.world.generator.GeneratorWrapper;
import com.wyspa.api.world.biome.UserDefinedBiome;
import com.wyspa.api.world.biome.provider.BiomeProvider;
import com.wyspa.api.world.generation.wyspaChunkGenerator;
import com.wyspa.api.world.palette.Palette;
import com.wyspa.config.pack.ConfigPack;
import com.wyspa.config.pack.WorldConfig;
import com.wyspa.profiler.WorldProfiler;
import com.wyspa.world.generation.math.samplers.Sampler;
import net.FastMath;

public class wyspaWorld {
    private final BiomeProvider provider;
    private final WorldConfig config;
    private final boolean safe;
    private final WorldProfiler profiler;
    private final World world;
    private final BlockData air;


    public wyspaWorld(World w, ConfigPack c, wyspaPlugin main) {
        if(!iswyspaWorld(w)) throw new IllegalArgumentException("World " + w + " is not a wyspa World!");
        this.world = w;
        config = c.toWorldConfig(this);
        this.provider = config.getProvider();
        profiler = new WorldProfiler(w);
        air = main.getWorldHandle().createBlockData("minecraft:air");
        main.getEventManager().callEvent(new wyspaWorldLoadEvent(this, c));
        safe = true;
    }

    public static boolean iswyspaWorld(World w) {
        return w.getGenerator().getHandle() instanceof GeneratorWrapper;
    }

    public World getWorld() {
        return world;
    }

    public wyspaChunkGenerator getGenerator() {
        return ((GeneratorWrapper) world.getGenerator().getHandle()).getHandle();
    }

    public BiomeProvider getBiomeProvider() {
        return provider;
    }

    public WorldConfig getConfig() {
        return config;
    }

    public boolean isSafe() {
        return safe;
    }

    public WorldProfiler getProfiler() {
        return profiler;
    }

    public BlockData getUngeneratedBlock(int x, int y, int z) {
        UserDefinedBiome biome = (UserDefinedBiome) provider.getBiome(x, z);
        Palette<BlockData> palette = biome.getGenerator(world).getPalette(y);
        Sampler sampler = config.getSamplerCache().get(x, z);
        int fdX = FastMath.floorMod(x, 16);
        int fdZ = FastMath.floorMod(z, 16);
        double noise = sampler.sample(fdX, y, fdZ);
        if(noise > 0) {
            int level = 0;
            for(int yi = world.getMaxHeight(); yi > y; yi--) {
                if(sampler.sample(fdX, yi, fdZ) > 0) level++;
                else level = 0;
            }
            return palette.get(level, x, y, z);
        } else if(y <= biome.getConfig().getSeaLevel()) {
            return biome.getConfig().getOceanPalette().get(biome.getConfig().getSeaLevel() - y, x, y, z);
        } else return air;
    }

    public BlockData getUngeneratedBlock(Location l) {
        return getUngeneratedBlock(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public BlockData getUngeneratedBlock(Vector3 v) {
        return getUngeneratedBlock(v.getBlockX(), v.getBlockY(), v.getBlockZ());
    }
}
