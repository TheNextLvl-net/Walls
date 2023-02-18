package net.nonswag.fvr.walls.commands;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.nonswag.fvr.walls.Walls;
import net.nonswag.fvr.walls.api.Notifier;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class KitCommand implements CommandExecutor {
    private static final Set<UUID> used = new HashSet<>();
    public static final Map<Type, Map<String, List<ItemStack>>> kits = new ConcurrentHashMap<>();
    public static final Map<String, List<ItemStack>> free = new ConcurrentHashMap<>();
    public static final Map<String, List<ItemStack>> vote = new ConcurrentHashMap<>();
    public static final Map<String, List<ItemStack>> paid = new ConcurrentHashMap<>();

    private final Walls walls;

    static {
        free.put("archer", new Kit(new Item(Material.BOW).name("archer-bow"), new Item(Material.ARROW, 32)).getItems());
        free.put("grandpa", new Kit(new Item(Material.STICK).name("grandpa-cane").lore(ChatColor.RED + "get off my lawn").enchant(Enchantment.KNOCKBACK, 3)).getItems());
        free.put("stark", new Kit(new Item(Material.MONSTER_EGG, 2).egg((short) 95).lore("winter is coming!"), new Item(Material.BONE, 8), new Item(Material.PORK, 5)).getItems());

        free.put("boom", new Kit(new Item(Material.STONE_PLATE, 5).name("land").lore("BOOOM")).getItems());
        free.put("fisherman", new Kit(new Item(Material.FISHING_ROD).enchant(Enchantment.LUCK, 2).enchant(Enchantment.LURE, 2).name(ChatColor.AQUA + "manager.rod")).getItems());
        free.put("chef", new Kit(new Item(Material.CARROT_ITEM, 2), new Item(Material.COOKED_BEEF, 32)).getItems());
        kits.put(Type.FREE, free);

        paid.put("caveman", new Kit(new Item(Material.TORCH, 32), new Item(Material.COAL, 8), new Item(Material.IRON_PICKAXE).name(ChatColor.DARK_AQUA + "grunt")).getItems());
        paid.put("endermage", new Kit(new Item(Material.ENDER_PEARL, 5).name("escape")).getItems());
        paid.put("warrior", new Kit(new Item(Material.IRON_CHESTPLATE).enchant(Enchantment.PROTECTION_PROJECTILE, 1)).getItems());
        paid.put("pyroarcher", new Kit(new Item(Material.BOW).uses(8).enchant(Enchantment.ARROW_FIRE, 1).name("flaming"), new Item(Material.ARROW, 8)).getItems());
        paid.put("assassin", new Kit(new Item(Material.POTION).potion(PotionType.INVISIBILITY, 1), new Item(Material.COMPASS).name(ChatColor.ITALIC + "compass")).getItems());
        paid.put("chemist", new Kit(new Item(Material.POTION, 2).potion(PotionType.REGEN, 1), new Item(Material.POTION).potion(PotionType.POISON, 1), new Item(Material.POTION).potion(PotionType.INSTANT_DAMAGE, 2)).getItems());
        paid.put("leprechaun", new Kit(new Item(Material.POTATO_ITEM).name("luck").lore("Luck O' the Irish").enchant(Enchantment.LOOT_BONUS_MOBS, 1)).getItems());

        paid.put("thor", new Kit(new Item(Material.GOLD_AXE).name("Thors Hammer").lore("Weapon of the Gods!").uses(3).enchant(Enchantment.ARROW_DAMAGE, 2), new Item(Material.ARROW, 3)).getItems());

        paid.put("grandma", new Kit(new Item(Material.BOW).name("get off my lawn!").uses(4).enchant(Enchantment.ARROW_KNOCKBACK, 1), new Item(Material.ARROW, 4)).getItems());
        paid.put("runner", new Kit(new Item(Material.IRON_BOOTS).name("zoooom!").enchant(Enchantment.PROTECTION_FALL, 3), new Item(Material.POTION).potion(PotionType.SPEED, 2)).getItems());
        paid.put("healer", new Kit(new Item(Material.POTION, 3).potion(PotionType.INSTANT_HEAL, 2)).getItems());
        paid.put("demoman", new Kit(new Item(Material.TNT, 16), new Item(Material.REDSTONE, 32), new Item(Material.STONE_BUTTON, 2)).getItems());
        paid.put("builder", new Kit(new Item(Material.QUARTZ_BLOCK, 64), new Item(Material.LOG, 64), new Item(Material.GLASS, 32)).getItems());
        paid.put("snowman", new Kit(new Item(Material.SNOW_BLOCK, 4), new Item(Material.PUMPKIN, 2), new Item(Material.DIAMOND_SPADE)).getItems());
        paid.put("firefighter", new Kit(new Item(Material.WATER_BUCKET, 2), new Item(Material.FLINT_AND_STEEL, 1), new Item(Material.POTION).potion(PotionType.FIRE_RESISTANCE, 1)).getItems());
        paid.put("trader", new Kit(new Item(Material.MONSTER_EGG, 2).egg((short) 120), new Item(Material.EMERALD, 18)).getItems());

        kits.put(Type.PAID, paid);

        vote.put("manager.voter", new Kit(new Item(Material.GOLD_AXE), new Item(Material.GOLDEN_APPLE)).getItems());
        kits.put(Type.VOTE, vote);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (walls.isSpectator(player)) {
                Notifier.error(player, "Specs can fly! You don't need a kit :)");
                return true;
            }
            if (walls.getGameState() == Walls.GameState.PEACETIME) {
                if (args.length == 0) {
                    Notifier.error(player, "You need to say which kit :) /kit <kitname>");
                    return true;
                }
                final String choice = args[0].toLowerCase();
                playerChoice(walls, player, choice);
            } else Notifier.error(player, "Kits are only available during Peace time :)");
        } else Notifier.error(sender, "This is a player command");
        return true;
    }

    public static void playerChoice(Walls walls, Player player, String choice) {
        if (!used.contains(player.getUniqueId())) {
            if (free.containsKey(choice)) {
                player.getInventory().addItem(free.get(choice).toArray(new ItemStack[]{}));
                Notifier.error(player, "Enjoy FREE kit " + choice);
                used.add(player.getUniqueId());
            } else if (paid.containsKey(choice)) {
                if (walls.getPlayer(player).getRank().pro()) {
                    if (choice.equalsIgnoreCase("thor")) {
                        walls.thorOwners.put(player.getUniqueId(), 3);
                    } else if (choice.equalsIgnoreCase("leprechaun")) {
                        walls.leprechaunOwners.put(player.getUniqueId(), 3);
                    }
                    player.getInventory().addItem(paid.get(choice).toArray(new ItemStack[]{}));
                    Notifier.error(player, "Enjoy PRO kit " + choice);
                    used.add(player.getUniqueId());
                } else Notifier.error(player, "This is a PRO kit - you need to upgrade to get this one :-/");
            } else Notifier.error(player, "No luck - kit " + choice + " does not exist :(");
            player.updateInventory();
        } else Notifier.error(player, "Seems like you already have a kit :)");
    }

    @Getter
    public static class Kit {
        private final List<ItemStack> items = new ArrayList<>();

        private Kit(Item... items) {
            for (Item item : items) this.items.add(item.getItem());
        }
    }

    public enum Type {
        FREE, PAID, VOTE
    }

    @Getter
    private static class Item {
        private ItemStack item;

        public Item(Material type) {
            this(type, 1);
        }

        public Item(Material type, int amount) {
            this.item = new ItemStack(type, amount);
        }

        public Item egg(short type) {
            this.item = new ItemStack(Material.MONSTER_EGG, this.item.getAmount(), type);
            return this;
        }

        public Item enchant(Enchantment enchantment, int level) {
            final ItemMeta meta = this.item.getItemMeta();
            meta.addEnchant(enchantment, level, true);
            this.item.setItemMeta(meta);
            return this;
        }

        public Item lore(String... lore) {
            final ItemMeta meta = this.item.getItemMeta();
            meta.setLore(Arrays.asList(lore));
            this.item.setItemMeta(meta);
            return this;
        }

        public Item name(String name) {
            final ItemMeta meta = this.item.getItemMeta();
            meta.setDisplayName(name);
            this.item.setItemMeta(meta);
            return this;
        }

        public Item potion(PotionType potionType, int level) {
            final ItemMeta meta = this.item.getItemMeta();
            if (!(meta instanceof PotionMeta)) {
                return this;
            }
            final Potion potion = new Potion(potionType, level).splash();
            potion.apply(this.item);
            return this;
        }

        public Item uses(int uses) {
            this.item.setDurability((short) ((this.item.getType().getMaxDurability() - uses) + 1));
            return this;
        }
    }
}
