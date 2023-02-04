package net.nonswag.fvr.walls.utils;

import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.Walls.WallsPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;

public class DeathMessages {
    public static final Map<String, String> deathMessages = new HashMap<>();

    public static void initializeDeathMessages() {
        DeathMessages.deathMessages.put("messages.burn", "&9<killed>&7 from <team>&7 burnt to crisp!");
        DeathMessages.deathMessages.put("messages.lava", "&9<killed>&7 from <team>&7 felt they were so hawt they could swim in lava.. they were wrong.");
        DeathMessages.deathMessages.put("messages.cactus", "&9<killed>&7 from <team>&7 ran into a cactus and couldn't stop!");
        DeathMessages.deathMessages.put("messages.drowning", "&9<killed>&7 from <team>&7 could not breath under water :( - Drowned!");
        DeathMessages.deathMessages.put("messages.explosion", "&9<killed>&7 from <team>&7 went kabooom!");
        DeathMessages.deathMessages.put("messages.fall", "&9<killed>&7 from <team>&7 fell to their death!");
        DeathMessages.deathMessages.put("messages.lightning", "&9<killed>&7 from <team>&7 was struck by lightning!");
        DeathMessages.deathMessages.put("messages.poison", "&9<killed>&7 from <team>&7 was poisoned to death!");
        DeathMessages.deathMessages.put("messages.potion", "&9<killed>&7 from <team>&7 was killed by a potion!");
        DeathMessages.deathMessages.put("messages.starvation", "&9<killed>&7 from <team>&7 starved to death!");
        DeathMessages.deathMessages.put("messages.suffocation", "&9<killed>&7 from <team>&7 suffocated!");
        DeathMessages.deathMessages.put("messages.suicide", "&9<killed>&7 from <team>&7 killed themselves!");
        DeathMessages.deathMessages.put("messages.void", "&9<killed>&7 from <team>&7 fell out of the world");
        DeathMessages.deathMessages.put("messages.anvil", "&9<killed>&7 from <team>&7 failed to avoid somthing heavy falling out of the sky and got squished!");
        DeathMessages.deathMessages.put("messages.mob-attack", "&9<killed>&7 from <team>&7 was murdered by a <killer>!");
        DeathMessages.deathMessages.put("messages.skeleton", "&9<killed>&7 from <team>&7 ran through a minefield of aimbot skeletons and lost!");
        DeathMessages.deathMessages.put("messages.pvp", "&9<killed>&7 from <team>&7 was slain by &9<killer>&7 from <killerteam>&7 wielding <wielding>!");
        DeathMessages.deathMessages.put("messages.projectile", "&9<killed>&7 from <team>&7 was shot to death from <blocks> blocks away by &9<killer>&7 from <killerteam>&7!");
        DeathMessages.deathMessages.put("messages.unknown", "&9<killed>&7 died");
    }

    public enum DeathCause {
        BURN(DamageCause.FIRE, DamageCause.FIRE_TICK), LAVA(DamageCause.LAVA), CACTUS(DamageCause.CONTACT), DROWNING(DamageCause.DROWNING),
        EXPLOSION(DamageCause.BLOCK_EXPLOSION, DamageCause.ENTITY_EXPLOSION), FALL(DamageCause.FALL), LIGHTNING(DamageCause.LIGHTNING),
        MOB_ATTACK(DamageCause.ENTITY_ATTACK), POISON(DamageCause.POISON), POTION(DamageCause.MAGIC), PROJECTILE(DamageCause.PROJECTILE),
        PVP(DamageCause.ENTITY_ATTACK), STARVATION(DamageCause.STARVATION), SUFFOCATION(DamageCause.SUFFOCATION), SUICIDE(DamageCause.SUICIDE), VOID(DamageCause.VOID),
        FALLING_BLOCK(DamageCause.FALLING_BLOCK),
        UNKNOWN;

        private final Set<DamageCause> damageCauses = new HashSet<>();

        DeathCause(DamageCause... causes) {
            Collections.addAll(this.damageCauses, causes);
        }

        private static DeathCause getByDamageCause(DamageCause cause) {
            for (DeathCause deathCause : DeathCause.values()) {
                if (deathCause.damageCauses.contains(cause)) return deathCause;
            }
            return UNKNOWN;
        }
    }

