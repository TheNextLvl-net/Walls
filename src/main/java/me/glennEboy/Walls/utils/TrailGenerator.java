package me.glennEboy.Walls.utils;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class TrailGenerator {

    
    private static ItemStack diamond1 = new ItemStack(Material.DIAMOND);
    private static ItemStack gold1 = new ItemStack(Material.GOLD_INGOT);
    private static ItemStack emerald1 = new ItemStack(Material.EMERALD);
    private static ItemStack iron1 = new ItemStack(Material.IRON_INGOT);

    
    private static ItemStack redrose = new ItemStack(Material.RED_ROSE);
    private static ItemStack dandelion = new ItemStack(Material.YELLOW_FLOWER);

    private static ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
    private static ItemStack pants = new ItemStack(Material.IRON_LEGGINGS);
    private static ItemStack helmet = new ItemStack(Material.GOLD_HELMET);
    private static ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);

    private static ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
    private static ItemStack axe = new ItemStack(Material.IRON_AXE);
    private static ItemStack bow = new ItemStack(Material.BOW);
    private static ItemStack rod = new ItemStack(Material.BLAZE_ROD);
    
    private static ItemStack beef = new ItemStack(Material.COOKED_BEEF);
    private static ItemStack chicken = new ItemStack(Material.COOKED_CHICKEN);
    private static ItemStack fish = new ItemStack(Material.COOKED_FISH);
    private static ItemStack soup = new ItemStack(Material.MUSHROOM_SOUP);

    private static ItemStack dirt = new ItemStack(Material.DIRT);
    private static ItemStack bedrock = new ItemStack(Material.BEDROCK);
    private static ItemStack stone = new ItemStack(Material.STONE);
    private static ItemStack cobble = new ItemStack(Material.COBBLESTONE);
    
    private static ItemStack bed = new ItemStack(Material.BED);
    private static ItemStack anvil = new ItemStack(Material.ANVIL);
    private static ItemStack portal = new ItemStack(Material.PORTAL);
    private static ItemStack table = new ItemStack(Material.ENCHANTMENT_TABLE);

    private static ItemStack torch = new ItemStack(Material.TORCH);
    private static ItemStack lamp = new ItemStack(Material.REDSTONE_LAMP_ON);
    private static ItemStack redtorch = new ItemStack(Material.REDSTONE_TORCH_ON);

    private static ItemStack lava = new ItemStack(Material.LAVA_BUCKET);
    private static ItemStack fire = new ItemStack(Material.FIRE);

    private static ItemStack cookie = new ItemStack(Material.COOKIE);
    private static ItemStack cake = new ItemStack(Material.CAKE);
    private static ItemStack sugar = new ItemStack(Material.SUGAR);

    public static ItemStack getItem(String trailType){
        if (trailType.equalsIgnoreCase("FlowerPower")){
            
            Random dropchance = new Random();
            int newflower = dropchance.nextInt(10);
            if (newflower < 5){
                return redrose;
            }else if (newflower > 6){
                return dandelion;
            }

        }else if (trailType.equalsIgnoreCase("diamond")){
            
            Random dropchance = new Random();
            int dropped = dropchance.nextInt(5);
            if (dropped == 1) {
                return diamond1;
            } else if (dropped == 2) {
                return chestplate;
            } else if (dropped == 3) {
                return sword;
            } else if (dropped == 4) {
                return boots;
            }
        }else if (trailType.equalsIgnoreCase("RichBoi")){
            
            Random dropchance = new Random();
            int dropped = dropchance.nextInt(5);
            if (dropped == 1) {
                return diamond1;
            } else if (dropped == 2) {
                return gold1;
            } else if (dropped == 3) {
                return iron1;
            } else if (dropped == 4) {
                return emerald1;
            }
        }else if (trailType.equalsIgnoreCase("BodyGuard")){
            
            Random dropchance = new Random();
            int dropped = dropchance.nextInt(5);
            if (dropped == 1) {
                return chestplate;
            } else if (dropped == 2) {
                return pants;
            } else if (dropped == 3) {
                return helmet;
            } else if (dropped == 4) {
                return boots;
            }
        }else if (trailType.equalsIgnoreCase("eZ")){
            
            Random dropchance = new Random();
            int dropped = dropchance.nextInt(5);
            if (dropped == 1) {
                return sword;
            } else if (dropped == 2) {
                return axe;
            } else if (dropped == 3) {
                return bow;
            } else if (dropped == 4) {
                return rod;
            }
        }else if (trailType.equalsIgnoreCase("Hungry")){
            
            Random dropchance = new Random();
            int dropped = dropchance.nextInt(5);
            if (dropped == 1) {
                return beef;
            } else if (dropped == 2) {
                return chicken;
            } else if (dropped == 3) {
                return fish;
            } else if (dropped == 4) {
                return soup;
            }
        }else if (trailType.equalsIgnoreCase("BlockBawz")){
            
            Random dropchance = new Random();
            int dropped = dropchance.nextInt(5);
            if (dropped == 1) {
                return dirt;
            } else if (dropped == 2) {
                return bedrock;
            } else if (dropped == 3) {
                return stone;
            } else if (dropped == 4) {
                return cobble;
            }
        }else if (trailType.equalsIgnoreCase("random")){
            
            Random dropchance = new Random();
            int dropped = dropchance.nextInt(5);
            if (dropped == 1) {
                return bed;
            } else if (dropped == 2) {
                return anvil;
            } else if (dropped == 3) {
                return portal;
            } else if (dropped == 4) {
                return table;
            }
        }else if (trailType.equalsIgnoreCase("Flashy")){
            
            Random dropchance = new Random();
            int dropped = dropchance.nextInt(4);
            if (dropped == 1) {
                return torch;
            } else if (dropped == 2) {
                return redtorch;
            } else if (dropped == 3) {
                return lamp;
            }
        }else if (trailType.equalsIgnoreCase("Sweets")){
            
            Random dropchance = new Random();
            int dropped = dropchance.nextInt(4);
            if (dropped == 1) {
                return cookie;
            } else if (dropped == 2) {
                return cake;
            } else if (dropped == 3) {
                return sugar;
            }
        }else if (trailType.equalsIgnoreCase("dangerous")){
            
            Random dropchance = new Random();
            int newflower = dropchance.nextInt(10);
            if (newflower < 5){
                return lava;
            }else if (newflower > 6){
                return fire;
            }
        }
        
        return null;

    }
}