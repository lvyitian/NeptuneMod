/*
 * This file is part of NeptuneVanilla, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015-2017, Jamie Mansfield <https://github.com/jamierocks>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.neptunepowered.vanilla.mixin.minecraft.world;

import co.aikar.timings.TimingHistory;
import co.aikar.timings.WorldTimingsHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.neptunepowered.vanilla.interfaces.minecraft.entity.IMixinEntity;
import org.neptunepowered.vanilla.interfaces.minecraft.tileentity.IMixinTileEntity;
import org.neptunepowered.vanilla.interfaces.minecraft.world.IMixinWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mixin(World.class)
public abstract class MixinWorld implements IMixinWorld {

    @Shadow @Final public List<Entity> weatherEffects;
    @Shadow @Final public List<Entity> loadedEntityList;
    @Shadow @Final protected List<Entity> unloadedEntityList;
    @Shadow @Final private List<TileEntity> tileEntitiesToBeRemoved;
    @Shadow @Final public List<TileEntity> loadedTileEntityList;
    @Shadow @Final public List<TileEntity> tickableTileEntities;
    @Shadow @Final private List<TileEntity> addedTileEntityList;
    @Shadow @Final public List<EntityPlayer> playerEntities;
    @Shadow @Final private WorldBorder worldBorder;
    @Shadow @Final public Profiler theProfiler;
    @Shadow @Final public Random rand;
    @Shadow @Final public WorldProvider provider;
    @Shadow protected WorldInfo worldInfo;
    @Shadow private boolean processingLoadedTiles;
    @Shadow protected boolean spawnHostileMobs;
    @Shadow protected boolean spawnPeacefulMobs;
    @Shadow protected IChunkProvider chunkProvider;
    @Shadow protected VillageCollection villageCollectionObj;
    @Shadow protected Set<ChunkCoordIntPair> activeChunkSet;
    @Shadow protected int updateLCG;

    protected WorldTimingsHandler timings;

    @Shadow public abstract long getSeed();
    @Shadow public abstract void updateEntity(Entity ent);
    @Shadow protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);
    @Shadow public abstract Chunk getChunkFromChunkCoords(int chunkX, int chunkZ);
    @Shadow protected abstract void onEntityRemoved(Entity entityIn);
    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);
    @Shadow public abstract Chunk getChunkFromBlockCoords(BlockPos pos);
    @Shadow public abstract boolean addTileEntity(TileEntity tile);
    @Shadow public abstract void markBlockForUpdate(BlockPos pos);
    @Shadow public abstract boolean isAreaLoaded(BlockPos from, BlockPos to);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract WorldInfo getWorldInfo();
    @Shadow public abstract EnumDifficulty shadow$getDifficulty();
    @Shadow public abstract GameRules getGameRules();
    @Shadow public abstract int calculateSkylightSubtracted(float p_72967_1_);
    @Shadow public abstract int getSkylightSubtracted();
    @Shadow public abstract void setSkylightSubtracted(int newSkylightSubtracted);
    @Shadow public abstract long getTotalWorldTime();
    @Shadow protected abstract void playMoodSoundAndCheckLight(int p_147467_1_, int p_147467_2_, Chunk chunkIn);
    @Shadow public abstract boolean isRainingAt(BlockPos strikePosition);
    @Shadow public abstract BlockPos getPrecipitationHeight(BlockPos pos);
    @Shadow public abstract boolean canBlockFreezeNoWater(BlockPos pos);
    @Shadow public abstract boolean setBlockState(BlockPos pos, IBlockState state);
    @Shadow public abstract boolean canSnowAt(BlockPos pos, boolean checkLight);
    @Shadow public abstract BiomeGenBase getBiomeGenForCoords(final BlockPos pos);
    @Shadow public abstract WorldType shadow$getWorldType();
    @Shadow public void tick() {}
    @Shadow protected void updateBlocks() {}

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client,
            CallbackInfo ci) {
        // This needs to be initialised here because any earlier and the worldInfo won't have been set.
        this.timings = new WorldTimingsHandler((WorldServer) (Object) this);
    }

    /**
     * @author jamierocks - 2nd October 2016
     * @reason Add timings calls
     */
    @Overwrite
    public void updateEntities() {
        this.theProfiler.startSection("entities");
        this.theProfiler.startSection("global");

        for (int i = 0; i < this.weatherEffects.size(); ++i)  {
            Entity entity = this.weatherEffects.get(i);

            try {
                ++entity.ticksExisted;
                entity.onUpdate();
            } catch (Throwable throwable2) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable2, "Ticking entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");

                if (entity == null) {
                    crashreportcategory.addCrashSection("Entity", "~~NULL~~");
                } else {
                    entity.addEntityCrashInfo(crashreportcategory);
                }

                throw new ReportedException(crashreport);
            }

            if (entity.isDead) {
                this.weatherEffects.remove(i--);
            }
        }

        this.theProfiler.endStartSection("remove");
        this.timings.entityRemoval.startTiming(); // Neptune - timings
        this.loadedEntityList.removeAll(this.unloadedEntityList);

        for (int k = 0; k < this.unloadedEntityList.size(); ++k) {
            Entity entity = this.unloadedEntityList.get(k);
            int j = entity.chunkCoordX;
            int l1 = entity.chunkCoordZ;

            if (entity.addedToChunk && this.isChunkLoaded(j, l1, true)) {
                this.getChunkFromChunkCoords(j, l1).removeEntity(entity);
            }
        }

        for (int l = 0; l < this.unloadedEntityList.size(); ++l) {
            this.onEntityRemoved(this.unloadedEntityList.get(l));
        }

        this.unloadedEntityList.clear();
        this.timings.entityRemoval.stopTiming(); // Neptune - timings
        this.theProfiler.endStartSection("regular");

        this.timings.entityTick.startTiming(); // Neptune - timings
        TimingHistory.entityTicks += this.loadedEntityList.size(); // Neptune - Timings

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1) {
            Entity entity = this.loadedEntityList.get(i1);

            if (entity.ridingEntity != null) {
                if (!entity.ridingEntity.isDead && entity.ridingEntity.riddenByEntity == entity) {
                    continue;
                }

                entity.ridingEntity.riddenByEntity = null;
                entity.ridingEntity = null;
            }

            this.theProfiler.startSection("tick");

            if (!entity.isDead) {
                try {
                    ((IMixinEntity) entity).getTimingsHandler().startTiming(); // Neptune - Timings
                    this.updateEntity(entity);
                } catch (Throwable throwable1) {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable1, "Ticking entity");
                    CrashReportCategory crashreportcategory2 = crashreport1.makeCategory("Entity being ticked");
                    entity.addEntityCrashInfo(crashreportcategory2);
                    throw new ReportedException(crashreport1);
                }
                // Neptune - start
                finally {
                    ((IMixinEntity) entity).getTimingsHandler().stopTiming();
                }
                // Neptune - end
            }

            this.theProfiler.endSection();
            this.theProfiler.startSection("remove");

            if (entity.isDead) {
                int k1 = entity.chunkCoordX;
                int i2 = entity.chunkCoordZ;

                if (entity.addedToChunk && this.isChunkLoaded(k1, i2, true)) {
                    this.getChunkFromChunkCoords(k1, i2).removeEntity(entity);
                }

                this.loadedEntityList.remove(i1--);
                this.onEntityRemoved(entity);
            }

            this.theProfiler.endSection();
        }
        this.timings.entityTick.stopTiming(); // Neptune - timings

        this.theProfiler.endStartSection("blockEntities");
        this.timings.tileEntityTick.startTiming(); // Neptune - timings
        this.processingLoadedTiles = true;
        Iterator<TileEntity> iterator = this.tickableTileEntities.iterator();

        while (iterator.hasNext()) {
            TileEntity tileentity = iterator.next();

            if (!tileentity.isInvalid() && tileentity.hasWorldObj()) {
                BlockPos blockpos = tileentity.getPos();

                if (this.isBlockLoaded(blockpos) && this.worldBorder.contains(blockpos)) {
                    try {
                        ((IMixinTileEntity) tileentity).getTimingsHandler().startTiming(); // Neptune - Timings
                        ((ITickable) tileentity).update();
                    } catch (Throwable throwable) {
                        CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        CrashReportCategory crashreportcategory1 = crashreport2.makeCategory("Block entity being ticked");
                        tileentity.addInfoToCrashReport(crashreportcategory1);
                        throw new ReportedException(crashreport2);
                    }
                    // Neptune - start
                    finally {
                        ((IMixinTileEntity) tileentity).getTimingsHandler().stopTiming();
                    }
                    // Neptune - end
                }
            }

            if (tileentity.isInvalid()) {
                iterator.remove();
                this.loadedTileEntityList.remove(tileentity);

                if (this.isBlockLoaded(tileentity.getPos())) {
                    this.getChunkFromBlockCoords(tileentity.getPos()).removeTileEntity(tileentity.getPos());
                }
            }
        }

        this.processingLoadedTiles = false;

        if (!this.tileEntitiesToBeRemoved.isEmpty()) {
            this.tickableTileEntities.removeAll(this.tileEntitiesToBeRemoved);
            this.loadedTileEntityList.removeAll(this.tileEntitiesToBeRemoved);
            this.tileEntitiesToBeRemoved.clear();
        }
        this.timings.tileEntityTick.stopTiming(); // Neptune - timings

        this.theProfiler.endStartSection("pendingBlockEntities");
        this.timings.tileEntityPending.startTiming(); // Neptune - timings

        if (!this.addedTileEntityList.isEmpty()) {
            for (TileEntity tileEntity : this.addedTileEntityList) {
                if (!tileEntity.isInvalid()) {
                    if (!this.loadedTileEntityList.contains(tileEntity)) {
                        this.addTileEntity(tileEntity);
                    }

                    if (this.isBlockLoaded(tileEntity.getPos())) {
                        this.getChunkFromBlockCoords(tileEntity.getPos()).addTileEntity(tileEntity.getPos(), tileEntity);
                    }

                    this.markBlockForUpdate(tileEntity.getPos());
                }
            }

            this.addedTileEntityList.clear();
        }

        this.timings.tileEntityPending.stopTiming(); // Neptune - timings
        TimingHistory.tileEntityTicks += this.tickableTileEntities.size(); // Neptune - Timings

        this.theProfiler.endSection();
        this.theProfiler.endSection();
    }

    @Override
    public void setWorldInfo(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    @Override
    public WorldTimingsHandler getTimings() {
        return this.timings;
    }

}
