package de.redstoneworld.luckpermsnotifier;

import de.themoep.bungeeplugin.BungeePlugin;
import de.themoep.bungeeplugin.PluginCommand;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Group;
import me.lucko.luckperms.api.event.EventHandler;
import me.lucko.luckperms.api.event.user.track.UserTrackEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.IOException;
import java.util.Optional;

public final class LuckPermsNotifier extends BungeePlugin {
    
    private EventHandler<UserTrackEvent> trackEventHandler;
    
    @Override
    public void onEnable() {
        trackEventHandler = LuckPerms.getApi().getEventBus().subscribe(UserTrackEvent.class, e -> {
            if (hasLang("usertrackevent." + e.getAction() + ".target")) {
                ProxiedPlayer player = getProxy().getPlayer(e.getUser().getUuid());
                if (player != null) {
                    player.sendMessage(getLang("usertrackevent." + e.getAction() + ".target",
                            "target", e.getUser().getName(),
                            //"sender", e.getSender().getName(),
                            "group-to", e.getGroupTo().orElse(getLang("no-group")),
                            "group-to-display", getGroupName(e.getGroupTo().orElse("")).orElse(getLang("no-group")),
                            "group-from", e.getGroupFrom().orElse(getLang("no-group")),
                            "group-from-display", getGroupName(e.getGroupFrom().orElse("")).orElse(getLang("no-group")),
                            "track", e.getTrack().getName()));
                }
            }
            /*if (hasLang("usertrackevent." + e.getAction() + ".sender")) {
                ProxiedPlayer player = getProxy().getPlayer(e.getSender().getUuid());
                if (player != null) {
                    player.sendMessage(getLang("usertrackevent." + e.getAction() + ".sender",
                            "target", e.getUser().getName(),
                            "sender", e.getSender().getName(),
                            "group-to", e.getGroupTo().orElse(getLang("no-group")),
                            "group-to-display", getGroupName(e.getGroupTo().orElse("")).orElse(getLang("no-group")),
                            "group-from", e.getGroupFrom().orElse(getLang("no-group")),
                            "group-from-display", getGroupName(e.getGroupFrom().orElse("")).orElse(getLang("no-group")),
                            "track", e.getTrack().getName()));
                }
                
            }*/
            if (hasLang("usertrackevent." + e.getAction() + ".broadcast")) {
                String message = getLang("usertrackevent." + e.getAction() + ".broadcast",
                        "target", e.getUser().getName(),
                        //"sender", e.getSender().getName(),
                        "group-to", e.getGroupTo().orElse(getLang("no-group")),
                        "group-to-display", getGroupName(e.getGroupTo().orElse("")).orElse(getLang("no-group")),
                        "group-from", e.getGroupFrom().orElse(getLang("no-group")),
                        "group-from-display", getGroupName(e.getGroupFrom().orElse("")).orElse(getLang("no-group")),
                        "track", e.getTrack().getName());
                getProxy().getPlayers().forEach(p -> p.sendMessage(message));
            }
        });
        getProxy().getPluginManager().registerCommand(this, new PluginCommand<LuckPermsNotifier>(this, "luckpermsnotifier") {
            @Override
            protected boolean run(CommandSender sender, String[] args) {
                if (args.length > 0) {
                    if ("reload".equalsIgnoreCase(args[0])) {
                        try {
                            getConfig().loadConfig();
                            sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
                        } catch (IOException e) {
                            sender.sendMessage(ChatColor.RED + "Error while reloading the config! " + e.getMessage());
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }
    
    private String getLang(String key, String... replacements) {
        return translate(
                getConfig().getString(
                        "lang." + key.toLowerCase(),
                        ChatColor.RED + "Unknown language key " + ChatColor.WHITE + key.toLowerCase()
                ), replacements);
    }
    
    private boolean hasLang(String key) {
        return getConfig().isSet("lang." + key.toLowerCase(), true);
    }
    
    private Optional<String> getGroupName(String groupId) {
        Group group = LuckPerms.getApi().getGroup(groupId);
        if (group != null) {
            String groupName = group.getFriendlyName();
            int bracket = groupName.indexOf('(');
            if (bracket != -1 && groupName.endsWith(")")) {
                groupName = groupName.substring(bracket + 1, groupName.length() - 1);
            }
            return Optional.of(groupName);
        }
        return Optional.empty();
    }
    
    @Override
    public void onDisable() {
        trackEventHandler.unregister();
    }
}
