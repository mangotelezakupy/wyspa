package com.papaya.wyspa.world;

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
@Override
    public void getNoise(BiomeGenerator biomeGenerator, double[] buffer, int x, int z) {
        final double d0 = settings.getCoordinateScale();
        final double d2 = settings.getHeightScale();
        final double d3 = settings.getCoordinateScale() / settings.getMainNoiseScaleX();
        final double d4 = settings.getHeightScale() / settings.getMainNoiseScaleY();
        final double d5 = settings.getCoordinateScale() / settings.getMainNoiseScaleZ();

        this.a(biomeGenerator, buffer, x, z, d0, d2, d3, d4, d5, 3, -10);

    }

    @Override
    public TerrainSettings getTerrainSettings() {
        TerrainSettings settings = new TerrainSettings();
        settings.stoneBlock = this.settings.getStoneBlock();
        settings.waterBlock = this.settings.getWaterBlock();
        settings.seaLevel = this.settings.getSeaLevel();
        return settings;
    }
    
        private double a(double d0, double d1, int i) {
        final double d2 = this.settings.getBaseSize();
                double d3 = (i - (d2 + d0 * d2 / 8.0 * 4.0)) * this.settings.getStretchY() * 128.0 / 256.0 / d1;
      
             if (d3 < 0.0) {
                  d3 *= 4.0;
           
                   }
                      return d3;
    }

    private double c(final int i, final int j) {
            double d0 = this.depthNoise.a(i * this.settings.getDepthNoiseScaleX(), 10.0,
                    j * this.settings.getDepthNoiseScaleZ(), 1.0, 0.0, true) / 8000.0;

            if (d0 < 0.0) {
                d0 = -d0 * 0.3;

            }
            d0 = d0 * 3.0 - 2.0;
            if (d0 < 0.0) {
                d0 /= 28.0;
            } else {
                if (d0 > 1.0) {
                    d0 = 1.0;

                }
                d0 /= 40.0;

            }
            return d0;

        }

    protected double g() {
        return this.i() - 4;
    }

    protected double h() {
        return 0.0;

    }

    private int i() {
        return 33;
    }
}
// Wyspa Retardów | Generator Świata 01.08
