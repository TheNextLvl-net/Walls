package me.glennEboy.Walls.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import me.glennEboy.Walls.TheWalls;
import me.glennEboy.Walls.utils.GameNotifications;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class KitCmd implements CommandExecutor{
	
    public enum Type {
        FREE,
        PAID,
        VOTE;
    }
    public class Kit {
        private final List<ItemStack> items;

        private Kit(Item... items) {
            this.items = new ArrayList<ItemStack>();
            for (final Item item : items) {
                this.items.add(item.getItem());
            }
        }

        public List<ItemStack> getItems() {
            return new ArrayList<ItemStack>(this.items);
        }

    }

    private class Item {

        private ItemStack stack;

        public Item(Material type) {
            this(type, 1);
        }

        public Item(Material type, int amount) {
            this.stack = new ItemStack(type, amount);
        }

        public Item egg(short type) {
//            public Item egg(EntityType type) {
//          this.stack = new ItemStack(Material.MONSTER_EGG, this.stack.getAmount(), (short) 95);
          this.stack = new ItemStack(Material.MONSTER_EGG, this.stack.getAmount(), type);
        	
            return this;
        }

        public Item enchant(Enchantment enchantment, int level) {
            final ItemMeta meta = this.stack.getItemMeta();
            meta.addEnchant(enchantment, level, true);
            this.stack.setItemMeta(meta);
            return this;
        }

        public ItemStack getItem() {
            return this.stack;
        }

        public Item lore(String... lore) {
            final ItemMeta meta = this.stack.getItemMeta();
            meta.setLore(Arrays.asList(lore));
            this.stack.setItemMeta(meta);
            return this;
        }

        public Item name(String name) {
            final ItemMeta meta = this.stack.getItemMeta();
            meta.setDisplayName(name);
            this.stack.setItemMeta(meta);
            return this;
        }

        public Item potion(PotionType potionType, int level) {
            final ItemMeta meta = this.stack.getItemMeta();
            if (!(meta instanceof PotionMeta)) {
                return this;
            }
            final Potion potion = new Potion(potionType, level).splash();
            potion.apply(this.stack);
            return this;
        }

        public Item uses(int uses) {
            this.stack.setDurability((short) ((this.stack.getType().getMaxDurability() - uses) + 1));
            return this;
        }
    }

	TheWalls myWalls;
    private final Set<UUID> used = new HashSet<UUID>();
//    private final Map<UUID, Map<UUID, List<ItemStack>>> users = new ConcurrentHashMap<UUID, Map<UUID, List<ItemStack>>>();

    public final Map<Type, Map<String, List<ItemStack>>> kits = new ConcurrentHashMap<Type, Map<String, List<ItemStack>>>();;
    
    public final Map<String, List<ItemStack>> free = new ConcurrentHashMap<String, List<ItemStack>>();

    public final Map<String, List<ItemStack>> vote = new ConcurrentHashMap<String, List<ItemStack>>();
    public final Map<String, List<ItemStack>> paid = new ConcurrentHashMap<String, List<ItemStack>>();

    
	public KitCmd(TheWalls tw){
		myWalls=tw;
        
        free.put("archer", new Kit(new Item(Material.BOW).name("archer-bow"), new Item(Material.ARROW, 32)).getItems());
        free.put("grandpa", new Kit(new Item(Material.STICK).name("grandpa-cane").lore(new String[] { ChatColor.RED + "get off my lawn" }).enchant(Enchantment.KNOCKBACK, 3)).getItems());
        free.put("stark", new Kit(new Item(Material.MONSTER_EGG, 2).egg((short) 95).lore(new String[] { "winter is coming!" }), new Item(Material.BONE, 8), new Item(Material.PORK, 5)).getItems());

        free.put("boom", new Kit(new Item(Material.STONE_PLATE, 5).name("land").lore("BOOOM")).getItems());
        free.put("fisherman", new Kit(new Item(Material.FISHING_ROD).enchant(Enchantment.getById(61), 2).enchant(Enchantment.getById(62), 2).name(ChatColor.AQUA + "manager.rod")).getItems());
        free.put("chef", new Kit(new Item(Material.CARROT_ITEM, 2), new Item(Material.COOKED_BEEF, 32)).getItems());
        this.kits.put(Type.FREE, free);

        paid.put("caveman", new Kit(new Item(Material.TORCH, 32), new Item(Material.COAL, 8), new Item(Material.IRON_PICKAXE).name(ChatColor.DARK_AQUA + "grunt")).getItems());
        paid.put("endermage", new Kit(new Item(Material.ENDER_PEARL, 5).name("escape")).getItems());
        paid.put("warrior", new Kit(new Item(Material.IRON_CHESTPLATE).enchant(Enchantment.PROTECTION_PROJECTILE, 1)).getItems());
        paid.put("pyroarcher", new Kit(new Item(Material.BOW).uses(8).enchant(Enchantment.ARROW_FIRE, 1).name("flaming"), new Item(Material.ARROW, 8)).getItems());
        paid.put("assassin", new Kit(new Item(Material.POTION).potion(PotionType.INVISIBILITY, 1), new Item(Material.COMPASS).name(ChatColor.ITALIC + "compass")).getItems());
        paid.put("chemist", new Kit(new Item(Material.POTION, 2).potion(PotionType.REGEN, 1), new Item(Material.POTION).potion(PotionType.POISON, 1), new Item(Material.POTION).potion(PotionType.INSTANT_DAMAGE, 2)).getItems());
        paid.put("leprechaun", new Kit(new Item(Material.POTATO_ITEM).name("luck").lore("Luck O' the Irish").enchant(Enchantment.LOOT_BONUS_MOBS, 1)).getItems());

        paid.put("thor", new Kit(new Item(Material.GOLD_AXE).name("Thors Hammer").lore("Weapon of the Gods!").uses(3).enchant(Enchantment.ARROW_DAMAGE, 2), new Item(Material.ARROW, 3)).getItems());

        paid.put("grandma", new Kit(new Item(Material.BOW).name("get off my lawn!").uses(4).enchant(Enchantment.getById(49), 1), new Item(Material.ARROW, 4)).getItems());
        paid.put("runner", new Kit(new Item(Material.IRON_BOOTS).name("zoooom!").enchant(Enchantment.PROTECTION_FALL, 3), new Item(Material.POTION).potion(PotionType.SPEED, 2)).getItems());
        paid.put("healer", new Kit(new Item(Material.POTION, 3).potion(PotionType.INSTANT_HEAL, 2)).getItems());
        paid.put("demoman", new Kit(new Item(Material.TNT, 16), new Item(Material.REDSTONE, 32), new Item(Material.STONE_BUTTON, 2)).getItems());
        paid.put("builder", new Kit(new Item(Material.QUARTZ_BLOCK, 64), new Item(Material.LOG, 64), new Item(Material.GLASS, 32)).getItems());
        paid.put("snowman", new Kit(new Item(Material.SNOW_BLOCK, 4), new Item(Material.PUMPKIN, 2), new Item(Material.DIAMOND_SPADE)).getItems());
        paid.put("firefighter", new Kit(new Item(Material.WATER_BUCKET, 2), new Item(Material.FLINT_AND_STEEL, 1), new Item(Material.POTION).potion(PotionType.FIRE_RESISTANCE, 1)).getItems());
        paid.put("trader", new Kit(new Item(Material.MONSTER_EGG, 2).egg((short) 120), new Item(Material.EMERALD, 18)).getItems());

        this.kits.put(Type.PAID, paid);

        vote.put("manager.voter", new Kit(new Item(Material.GOLD_AXE), new Item(Material.GOLDEN_APPLE)).getItems());
        this.kits.put(Type.VOTE, vote);
		
	}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final Player player = (Player) sender;

        if (myWalls.isSpec(player.getUniqueId())){
        	GameNotifications.sendPlayerCommandError(player, "Specs can fly! You dont need a kit :)");
            return true;
        }
    	switch (myWalls.getGameState()){
    	case PREGAME:
    		break;
    	case PEACETIME:
	        if (args.length == 0) {
	        	GameNotifications.sendPlayerCommandError(player, "You need to say which kit :) /kit <kitname>");
	            break;
	        }
	        final String choice = args[0].toLowerCase();
	        this.playerChoice(player, choice);
    		break;
    	case FIGHTING:
    	case FINISHED:
    		GameNotifications.sendPlayerCommandError(player, "Kits only availble during Peace time :)"); 
    		break;
    	default:
    		break;
    	}

    	return true;
    }
    
    
    @SuppressWarnings("deprecation")
	public void playerChoice(Player player, String choice) {
    	
    	
    	
        if (this.used.contains(player.getUniqueId()) && !player.isOp()) {
        	
        	GameNotifications.sendPlayerCommandError(player, "Seems like you already have a kit :)");
        	

        	
        } else {
        	


        	if (this.free.containsKey(choice)){
        		player.getInventory().addItem(this.free.get(choice).toArray((new ItemStack[this.free.get(choice).size()])));
        		GameNotifications.sendPlayerCommandError(player, "Enjoy FREE kit " + choice);
        		this.used.add(player.getUniqueId());
        	}else if (this.paid.containsKey(choice)){
        		
        		if (myWalls.isPRO(player.getUniqueId())){        			
        			if (choice.equalsIgnoreCase("thor")){
        				myWalls.thorOwners.put(player.getUniqueId(), 3);
        			}else if (choice.equalsIgnoreCase("leprechaun")){
        				myWalls.leprechaunOwners.put(player.getUniqueId(), 3);
        			}
        			player.getInventory().addItem(this.paid.get(choice).toArray((new ItemStack[this.paid.get(choice).size()])));
        			GameNotifications.sendPlayerCommandError(player, "Enjoy PRO kit " + choice);
        			this.used.add(player.getUniqueId());
        		}else if (myWalls.getWallsPlayer(player.getUniqueId()).paidKits!=null && myWalls.getWallsPlayer(player.getUniqueId()).paidKits.indexOf(choice)>-1){
        			// they have paid for this kit
        			if (choice.equalsIgnoreCase("thor")){
        				myWalls.thorOwners.put(player.getUniqueId(), 3);
        			}else if (choice.equalsIgnoreCase("leprechaun")){
        				myWalls.leprechaunOwners.put(player.getUniqueId(), 3);
        			}
        			player.getInventory().addItem(this.paid.get(choice).toArray((new ItemStack[this.paid.get(choice).size()])));
        			GameNotifications.sendPlayerCommandError(player, "Enjoy PRO kit " + choice);
        			this.used.add(player.getUniqueId());
        			
        		}else{
        			GameNotifications.sendPlayerCommandError(player, "This is a PRO kit - you need to upgrade to get this one :-/");
        		}
        	}else{
        		GameNotifications.sendPlayerCommandError(player, "No luck - kit " + choice + " does not exist :(");
        		
        	}
        	player.updateInventory();
            
        }
    }

}
