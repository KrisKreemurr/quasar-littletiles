package team.creative.littletiles.server.level.little;

import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.border.WorldBorder.Settings;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import team.creative.littletiles.common.level.little.FakeLevelInfo;

public class FakeServerLevelInfo extends FakeLevelInfo implements ServerLevelData {
    
    public FakeServerLevelInfo(Difficulty difficulty, boolean hardcore, boolean isFlat) {
        super(difficulty, hardcore, isFlat);
    }
    
    @Override
    public String getLevelName() {
        return "fake";
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setThundering(boolean thundering) {}
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public int getRainTime() {
        return 0;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setRainTime(int time) {}
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setThunderTime(int time) {}
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public int getThunderTime() {
        return 0;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public int getClearWeatherTime() {
        return 0;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setClearWeatherTime(int time) {}
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public int getWanderingTraderSpawnDelay() {
        return 0;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setWanderingTraderSpawnDelay(int delay) {}
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public int getWanderingTraderSpawnChance() {
        return 0;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setWanderingTraderSpawnChance(int chance) {}
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public UUID getWanderingTraderId() {
        return null;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setWanderingTraderId(UUID uuid) {}
    
    @Override
    public GameType getGameType() {
        return GameType.SURVIVAL;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setWorldBorder(Settings settings) {}
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public Settings getWorldBorder() {
        return WorldBorder.DEFAULT_SETTINGS;
    }
    
    @Override
    public boolean isInitialized() {
        return true;
    }
    
    @Override
    public void setInitialized(boolean init) {}
    
    @Override
    public boolean isAllowCommands() {
        return false;
    }
    
    @Override
    public void setGameType(GameType type) {}
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public TimerQueue<MinecraftServer> getScheduledEvents() {
        return null;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public float getDayTimeFraction() {
        return 0;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public float getDayTimePerTick() {
        return 0;
    }
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setDayTimeFraction(float dayTimeFraction) {}
    
    // 26.3: no longer part of the level data interfaces (kept as a plain method)
    public void setDayTimePerTick(float dayTimePerTick) {}
    
    @Override
    public void setAllowCommands(boolean allow) {}
}
