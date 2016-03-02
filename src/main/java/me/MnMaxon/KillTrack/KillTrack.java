package me.MnMaxon.KillTrack;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class KillTrack extends JavaPlugin {
    public static String dataFolder;
    public static KillTrack plugin;
    private static String message;
    private static MySQL database;

    public static void reloadConfigs(CommandSender sender) {
        boolean save = false;
        SuperYaml mainConfig = new SuperYaml(dataFolder + "/Config.yml");
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("Message", "&a%p has &c%k kills &aand %d deaths.");
        defaults.put("MySQL.IP", "localhost");
        defaults.put("MySQL.Database", "MineCraft");
        defaults.put("MySQL.Username", "admin");
        defaults.put("MySQL.Password", "admin");
        try {
            database = new MySQL(mainConfig.getString("MySQL.IP"), mainConfig.getString("MySQL.Database"),
                    mainConfig.getString("MySQL.Username"), mainConfig.getString("MySQL.Password"));
        } catch (Exception ex) {
            String error = "KillTrack failed to connect to the database, shutting down...";
            if (sender != null) sender.sendMessage(error);
            Bukkit.getLogger().severe(error);
            plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }
        ArrayList<String> statements = new ArrayList<>();
        statements.add("CREATE TABLE IF NOT EXISTS Stats (UUID VARCHAR(36));");
        statements.add("ALTER TABLE Stats ADD COLUMN Kills INT(10);");
        statements.add("ALTER TABLE Stats ADD COLUMN Deaths INT(10);");
        for (String statement : statements) getDB().executePreparedStatement(statement, true);
        for (Map.Entry<String, Object> entry : defaults.entrySet())
            if (mainConfig.get(entry.getKey()) == null) {
                mainConfig.set(entry.getKey(), entry.getValue());
                save = true;
            }
        message = ChatColor.translateAlternateColorCodes('&', mainConfig.getString("Message"));
        if (save) mainConfig.save();
        if (PlayerInfo.getRegistered().size() > 0) PlayerInfo.removeAll();
        PlayerInfo.loadAll();
    }

    public static MySQL getDB() {
        return database;
    }

    @Override
    public void onEnable() {
        plugin = this;
        dataFolder = this.getDataFolder().getAbsolutePath();
        getServer().getPluginManager().registerEvents(new KillTrackListener(), this);
        reloadConfigs(null);
    }

    @Override
    public void onDisable() {
        PlayerInfo.removeAll();
    }

    @SuppressWarnings("Contract")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
            if (sender.isOp()) {
                reloadConfigs(sender);
                sender.sendMessage(ChatColor.GREEN + "Config Reloaded!");
            } else sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
            return true;
        }
        OfflinePlayer target;
        if (args.length == 0) {
            if (sender instanceof Player) target = (OfflinePlayer) sender;
            else {
                sender.sendMessage(ChatColor.RED + "Use Like: /Stat [Player]");
                return true;
            }
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) target = Bukkit.getOfflinePlayer(args[0]);
        }
        if (target == null || (!target.isOnline() && !target.hasPlayedBefore()))
            sender.sendMessage(ChatColor.RED + "The player " + ChatColor.AQUA + args[0] + ChatColor.RED + " could not be found!");
        else {
            PlayerInfo pInfo = PlayerInfo.get(target);
            sender.sendMessage(message.replace("%p", target.getName()).replace("%d", pInfo.getDeaths() + "").replace("%k", pInfo.getKills() + ""));
        }
        return true;
    }
}