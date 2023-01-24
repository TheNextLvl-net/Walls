package me.glennEboy.Walls.utils;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.util.io.netty.channel.Channel;

import net.minecraft.server.v1_7_R4.PacketPlayOutChat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector.PacketTitle;

public class TitleManager {

    private static Class<?> nmsChatSerializer = Reflection.getNMSClass("ChatSerializer");
    private static int VERSION = 47;

    public static void sendTitle(Player p, String title, TitleColor color) {
        if (!(getVersion(p) >= VERSION))
            return;
        String raw = String.format("{\"text\":\"\",\"extra\":[{\"text\":\"%s\",\"color\":\"%s\",bold:true}]}", title, color.getName());
        try {
            final Object handle = Reflection.getHandle(p);
            final Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
            final Object serialized = Reflection.getMethod(nmsChatSerializer, "a", String.class).invoke(null, raw);
            Object packet = PacketTitle.class.getConstructor(PacketTitle.Action.class, Reflection.getNMSClass("IChatBaseComponent")).newInstance(
                    PacketTitle.Action.TITLE, serialized);
            Reflection.getMethod(connection.getClass(), "sendPacket").invoke(connection, packet);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendSubTitle(Player p, String subtitle, TitleColor color) {
        if (!(getVersion(p) >= VERSION))
            return;
        String raw = String.format("{\"text\":\"\",\"extra\":[{\"text\":\"%s\",\"color\":\"%s\"}]}", subtitle, color.getName());
        try {
            final Object handle = Reflection.getHandle(p);
            final Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
            final Object serialized = Reflection.getMethod(nmsChatSerializer, "a", String.class).invoke(null, raw);
            Object packet = PacketTitle.class.getConstructor(PacketTitle.Action.class, Reflection.getNMSClass("IChatBaseComponent")).newInstance(
                    PacketTitle.Action.SUBTITLE, serialized);
            Reflection.getMethod(connection.getClass(), "sendPacket").invoke(connection, packet);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    
    public static void sendTimings(Player p, int fadeIn, int stay, int fadeOut) {
        if (!(getVersion(p) >= VERSION))
            return;
        try {
            final Object handle = Reflection.getHandle(p);
            final Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
            Object packet = PacketTitle.class.getConstructor(PacketTitle.Action.class, int.class, int.class, int.class).newInstance(PacketTitle.Action.TIMES,
                    fadeIn, stay, fadeOut);
            Reflection.getMethod(connection.getClass(), "sendPacket").invoke(connection, packet);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void reset(Player p) {
        if (!(getVersion(p) >= VERSION))
            return;
        try {
            final Object handle = Reflection.getHandle(p);
            final Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
            Object packet = PacketTitle.class.getConstructor(PacketTitle.Action.class).newInstance(PacketTitle.Action.RESET);
            Reflection.getMethod(connection.getClass(), "sendPacket").invoke(connection, packet);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void clear(Player p) {
        if (!(getVersion(p) >= VERSION))
            return;
        try {
            final Object handle = Reflection.getHandle(p);
            final Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
            Object packet = PacketTitle.class.getConstructor(PacketTitle.Action.class).newInstance(PacketTitle.Action.CLEAR);
            Reflection.getMethod(connection.getClass(), "sendPacket").invoke(connection, packet);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static int getVersion(Player p) {
        try {
            final Object handle = Reflection.getHandle(p);
            final Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
            final Object network = Reflection.getField(connection.getClass(), "networkManager").get(connection);
            final Object channel = Reflection.getField(network.getClass(), "m").get(network);
            final Object version = Reflection.getMethod(network.getClass(), "getVersion", Channel.class).invoke(network, channel);
            return (int) version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public enum TitleColor {
        RED, GREEN, BLUE, YELLOW, GOLD, AQUA, GRAY;

        public String getName() {
            return name().toLowerCase();
        }
    }

    /**
     * 
     * Reflection class
     * 
     */

    public static class Reflection {
        public static String getVersion() {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            String version = name.substring(name.lastIndexOf('.') + 1) + ".";
            return version;
        }

        public static Class<?> getNMSClass(String className) {
            String fullName = "net.minecraft.server." + getVersion() + className;
            Class<?> clazz = null;
            try {
                clazz = Class.forName(fullName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return clazz;
        }

        public static Class<?> getOBCClass(String className) {
            String fullName = "org.bukkit.craftbukkit." + getVersion() + className;
            Class<?> clazz = null;
            try {
                clazz = Class.forName(fullName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return clazz;
        }

        public static Object getHandle(Object obj) {
            try {
                return getMethod(obj.getClass(), "getHandle", new Class[0]).invoke(obj, new Object[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public static Field getField(Class<?> clazz, String name) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
            for (Method m : clazz.getMethods()) {
                if ((m.getName().equals(name)) && ((args.length == 0) || (ClassListEqual(args, m.getParameterTypes())))) {
                    m.setAccessible(true);
                    return m;
                }
            }
            return null;
        }

        public static boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
            boolean equal = true;
            if (l1.length != l2.length) {
                return false;
            }
            for (int i = 0; i < l1.length; i++) {
                if (l1[i] != l2[i]) {
                    equal = false;
                    break;
                }
            }
            return equal;
        }
    }
}
