package me.MnMaxon.KillTrack;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class KillTrackListener implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PlayerInfo.load(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerInfo.remove(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        PlayerInfo victimInfo = PlayerInfo.get(e.getEntity());
        victimInfo.addDeath();
        victimInfo.save();
        Player killer = e.getEntity().getKiller();
        if (killer != null && !killer.equals(e.getEntity()) && killer.isOnline()) {
            PlayerInfo killerInfo = PlayerInfo.get(killer);
            killerInfo.addKill();
            killerInfo.save();
        }
    }
}
