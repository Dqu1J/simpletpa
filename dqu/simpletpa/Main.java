package dqu.simpletpa;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import dqu.updater.AutoUpdater;

import java.util.*;

public class Main extends JavaPlugin implements Listener
{
    public FileConfiguration config = getConfig();
    public HashMap<Player, List<String>> requests = new HashMap<>();
    public HashMap<String, List<String>> ignorelist = new HashMap<>();
    public List<String> togglelist = new ArrayList<>();

    @Override
    public void onEnable()
    {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        loadList();
        String path = this.getFile().getAbsoluteFile().toString();
        new AutoUpdater(this, "https://dqu1j.github.io/simpletpa/info.txt", "https://dqu1j.github.io/simpletpa/SimpleTpa2.jar", config.getBoolean("properties.autoupdate"), false, false, path);
        this.getLogger().info("Plugin has been enabled.");
    }

    @Override
    public void onDisable()
    {
        saveList();
        this.getLogger().info("Plugin has been disabled.");
    }

    public void loadList()
    {
        togglelist.clear();
        String listraw = config.getString("toggledlist");
        togglelist.addAll(Arrays.asList(listraw.split(";")));
        ignorelist.clear();
        listraw = config.getString("ignoredlist");
        List<String> ign = new ArrayList<>();
        for (String element : listraw.split(";"))
        {
            ign.clear();
            String[] part = element.split(":");
            if (part.length < 2) return;
            ign.addAll(Arrays.asList(part[1].split(",")));
            ignorelist.put(part[0], ign);
        }
    }

    public void saveList()
    {
        reloadConfig();
        config = getConfig();
        String listraw = "";
        for (String s : togglelist) {
            listraw += s + ";";
        }
        listraw = listraw.substring(0, listraw.length() - 1);
        config.set("toggledlist", listraw);

        listraw = "";
        for (Map.Entry<String, List<String>> element : ignorelist.entrySet())
        {
            String key = element.getKey();
            List<String> value = element.getValue();
            listraw += key + ":";
            for (String player : value)
            {
                listraw += player + ",";
            }
            if (listraw.length() > 0) listraw = listraw.substring(0, listraw.length() - 1);
            listraw += ";";
        }
        if (listraw.length() > 0) listraw = listraw.substring(0, listraw.length() - 1);
        config.set("ignoredlist", listraw);

        saveConfig();
    }

    public String getString(String text)
    {
        String str = config.getString(text);
        str = ChatColor.translateAlternateColorCodes('&', str);
        return str;
    }

