package com.joaootavios.crystolnetwork.essentials.systems.chats;

import com.crystolnetwork.offices.api.PlayerBase;
import com.crystolnetwork.offices.api.services.OfficesServices;
import com.crystolnetwork.offices.services.classlife.Singleton;
import com.joaootavios.crystolnetwork.essentials.EssentialsPlugin;
import com.joaootavios.crystolnetwork.essentials.utils.CooldownAPI;
import com.massivecraft.factions.entity.MPlayer;
import dev.walk.economy.Manager.AccountManager;
import dev.walk.economy.Manager.Magnata;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import rcore.specificutils.PlayerUtil;
import rcore.util.Sound;
import rcore.util.TXT;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class LocalChat implements Listener {

    private final int localChatRange = 64;
    private final CooldownAPI cooldownAPI = new CooldownAPI();
    private final boolean hasOfficePlugin = getServer().getPluginManager().getPlugin("CrystolOffices") != null;
    private final boolean hasEconomyPlugin = getServer().getPluginManager().getPlugin("CrystolEconomy") != null;
    private OfficesServices services;
    private PlayerBase playerBase;

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        final Player player = e.getPlayer();
        e.setCancelled(true);

        if (services == null && hasOfficePlugin){
            services = Singleton.getOrFill(OfficesServices.class);
            playerBase = services.getPlayerBase();
        }

        final String economy = (hasEconomyPlugin ? (new Magnata().isMagnata(new AccountManager(player.getUniqueId()).getInstance()) ? "<2>[$] " : "") : "&c[Economy not found]&r");
        final String cargo = (hasOfficePlugin ? playerBase.getUser(player).getLargestGroup().getPrefix() : "&c[Offices not found]&r");
        List<Player> recipients = new ArrayList<>();
        player.getNearbyEntities(localChatRange, localChatRange, localChatRange).forEach(en -> {
            if (en.getType() == EntityType.PLAYER && en != player) {
                recipients.add((Player) en);
            }
        });
        if (recipients.isEmpty()) {
            PlayerUtil.playSound(player, Sound.ENTITY_VILLAGER_NO);
            player.sendMessage(TXT.parse("&cNão há jogadores próximos para ler sua mensagem."));
            return;
        }
        if (cooldownAPI.getCooldownRemaining(player.getUniqueId(), "localchat") > 0) {
            PlayerUtil.playSound(player, Sound.ENTITY_VILLAGER_NO);
            player.sendMessage(TXT.parse("&cAguarde " + cooldownAPI.getCooldownRemainingVerb(player.getUniqueId(), "localchat") + " para falar no chat."));
            return;
        }

        if (EssentialsPlugin.config.getBoolean("compatible-with-factions") == true) {
            final MPlayer mp = MPlayer.get(player.getUniqueId());
            final String factionTag = mp.hasFaction() ? TXT.parse(" &7[&f" + mp.getFaction().getColor() + mp.getFactionTag() + "&7]") : TXT.parse("");
            final String factionRole = mp.hasFaction() ? TXT.parse("&7") + mp.getRole().getPrefix() : "";
            final String formatedMessage = TXT.parse("&e[L] &r"+ economy + factionTag + " " + cargo + " " + factionRole + player.getName() + " &f» &e" + e.getMessage());
            cooldownAPI.setCooldown(player.getUniqueId(), "localchat", 3L);
            recipients.add(player);
            recipients.forEach(a -> a.sendMessage(formatedMessage));
        } else {
            final String formatedMessage = TXT.parse("&e[L] &r"+ economy  + " " + cargo + " " + player.getName() + " &f» &e" + e.getMessage());
            cooldownAPI.setCooldown(player.getUniqueId(), "localchat", 3L);
            recipients.add(player);
            recipients.forEach(a -> a.sendMessage(formatedMessage));
        }
    }
}
