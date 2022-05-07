package org.csg.group.task;

import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemCheck {

    public static boolean itemCheck(Player p, String value) {
        List<itemv> il = new ArrayList<>();
        if (value.contains(",")) {
            String[] vl = value.split(",");
            for (String s : vl) {
                il.add(new itemv(s.split("_")[0], Integer.parseInt(s.split("_")[1])));
            }
        } else {
            il.add(new itemv(value.split("_")[0], Integer.parseInt(value.split("_")[1])));
        }

        Inventory inv = p.getInventory();
        for (itemv v : il) {
            v.reset();
        }
        for (ItemStack i : inv) {
            for (itemv v : il) {
                v.checkItem(i);
            }
        }
        for (itemv v : il) {
            if (!v.checkSuccess()) {
                return false;
            }
        }
        return true;
    }

    public static boolean itemConsume(Player p, String value) {
        List<itemv> il = new ArrayList<>();
        if (value.contains(",")) {
            String[] vl = value.split(",");
            for (String s : vl) {
                il.add(new itemv(s.split("_")[0], Integer.parseInt(s.split("_")[1])));
            }
        } else {
            il.add(new itemv(value.split("_")[0], Integer.parseInt(value.split("_")[1])));
        }

        Inventory inv = p.getInventory();
        for (itemv v : il) {
            v.reset();
        }
        for (ItemStack i : inv) {
            for (itemv v : il) {
                v.costItem(i);
            }
        }
        return true;
    }
}

class itemv {
    public String name;
    public int amount;
    public int counted = 0;

    public itemv(String Name, int Amount) {
        name = Name;
        amount = Amount;
    }

    public void reset() {
        counted = 0;
    }

    public void checkItem(ItemStack item) {
        if (item.hasItemMeta()) {
            if (item.getItemMeta().getDisplayName().contains(name)) {
                counted += item.getAmount();
            }
        }
    }

    public boolean checkSuccess() {
        return counted >= amount;
    }

    public void costItem(ItemStack item) {
        if (item.hasItemMeta()) {
            if (item.getItemMeta().getDisplayName().contains(name)) {
                int add = Math.min(item.getAmount(), amount - counted);
                counted += add;
                item.setAmount(item.getAmount() - add);
            }
        }
    }

}
