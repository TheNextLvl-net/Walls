package net.nonswag.fvr.walls.kits;


import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.ItemStackTools;
import net.nonswag.fvr.walls.api.Notifier;
import net.nonswag.fvr.walls.api.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;



public class SpecPlayerKit {

    private static BookMeta book;
    private static BookMeta vipbook;
    private final ItemMeta redWoolMeta, yellowWoolMeta, greenWoolMeta, blueWoolMeta;
    private final ItemMeta snowBallMeta;
    private final ItemMeta skullMeta;

    private final Walls walls;

    public SpecPlayerKit(Walls walls) {

        this.walls = walls;
        SpecPlayerKit.book = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);


        SpecPlayerKit.book.setPages(ChatColor.DARK_PURPLE + "Welcome to The Walls" + ChatColor.BLACK + "\n------------------\nThis is a PvP Game that consists of players having 15 minutes of peaceful mode where they should get resources and forge weapons. After those 15 minutes end, The Walls will drop and the battle begins!",
                "The last team standing wins! Keep an eye out for cool hidden treasures or watch out for harmful traps that can kill you! Have fun! Make sure to visit our website to see joinable servers and stats/forums:\n-\n" + ChatColor.BLUE + Walls.DISCORD, ChatColor.DARK_PURPLE + "RULES" + ChatColor.BLACK
                        + "\n--------------\n1- No swearing, flaming, ads, offensive skins\n2- No unapproved mods\n3- No exploiting\n4- Respect staff & each other\n5- No cheating\n6- Don't play against your team\n7- Have Fun!", ChatColor.DARK_GREEN + "Walls VIP Status" + ChatColor.BLACK
                        + "\n--------------\n- Spectating games with flight\n- Joining full servers.\n- Join full teams\n- /surface command\n-Teleport during peace time\n-Get random items and 5 levels of XP on start\n- Chat Prefix\n-Can /shout (3 times)\n-Cool forum tag/color, access to VIP forums.\n Buy it at\n" + ChatColor.BLUE + Walls.DISCORD);
        SpecPlayerKit.book.setAuthor("TheWalls");
        SpecPlayerKit.book.setTitle("Instructions");

        SpecPlayerKit.vipbook = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        SpecPlayerKit.vipbook.setPages(ChatColor.DARK_GREEN + "Walls VIP Status" + ChatColor.BLACK + "\n---------------\n- Spectating games with flight\n- Joining full servers.\n- Join full teams\n- /surface command\n-Teleport during peace time\n-Get random items and 5 levels of XP at start\n- Chat Prefix\n-Can /shout (3 times)\n-Cool forum tag/color, access to VIP forums. Buy it at\n" + ChatColor.BLUE
                + Walls.DISCORD);
        SpecPlayerKit.vipbook.setAuthor("My Walls");
        SpecPlayerKit.vipbook.setTitle("VIP");

        this.redWoolMeta = Bukkit.getItemFactory().getItemMeta(Material.WOOL);
        this.yellowWoolMeta = Bukkit.getItemFactory().getItemMeta(Material.WOOL);
        this.greenWoolMeta = Bukkit.getItemFactory().getItemMeta(Material.WOOL);
        this.blueWoolMeta = Bukkit.getItemFactory().getItemMeta(Material.WOOL);
        this.snowBallMeta = Bukkit.getItemFactory().getItemMeta(Material.SNOW_BALL);
        this.skullMeta = Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);

        this.redWoolMeta.setDisplayName(Walls.teamNames[1]);
        this.yellowWoolMeta.setDisplayName(Walls.teamNames[2]);
        this.greenWoolMeta.setDisplayName(Walls.teamNames[3]);
        this.blueWoolMeta.setDisplayName(Walls.teamNames[4]);
        this.snowBallMeta.setDisplayName(ChatColor.DARK_GREEN + "Hold THIS to fight! :)");
        this.skullMeta.setDisplayName("Click me to TP to players in game!");

    }


    public void givePlayerKit(Player player) {
        player.closeInventory();
        player.getInventory().clear();
        player.setFallDistance(0f);
        player.setFoodLevel(20);
        player.teleport(Walls.gameSpawn);
        ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK);
        newbook.setItemMeta(SpecPlayerKit.book);
        player.getInventory().setItem(0, newbook);
        switch (walls.getGameState()) {
            case PREGAME:
                ItemStack snowBall = new ItemStack(Material.SNOW_BALL);
                snowBall.setItemMeta(this.snowBallMeta);
                player.getInventory().setItem(8, ItemStackTools.enchantItem(snowBall, Enchantment.KNOCKBACK, 1));
                if (Walls.allowPickTeams) {
                    ItemStack red = new ItemStack(Material.WOOL, 1, (short) 14);
                    ItemStack yellow = new ItemStack(Material.WOOL, 1, (short) 4);
                    ItemStack green = new ItemStack(Material.WOOL, 1, (short) 5);
                    ItemStack blue = new ItemStack(Material.WOOL, 1, (short) 11);
                    red.setItemMeta(this.redWoolMeta);
                    yellow.setItemMeta(this.yellowWoolMeta);
                    green.setItemMeta(this.greenWoolMeta);
                    blue.setItemMeta(this.blueWoolMeta);
                    player.getInventory().setItem(2, red);
                    player.getInventory().setItem(3, yellow);
                    player.getInventory().setItem(4, green);
                    player.getInventory().setItem(5, blue);
                }
                Bukkit.getScheduler().runTaskLater(walls, () -> {
                    if (Walls.fullDiamond && Walls.UHC) {
                        TitleManager.sendTitle(player, "§9My UHC Stacked Walls", "§eRegen only with gold apples..");
                    } else if (Walls.diamondONLY && Walls.UHC) {
                        TitleManager.sendTitle(player, "§bMy UHC Diamond Walls", "§eRegen only with gold apples..");
                    } else if (Walls.diamondONLY) {
                        TitleManager.sendTitle(player, "§bMy Diamond Walls", "");
                    } else if (Walls.ironONLY) {
                        TitleManager.sendTitle(player, "§bMy Speed Walls", "");
                    } else if (Walls.UHC) {
                        TitleManager.sendTitle(player, "§cMy UHC Walls", "§eRegen only with gold apples..");
                    } else {
                        TitleManager.sendTitle(player, "§aMy Walls", "§e" + Walls.levelName);
                    }
                }, 20);
                Notifier.success(player, "Want to fly spectate /surface /spawn ? Get Walls " + ChatColor.GREEN + "VIP" + ChatColor.WHITE + " at " + Walls.DISCORD);
                break;
            case PEACETIME:
            case FIGHTING:
            case FINISHED:
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
                skull.setItemMeta(this.skullMeta);
                player.getInventory().addItem(skull);
                player.setAllowFlight(true);
                Notifier.success(player, "Your rank allows you to fly around and spectate invisibly!");
                Notifier.success(player, "Click the head in your hotbar to tp to players :)");
                break;
            default:
                break;
        }
    }
}
