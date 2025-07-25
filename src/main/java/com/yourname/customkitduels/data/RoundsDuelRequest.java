package com.yourname.customkitduels.data;

import org.bukkit.entity.Player;

public class RoundsDuelRequest {
    
    private final Player challenger;
    private final Player target;
    private final Kit kit;
    private final Arena arena;
    private final int targetRounds;
    private final long timestamp;
    
    public RoundsDuelRequest(Player challenger, Player target, Kit kit, Arena arena, int targetRounds) {
        this.challenger = challenger;
        this.target = target;
        this.kit = kit;
        this.arena = arena;
        this.targetRounds = targetRounds;
        this.timestamp = System.currentTimeMillis();
    }
    
    public Player getChallenger() {
        return challenger;
    }
    
    public Player getTarget() {
        return target;
    }
    
    public Kit getKit() {
        return kit;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public int getTargetRounds() {
        return targetRounds;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}