    public static void getDeathMessage(PlayerDeathEvent event, Walls myWalls) {
        final EntityDamageEvent ev = event.getEntity().getLastDamageCause();

        if (ev == null) {
            Notifier.broadcast(event.getEntity().getName() + " died suspiciously! O_o");
        }

        Player player = event.getEntity();
        WallsPlayer deadPlayer = myWalls.getWallsPlayer(player.getUniqueId());

        DamageCause damageCause = player.getLastDamageCause().getCause();
        DeathCause deathCause = DeathCause.getByDamageCause(damageCause);

        String message = DeathMessages.deathMessages.get("messages." + deathCause.name().toLowerCase().replace('_', '-'));

        Player killer = null;
        WallsPlayer wallsKiller = null;

        switch (deathCause) {
            case MOB_ATTACK:
            case PVP:
                killer = player.getKiller();
                if (killer != null) {
                    wallsKiller = myWalls.getWallsPlayer(killer.getUniqueId());
                    StringBuilder wielding = new StringBuilder("Unknown");
                    ItemStack killedByItem = null;
                    if (killer.getItemInHand() != null) {
                        final String[] itemName = killer.getItemInHand().getType().name().split("_");
                        killedByItem = killer.getItemInHand();
                        wielding = new StringBuilder(itemName[0].replace("AIR", "Fists").toLowerCase());
                        wielding = new StringBuilder(wielding.substring(0, 1).toUpperCase() + wielding.substring(1));
                        for (int i = 1; i < itemName.length; i++) {
                            wielding.append(" ").append(itemName[i].substring(0, 1).concat(itemName[i].substring(1).toLowerCase()));
                        }
                    }
                    message = DeathMessages.deathMessages.get("messages.pvp").replace("<killer>", Walls.teamChatColors[wallsKiller.playerState.ordinal()]
                            + killer.getName()).replace("<wielding>", wielding.toString()).replace("<killerteam>", Walls.teamsNames[wallsKiller.playerState.ordinal()]);
                    message = message.replace("<killed>", Walls.teamChatColors[deadPlayer.playerState.ordinal()]
                            + player.getName()).replace("<team>", Walls.teamsNames[deadPlayer.playerState.ordinal()]);
                    if (killedByItem != null && killedByItem.getType() != Material.AIR) {
                        Hologram hologram = HolographicDisplaysAPI.get(myWalls).createHologram(player.getLocation().add(0, 2.2, 0));
                        hologram.getLines().appendItem(killedByItem);
                        hologram.getLines().appendText(Walls.teamChatColors[deadPlayer.playerState.ordinal()] + player.getDisplayName());
                        hologram.getLines().appendText(ChatColor.WHITE + " died here at the hand of ");
                        hologram.getLines().appendText(Walls.teamChatColors[wallsKiller.playerState.ordinal()] + killer.getDisplayName());
                    }
                } else {
                    if (ev instanceof EntityDamageByEntityEvent) {
                        Entity killerMob = ((EntityDamageByEntityEvent) ev).getDamager();
                        message = message.replace("<killer>", killerMob.getType().name().toLowerCase().replace('_', ' '));
                    }
                    message = message.replace("<killed>", Walls.teamChatColors[deadPlayer.playerState.ordinal()]
                            + player.getName()).replace("<team>", Walls.teamsNames[deadPlayer.playerState.ordinal()]);
                }
                break;
            case PROJECTILE:
                if (player.getKiller() != null) {
                    killer = player.getKiller();
                    wallsKiller = myWalls.getWallsPlayer(killer.getUniqueId());
                    message = message.replace("<killer>", Walls.teamChatColors[wallsKiller.playerState.ordinal()]
                            + killer.getName()).replace("<killerteam>", Walls.teamsNames[wallsKiller.playerState.ordinal()]);
                    message = message.replace("<killed>", Walls.teamChatColors[deadPlayer.playerState.ordinal()]
                            + player.getName()).replace("<team>", Walls.teamsNames[deadPlayer.playerState.ordinal()]);
                    double distance = killer.getLocation().distance(event.getEntity().getLocation());
                    message = message.replace("<blocks>", String.valueOf((int) distance));

                } else {
                    if (ev instanceof EntityDamageByEntityEvent) {
                        Entity killerMob = ((EntityDamageByEntityEvent) ev).getDamager();
                        final ProjectileSource shooter = ((Projectile) killerMob).getShooter();
                        if (shooter instanceof Skeleton) {
                            message = DeathMessages.deathMessages.get("messages.skeleton");
                            message = message.replace("<killed>", Walls.teamChatColors[deadPlayer.playerState.ordinal()]
                                    + player.getName()).replace("<team>", Walls.teamsNames[deadPlayer.playerState.ordinal()]);
                        }
                    }
                }
                break;
            case BURN:
            case LAVA:
            case CACTUS:
            case DROWNING:
            case EXPLOSION:
            case FALL:
            case LIGHTNING:
            case POISON:
            case POTION:
            case STARVATION:
            case SUFFOCATION:
            case SUICIDE:
            case VOID:
            case FALLING_BLOCK:
            case UNKNOWN:
                break;
            default:
                message = null;
                break;
        }
        if (deadPlayer.playerState != null) {
            message = message.replace("<killed>", Walls.teamChatColors[deadPlayer.playerState.ordinal()]
                    + player.getName()).replace("<team>", Walls.teamsNames[deadPlayer.playerState.ordinal()]);
        } else message = deadPlayer.username + " died suspiciously! O_o";
        Notifier.broadcast(ChatColor.translateAlternateColorCodes('&', message));
        if (wallsKiller != null) {
            int coinsEarned = (Walls.coinsKillReward * (myWalls.getCoinKillMultiplier(event.getEntity().getKiller().getUniqueId())));
            Notifier.success(killer, String.format("You killed %s and gained " + coinsEarned + " coin!", player.getName()));
            wallsKiller.kills = wallsKiller.kills + 1;
            myWalls.getAllPlayers().put(killer.getUniqueId(), wallsKiller);
        }
    }
}
