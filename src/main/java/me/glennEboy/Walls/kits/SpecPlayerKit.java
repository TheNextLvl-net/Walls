package me.glennEboy.Walls.kits;


import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.utils.GameNotifications;
import me.glennEboy.Walls.utils.ItemStackTools;
import me.glennEboy.Walls.utils.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;



public class SpecPlayerKit {
    
    private static BookMeta book;
    private static BookMeta vipbook;
    private final ItemMeta team1WoolMeta, team2WoolMeta, team3WoolMeta, team4WoolMeta;
    private final ItemMeta snowForce;
    private final ItemMeta skull;

    private TheWalls myWalls;
    
    public SpecPlayerKit(TheWalls wallsPlugin){
        
        myWalls = wallsPlugin;
        SpecPlayerKit.book = (BookMeta) Bukkit.getServer().getItemFactory().getItemMeta(Material.WRITTEN_BOOK);

        
        SpecPlayerKit.book.setPages(ChatColor.DARK_PURPLE + "Welcome to The Walls" + ChatColor.BLACK + "\n------------------\nThis is a PvP Game that consists of players having 15 minutes of peaceful mode where they should get resources and forge weapons. After those 15 minutes end, The Walls will drop and the battle begins!",
                "The last team standing wins! Keep an eye out for cool hidden treasures or watch out for harmful traps that can kill you! Have fun! Make sure to visit our website to see joinable servers and stats/forums:\n-\n" + ChatColor.BLUE + "mysite.COM", ChatColor.DARK_PURPLE + "RULES" + ChatColor.BLACK
                        + "\n--------------\n1- No swearing, flaming, ads, offensive skins\n2- No unapproved mods\n3- No exploiting\n4- Respect staff & each other\n5- No cheating\n6- Don't play against your team\n7- Have Fun!", ChatColor.DARK_GREEN + "Walls VIP Status" + ChatColor.BLACK
                        + "\n--------------\n- Spectating games with flight\n- Joining full servers.\n- Join full teams\n- /surface command\n-Teleport during peace time\n-Get random items and 5 levels of XP on start\n- Chat Prefix\n-Can /shout (3 times)\n-Cool forum tag/color, access to VIP forums.\n Buy it at\n" + ChatColor.BLUE + "mysite.COM");
        SpecPlayerKit.book.setAuthor("TheWalls");
        SpecPlayerKit.book.setTitle("Instructions");

        SpecPlayerKit.vipbook = (BookMeta) Bukkit.getServer().getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        SpecPlayerKit.vipbook.setPages(ChatColor.DARK_GREEN + "Walls VIP Status" + ChatColor.BLACK + "\n---------------\n- Spectating games with flight\n- Joining full servers.\n- Join full teams\n- /surface command\n-Teleport during peace time\n-Get random items and 5 levels of XP at start\n- Chat Prefix\n-Can /shout (3 times)\n-Cool forum tag/color, access to VIP forums. Buy it at\n" + ChatColor.BLUE
                + "mysite.COM");
        SpecPlayerKit.vipbook.setAuthor("My Walls");
        SpecPlayerKit.vipbook.setTitle("VIP");

        this.team1WoolMeta = Bukkit.getServer().getItemFactory().getItemMeta(Material.WOOL);
        this.team1WoolMeta.setDisplayName(TheWalls.teamsNames[1]);
        this.team2WoolMeta = Bukkit.getServer().getItemFactory().getItemMeta(Material.WOOL);
        this.team2WoolMeta.setDisplayName(TheWalls.teamsNames[2]);
        this.team3WoolMeta = Bukkit.getServer().getItemFactory().getItemMeta(Material.WOOL);
        this.team3WoolMeta.setDisplayName(TheWalls.teamsNames[3]);
        this.team4WoolMeta = Bukkit.getServer().getItemFactory().getItemMeta(Material.WOOL);
        this.team4WoolMeta.setDisplayName(TheWalls.teamsNames[4]);
        
        this.snowForce = Bukkit.getServer().getItemFactory().getItemMeta(Material.SNOW_BALL);
        this.snowForce.setDisplayName(ChatColor.DARK_GREEN+"Hold THIS to fight! :)");
        
        this.skull = Bukkit.getServer().getItemFactory().getItemMeta(Material.SKULL_ITEM);
        this.skull.setDisplayName("Click me to TP to players in game!");
        
    }
    

    
    @SuppressWarnings("deprecation")
    public void givePlayerKit(final Player p) {


        p.closeInventory();
        p.getInventory().clear();
        p.setFallDistance(0f);
        p.setFoodLevel(20);
        p.teleport(TheWalls.gameSpawn);
        
        final ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
        newbook.setItemMeta(SpecPlayerKit.book);
        p.getInventory().setItem(0, newbook);


        switch (myWalls.getGameState()){
        case PREGAME:
            
            final ItemStack snowBawz = new ItemStack(Material.SNOW_BALL, 1);
            snowBawz.setItemMeta(this.snowForce);

            p.getInventory().setItem(8, ItemStackTools.enchantItem(snowBawz,Enchantment.KNOCKBACK,1));

            if (TheWalls.allowPickTeams){
                
                final ItemStack team1Wool = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
                team1Wool.setItemMeta(this.team1WoolMeta);
                
                p.getInventory().setItem(2, team1Wool);
                
                final ItemStack team2Wool = new ItemStack(Material.WOOL, 1, DyeColor.YELLOW.getData());
                team2Wool.setItemMeta(this.team2WoolMeta);
                
                p.getInventory().setItem(3, team2Wool);
                
                final ItemStack team3Wool = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData());
                team3Wool.setItemMeta(this.team3WoolMeta);
                
                p.getInventory().setItem(4, team3Wool);
                
                final ItemStack team4Wool = new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData());
	        	team4Wool.setItemMeta(this.team4WoolMeta);
	        	
	        	p.getInventory().setItem(5, team4Wool);
	        }

	        
            myWalls.getServer().getScheduler().runTaskLater(myWalls, () -> {
                if (TheWalls.fullDiamond && TheWalls.UHC) {

                    TitleManager.sendTitle(p, "§9My UHC Stacked Walls", "§eRegen only with gold apples..");

                } else if (TheWalls.diamondONLY && TheWalls.UHC) {

                    TitleManager.sendTitle(p, "§bMy UHC Diamond Walls", "§eRegen only with gold apples..");

                } else if (TheWalls.diamondONLY) {

                    TitleManager.sendTitle(p, "§bMy Diamond Walls", "");

                } else if (TheWalls.ironONLY) {

                    TitleManager.sendTitle(p, "§bMy Speed Walls", "");

                } else if (TheWalls.UHC) {

                    TitleManager.sendTitle(p, "§cMy UHC Walls", "§eRegen only with gold apples..");

                } else {

                    TitleManager.sendTitle(p, "§eMy Walls", "");

                }
            }, 2 * 20);

	        GameNotifications.sendPlayerCommandSuccess(p, "Pick a team --> /team 1-4  If you don't pick, you will be randomly assigned to one.");
	        GameNotifications.sendPlayerCommandSuccess(p, "Want to fly spectate /surface /spawn ? Get Walls " + ChatColor.GREEN + "VIP" + ChatColor.WHITE + " at Mysite.COM");

			break;
		case PEACETIME:
		case FIGHTING:			
		case FINISHED:

	        final ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
	        skull.setItemMeta(this.skull);
	        p.getInventory().addItem(skull);
	        p.setAllowFlight(true);
	        GameNotifications.sendPlayerCommandSuccess(p, "Your rank allows you to fly around and spectate invisibly!");
			GameNotifications.sendPlayerCommandSuccess(p, "Click the head in your hotbar to tp to players :)");
			break;
		default:
			break;
		}
		
	}

}
