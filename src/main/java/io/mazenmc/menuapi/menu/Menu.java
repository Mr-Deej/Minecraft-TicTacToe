/*
 * MIT License
 *
 * Copyright (c) 2020 Luke Anderson (stuntguy3000)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * Copyright (c) 2015, Mazen Kotb, email@mazenmc.io
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package io.mazenmc.menuapi.menu;

import io.mazenmc.menuapi.items.Item;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Menu implements Listener {
    private static final JavaPlugin OWNER = JavaPlugin.getProvidingPlugin(Menu.class);
    protected Map<Integer, Item> items = new HashMap<>(); // map for quick lookup
    private String name;
    private int size;
    private Inventory inventory;
    private Menu parent;
    private boolean stickyMenu = false;

    protected Menu(String name, int size, boolean sticky) { // allow for sub classes
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.size = size;
        this.stickyMenu = sticky;

        this.inventory = Bukkit.createInventory(null, size, this.name);
        Bukkit.getPluginManager().registerEvents(this, OWNER);
    }

    public static Menu createMenu(String name, int size, boolean sticky) {
        return new Menu(name, size, sticky);
    }

    /**
     * The name of this menu, with translated color codes
     *
     * @return The name
     */
    public String name() {
        return name;
    }

    /**
     * The size of this menu
     *
     * @return The size of this menu
     */
    public int size() {
        return size;
    }

    /**
     * The inventory representation of this menu
     *
     * @return The inventory representation
     */
    public Inventory inventory() {
        return inventory;
    }

    /**
     * Returns the item at specified index
     *
     * @param index
     * @return Found item
     */
    public Item itemAt(int index) {
        return items.get(index);
    }

    /**
     * Returns the item at specified coordinates, where x is on the horizontal axis and z is on the vertical axis.
     *
     * @param x The x coordinate
     * @param z The z coordinate
     * @return Found item
     */
    public Item itemAt(int x, int z) {
        return items.get(z * 9 + x);
    }

    /**
     * Sets the item at the specified index
     *
     * @param index Index of the item you wish to set
     * @param item  The item you wish to set the index as
     */
    public Menu setItem(int index, Item item) {
        if (item == null) {
            inventory.setItem(index, null);
        } else {
            inventory.setItem(index, item.stack());
        }

        items.put(index, item);
        return this;
    }

    /**
     * Sets the item at the specified coordinates, where x is on the horizontal axis and z is on the vertical axis.
     *
     * @param x    The x coordinate
     * @param z    The z coordinate
     * @param item The item you wish to set the index as
     */
    public Menu setItem(int x, int z, Item item) {
        return setItem(z * 9 + x, item);
    }

    /**
     * Sets the parent of the menu, used when the player exits the menu
     */
    public Menu setParent(Menu parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Show the menu to the inputted players
     *
     * @param players The players you wish to show the menu too
     */
    public void showTo(Player... players) {
        for (Player p : players) {
            p.openInventory(inventory);
        }
    }

    @EventHandler
    public void onExit(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory))
            return;

        if (parent != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().openInventory(parent.inventory);
                }
            }.runTaskLater(OWNER, 2L);
        } else if (stickyMenu) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().openInventory(inventory);
                }
            }.runTaskLater(OWNER, 2L);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory))
            return;

        if (event.getRawSlot() >= size && !event.getClick().isShiftClick())
            return;

        event.setCancelled(true);

        if (!items.containsKey(event.getSlot())) {
            return;
        }

        stickyMenu = false;
        items.get(event.getSlot()).act((Player) event.getWhoClicked(), event.getClick());
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory))
            return;

        event.setCancelled(true);
    }

    public Menu setSticky(Boolean sticky) {
        this.stickyMenu = sticky;
        return this;
    }
}