    public void timeoutStart(Player target, Player player)
    {
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                if (requests.containsKey(target)) {
                    requests.remove(target);
                    target.sendMessage(getString("messages.timeout0").replace("%1", player.getName()));
                    player.sendMessage(getString("messages.timeout1").replace("%1", target.getName()));
                }
            }
        }, config.getInt("properties.accepttime")*20);
    }

    public void teleport(Player target, Player player)
    {
        if (requests.get(target).get(1).equalsIgnoreCase("0"))
        {
            player.sendMessage(getString("messages.teleporting0").replace("%1", target.getName()));
            target.sendMessage(getString("messages.teleporting1").replace("%1", player.getName()));
            player.teleport(target);
        } else {
            player.sendMessage(getString("messages.teleporting1").replace("%1", target.getName()));
            target.sendMessage(getString("messages.teleporting0").replace("%1", player.getName()));
            target.teleport(player);
        }
    }

    public boolean checkLocation(Location loc0, Location loc1)
    {
        if (loc0.getX() != loc1.getX()) return false;
        if (loc0.getY() != loc1.getY()) return false;
        if (loc0.getZ() != loc1.getZ()) return false;
        return true;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args)
    {
        if (alias.equalsIgnoreCase("tpa"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (args.length == 0) return false;
            Player player = (Player)sender;
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline())
            {
                sender.sendMessage(getString("messages.targetoffline").replace("%1", args[0]));
                return true;
            }
            if (requests.containsKey(target))
            {
                sender.sendMessage(getString("messages.targetpending").replace("%1", target.getName()));
                return true;
            }
            if (togglelist.contains(target.getName()))
            {
                sender.sendMessage(getString("messages.targettoggle").replace("%1", target.getName()));
                return true;
            }
            if (ignorelist.containsKey(target.getName()))
            {
                if (ignorelist.get(target.getName()).contains(player.getName()))
                {
                    sender.sendMessage(getString("messages.targettoggle").replace("%1", target.getName()));
                    return true;
                }
            }
            requests.put(target, Arrays.asList(player.getName(), "0"));
            timeoutStart(target, player);
            sender.sendMessage(getString("messages.sent").replace("%1", target.getName()));
            target.sendMessage(getString("messages.received0").replace("%1", player.getName()));
            return true;
        }

        if (alias.equalsIgnoreCase("tpahere"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (args.length == 0) return false;
            Player player = (Player)sender;
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline())
            {
                sender.sendMessage(getString("messages.targetoffline").replace("%1", args[0]));
                return true;
            }
            if (requests.containsKey(target))
            {
                sender.sendMessage(getString("messages.targetpending").replace("%1", target.getName()));
                return true;
            }
            if (togglelist.contains(target.getName()))
            {
                sender.sendMessage(getString("messages.targettoggle").replace("%1", target.getName()));
                return true;
            }
            if (ignorelist.containsKey(target.getName()))
            {
                if (ignorelist.get(target.getName()).contains(player.getName()))
                {
                    sender.sendMessage(getString("messages.targettoggle").replace("%1", target.getName()));
                    return true;
                }
            }
            requests.put(target, Arrays.asList(player.getName(), "1"));
            timeoutStart(target, player);
            sender.sendMessage(getString("messages.sent").replace("%1", target.getName()));
            target.sendMessage(getString("messages.received1").replace("%1", player.getName()));
            return true;
        }

        if (alias.equalsIgnoreCase("tpaccept"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            Player target = (Player) sender;
            if (!requests.containsKey(target))
            {
                sender.sendMessage(getString("messages.norequests"));
                return true;
            }
            Player player = Bukkit.getPlayerExact(requests.get(target).get(0));
            if (player == null || !player.isOnline())
            {
                sender.sendMessage(getString("messages.senderoffline"));
                requests.remove(target);
                return true;
            }
            requests.remove(target);
            if (config.getBoolean("properties.tpdelay"))
            {
                Location loc0 = player.getLocation();
                Location loc1 = target.getLocation();
                sender.sendMessage(getString("messages.tpdelay").replace("%1", String.valueOf(config.getInt("properties.tpdelaytime"))));
                Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                    @Override
                    public void run() {
                        if (!requests.containsKey(target)) return;
                        if (requests.get(target).get(1).equalsIgnoreCase("0"))
                        {
                            if (!checkLocation(loc0, player.getLocation()) && config.getBoolean("properties.cancelonmove"))
                            {
                                player.sendMessage(getString("messages.canceled"));
                                target.sendMessage(getString("messages.canceled"));
                                return;
                            }
                        } else {
                            if (!checkLocation(loc0, target.getLocation()) && config.getBoolean("properties.cancelonmove"))
                            {
                                player.sendMessage(getString("messages.canceled"));
                                target.sendMessage(getString("messages.canceled"));
                                return;
                            }
                        }
                        teleport(target, player);
                        requests.remove(target);
                    }
                }, config.getInt("properties.tpdelaytime")*20);
            } else {
                teleport(target, player);
            }
        }

        if (alias.equalsIgnoreCase("tpdeny"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            Player target = (Player) sender;
            if (!requests.containsKey(target))
            {
                sender.sendMessage(getString("messages.norequests"));
                return true;
            }
            Player player = Bukkit.getPlayerExact(requests.get(target).get(0));
            player.sendMessage(getString("messages.declined0").replace("%1", target.getName()));
            target.sendMessage(getString("messages.declined1").replace("%1", player.getName()));
            requests.remove(target);
            return true;
        }

        if (alias.equalsIgnoreCase("tpatoggle"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            Player player = (Player) sender;
            if (togglelist != null && togglelist.contains(player.getName()))
            {
                togglelist.remove(player.getName());
                sender.sendMessage(getString("messages.toggle0"));
            } else {
                togglelist.add(player.getName());
                sender.sendMessage(getString("messages.toggle1"));
            }
            return true;
        }

        if (alias.equalsIgnoreCase("tpaignore"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            if (args.length == 0) return false;
            Player player = (Player)sender;
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline())
            {
                sender.sendMessage(getString("messages.targetoffline").replace("%1", args[0]));
                return true;
            }
            if (ignorelist.containsKey(player.getName()))
            {
                if (ignorelist.get(player.getName()).contains(target.getName()))
                {
                    List<String> ignored = ignorelist.get(player.getName());
                    ignored.remove(target.getName());
                    ignorelist.put(player.getName(), ignored);
                    sender.sendMessage(getString("messages.ignore1").replace("%1", target.getName()));
                    return true;
                } else {
                    List<String> ignored = ignorelist.get(player.getName());
                    ignored.add(target.getName());
                    ignorelist.put(player.getName(), ignored);
                    sender.sendMessage(getString("messages.ignore0").replace("%1", target.getName()));
                    return true;
                }
            } else {
                List<String> ignored = new ArrayList<>();
                ignored.add(target.getName());
                ignorelist.put(player.getName(), ignored);
                sender.sendMessage(getString("messages.ignore0").replace("%1", target.getName()));
                return true;
            }
        }
        return true;
    }
}