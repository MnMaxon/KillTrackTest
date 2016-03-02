package me.MnMaxon.KillTrack;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Created by MnMaxon on 3/1/2016.  Aren't I great?
 */
public class PlayerInfo {
    private static HashMap<UUID, PlayerInfo> pMap = new HashMap<>();
    private final UUID uuid;
    private int deaths = 0;
    private int kills = 0;

    public PlayerInfo(OfflinePlayer p) {
        this.uuid = p.getUniqueId();
        MySQL.ResultPack r = KillTrack.getDB().executePreparedQuery("SELECT * FROM Stats WHERE UUID='" + p.getUniqueId() + "';");
        try {
            if (r.getResultSet().next()) try {
                kills = r.getResultSet().getInt("Kills");
                deaths = r.getResultSet().getInt("Deaths");
            } catch (Exception ignored) {
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        r.close();
    }

    public static void loadAll() {
        for (Player p : Bukkit.getOnlinePlayers()) load(p);
    }

    public static void load(Player p) {
        pMap.put(p.getUniqueId(), new PlayerInfo(p));
    }

    public static void removeAll() {
        for (Player p : Bukkit.getOnlinePlayers()) remove(p);
    }

    public static void remove(Player p) {
        get(p).save();
        pMap.remove(p.getUniqueId());
    }

    public static PlayerInfo get(OfflinePlayer op) {
        PlayerInfo playerInfo = pMap.get(op.getUniqueId());
        if (playerInfo != null) return playerInfo;
        return new PlayerInfo(op);
    }

    public static Set<UUID> getRegistered() {
        return pMap.keySet();
    }

    public void save() {
        boolean insert;
        MySQL.ResultPack r = KillTrack.getDB().executePreparedQuery("SELECT * FROM Stats WHERE UUID='" + uuid + "';");
        try {
            insert = !r.getResultSet().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        r.close();

        if (insert)
            KillTrack.getDB().executePreparedStatement("INSERT INTO Stats (UUID,Kills,Deaths) VALUES ('" + uuid + "','" + getKills() + "','" + getDeaths() + "');");
        else {
            KillTrack.getDB().executePreparedStatement("UPDATE Stats SET Kills='" + getKills() + "' WHERE UUID='" + uuid + "';");
            KillTrack.getDB().executePreparedStatement("UPDATE Stats SET Deaths='" + getDeaths() + "' WHERE UUID='" + uuid + "';");
        }
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }
}
