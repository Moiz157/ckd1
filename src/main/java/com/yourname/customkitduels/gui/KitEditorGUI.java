package com.yourname.customkitduels.gui;

import com.yourname.customkitduels.CustomKitDuels;
import com.yourname.customkitduels.data.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KitEditorGUI implements Listener {
    
    private final CustomKitDuels plugin;
    private final Player player;
    private final String kitName;
    private final Inventory gui;
    private final ItemStack[] kitContents;
    private final ItemStack[] kitArmor;
    private ItemStack offhandItem;
    private static final Map<UUID, KitEditorGUI> activeGuis = new HashMap<>();
    private boolean isActive = true;
    private boolean isBulkMode = false;
    private ItemStack bulkItem = null;
    private boolean isNavigating = false;
    
    public KitEditorGUI(CustomKitDuels plugin, Player player, String kitName) {
        this.plugin = plugin;
        this.player = player;
        this.kitName = kitName;
        this.gui = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Editing Kit " + kitName);
        this.kitContents = new ItemStack[36];
        this.kitArmor = new ItemStack[4];
        this.offhandItem = null;
        
        plugin.getLogger().info("[DEBUG] Creating KitEditorGUI for player " + player.getName() + " with kit " + kitName);
        
        // Load existing kit if editing
        Kit existingKit = plugin.getKitManager().getKit(player.getUniqueId(), kitName);
        if (existingKit != null) {
            plugin.getLogger().info("[DEBUG] Loading existing kit data for " + kitName);
            System.arraycopy(existingKit.getContents(), 0, kitContents, 0, Math.min(existingKit.getContents().length, 36));
            System.arraycopy(existingKit.getArmor(), 0, kitArmor, 0, 4);
            // Load offhand if available (stored in slot 36 of contents array)
            if (existingKit.getContents().length > 36 && existingKit.getContents()[36] != null) {
                this.offhandItem = existingKit.getContents()[36];
            }
        }
        
        setupGUI();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    private void setupGUI() {
        plugin.getLogger().info("[DEBUG] Setting up GUI for " + player.getName());
        
        // Clear GUI first
        gui.clear();
        
        // Fill main inventory slots (0-35) with colored glass panes or items
        for (int i = 0; i < 36; i++) {
            updateSlot(i);
        }
        
        // Fill armor slots (36-39) with colored glass panes or items
        for (int i = 0; i < 4; i++) {
            updateArmorSlot(i);
        }
        
        // Add offhand slot (slot 40)
        updateOffhandSlot();
        
        // Add control buttons
        setupControlButtons();
        
        plugin.getLogger().info("[DEBUG] GUI setup complete for " + player.getName());
    }
    
    private void updateSlot(int slot) {
        if (kitContents[slot] != null) {
            gui.setItem(slot, kitContents[slot].clone());
        } else {
            ItemStack glassPane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
            ItemMeta meta = glassPane.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + "Slot #" + (slot + 1));
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Left-click to add an item",
                ChatColor.GRAY + "Right-click to modify (if item present)",
                ChatColor.YELLOW + "Shift-click to enter bulk mode",
                ChatColor.GREEN + "💡 TIP: Use bulk mode for quick filling!"
            ));
            glassPane.setItemMeta(meta);
            gui.setItem(slot, glassPane);
        }
    }
    
    private void updateArmorSlot(int armorIndex) {
        String[] armorSlots = {"Boots", "Leggings", "Chestplate", "Helmet"};
        int guiSlot = 36 + armorIndex;
        
        if (kitArmor[armorIndex] != null) {
            gui.setItem(guiSlot, kitArmor[armorIndex].clone());
        } else {
            ItemStack glassPane = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
            ItemMeta meta = glassPane.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + armorSlots[armorIndex] + " Slot");
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Left-click to add " + armorSlots[armorIndex].toLowerCase(),
                ChatColor.GRAY + "Right-click to modify (if armor present)"
            ));
            glassPane.setItemMeta(meta);
            gui.setItem(guiSlot, glassPane);
        }
    }
    
    private void updateOffhandSlot() {
        if (offhandItem != null) {
            gui.setItem(40, offhandItem.clone());
        } else {
            ItemStack offhandPane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
            ItemMeta offhandMeta = offhandPane.getItemMeta();
            offhandMeta.setDisplayName(ChatColor.AQUA + "Offhand Slot");
            offhandMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Left-click to add offhand item",
                ChatColor.GRAY + "Right-click to modify (if item present)"
            ));
            offhandPane.setItemMeta(offhandMeta);
            gui.setItem(40, offhandPane);
        }
    }
    
    private void setupControlButtons() {
        ItemStack saveButton = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = saveButton.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save Kit");
        saveMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to save this kit"));
        saveButton.setItemMeta(saveMeta);
        gui.setItem(45, saveButton);
        
        ItemStack cancelButton = new ItemStack(Material.REDSTONE);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancelMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to cancel"));
        cancelButton.setItemMeta(cancelMeta);
        gui.setItem(53, cancelButton);
        
        // Clear button
        ItemStack clearButton = new ItemStack(Material.BARRIER);
        ItemMeta clearMeta = clearButton.getItemMeta();
        clearMeta.setDisplayName(ChatColor.YELLOW + "Clear All");
        clearMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to clear all slots"));
        clearButton.setItemMeta(clearMeta);
        gui.setItem(49, clearButton);
        
        // ENHANCED BULK MODE BUTTON - More prominent placement
        ItemStack bulkButton = new ItemStack(isBulkMode ? Material.LIME_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta bulkMeta = bulkButton.getItemMeta();
        
        if (isBulkMode) {
            bulkMeta.setDisplayName(ChatColor.GREEN + "🔥 BULK MODE: ACTIVE");
            bulkMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Item: " + (bulkItem != null ? bulkItem.getType().name() : "None"),
                ChatColor.YELLOW + "Click slots to place this item",
                ChatColor.RED + "Right-click here to exit bulk mode",
                ChatColor.GOLD + "💡 Left-click to change bulk item"
            ));
        } else {
            bulkMeta.setDisplayName(ChatColor.YELLOW + "🔥 ACTIVATE BULK MODE");
            bulkMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Quick-fill multiple slots with the same item",
                ChatColor.GREEN + "Click to select an item for bulk placement",
                ChatColor.AQUA + "Or shift-click any slot with an item",
                ChatColor.GOLD + "💡 Great for arrows, blocks, food, etc!"
            ));
        }
        
        bulkButton.setItemMeta(bulkMeta);
        gui.setItem(47, bulkButton); // More prominent position
        
        // Add bulk mode helper button
        if (!isBulkMode) {
            ItemStack helperButton = new ItemStack(Material.BOOK);
            ItemMeta helperMeta = helperButton.getItemMeta();
            helperMeta.setDisplayName(ChatColor.AQUA + "📖 Bulk Mode Help");
            helperMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Bulk mode allows you to quickly fill",
                ChatColor.GRAY + "multiple slots with the same item.",
                ChatColor.YELLOW + "Ways to activate:",
                ChatColor.WHITE + "• Click the yellow bulk button",
                ChatColor.WHITE + "• Shift-click any slot with an item",
                ChatColor.GREEN + "Perfect for: arrows, food, blocks!"
            ));
            helperButton.setItemMeta(helperMeta);
            gui.setItem(46, helperButton);
        }
    }
    
    public void open() {
        plugin.getLogger().info("[DEBUG] Opening KitEditorGUI for " + player.getName());
        
        // Clean up any existing GUI for this player
        KitEditorGUI existing = activeGuis.get(player.getUniqueId());
        if (existing != null && existing != this) {
            plugin.getLogger().info("[DEBUG] Cleaning up existing GUI for " + player.getName());
            existing.forceCleanup();
        }
        
        activeGuis.put(player.getUniqueId(), this);
        isActive = true;
        isNavigating = false;
        player.openInventory(gui);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        
        // CRITICAL: Only handle events for our specific player and GUI
        if (!clicker.getUniqueId().equals(player.getUniqueId())) return;
        if (!event.getInventory().equals(gui)) return;
        if (!isActive || isNavigating) return;
        
        event.setCancelled(true);
        int slot = event.getSlot();
        ClickType clickType = event.getClick();
        
        plugin.getLogger().info("[DEBUG] KitEditorGUI click event - Player: " + player.getName() + ", Slot: " + slot + ", ClickType: " + clickType + ", Active: " + isActive + ", BulkMode: " + isBulkMode);
        
        // Handle enhanced bulk mode button
        if (slot == 47) {
            if (isBulkMode) {
                if (clickType == ClickType.RIGHT) {
                    exitBulkMode();
                    return;
                } else if (clickType == ClickType.LEFT) {
                    // Change bulk item
                    openCategorySelectorForBulk(0);
                    return;
                }
            } else {
                // Activate bulk mode
                openCategorySelectorForBulk(0);
                return;
            }
        }
        
        // Handle control buttons
        if (slot == 45) { // Save button
            plugin.getLogger().info("[DEBUG] Save button clicked by " + player.getName());
            saveKit();
            return;
        }
        
        if (slot == 53) { // Cancel button
            plugin.getLogger().info("[DEBUG] Cancel button clicked by " + player.getName());
            forceCleanup();
            return;
        }
        
        if (slot == 49) { // Clear button
            plugin.getLogger().info("[DEBUG] Clear button clicked by " + player.getName());
            clearAllSlots();
            return;
        }
        
        // Handle slot selection (0-40: main inventory, armor, and offhand)
        if (slot <= 40) {
            // Handle bulk mode
            if (isBulkMode && bulkItem != null) {
                if (slot < 36) { // Only main inventory slots for bulk mode
                    setSlotItem(slot, bulkItem.clone());
                    player.sendMessage(ChatColor.GREEN + "🔥 Bulk placed " + bulkItem.getType().name() + " in slot " + (slot + 1) + "!");
                    return;
                } else {
                    player.sendMessage(ChatColor.RED + "Bulk mode only works for main inventory slots!");
                    return;
                }
            }
            
            // Handle shift-click for bulk mode activation
            if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                ItemStack currentItem = getCurrentItemInSlot(slot);
                if (currentItem != null && !isPlaceholderItem(currentItem)) {
                    enterBulkMode(currentItem);
                    return;
                } else if (slot < 36) { // Only for main inventory slots
                    // Open category selector to choose bulk item
                    openCategorySelectorForBulk(slot);
                    return;
                }
            }
            
            if (clickType == ClickType.RIGHT) {
                // Right-click: open item modification menu if item exists
                ItemStack currentItem = getCurrentItemInSlot(slot);
                if (currentItem != null && !isPlaceholderItem(currentItem)) {
                    plugin.getLogger().info("[DEBUG] Right-click on slot " + slot + " with item " + currentItem.getType());
                    openItemModificationMenu(slot, currentItem);
                    return;
                }
            }
            
            // Left-click or right-click on empty slot: open appropriate selector
            plugin.getLogger().info("[DEBUG] Slot " + slot + " clicked by " + player.getName() + " - opening selector");
            
            // Determine what type of selector to open based on slot
            if (slot >= 36 && slot <= 39) {
                // Armor slot - open armor-specific selector
                openArmorSelector(slot);
            } else {
                // Regular slot - open category selector
                openCategorySelector(slot);
            }
        }
    }
    
    private void enterBulkMode(ItemStack item) {
        isBulkMode = true;
        bulkItem = item.clone();
        setupControlButtons(); // Refresh to show bulk indicator
        player.sendMessage(ChatColor.GREEN + "🔥 Bulk mode activated! Click slots to place " + item.getType().name());
        player.sendMessage(ChatColor.YELLOW + "Right-click the green button to exit bulk mode");
        player.sendMessage(ChatColor.AQUA + "Left-click the green button to change bulk item");
    }
    
    private void exitBulkMode() {
        isBulkMode = false;
        bulkItem = null;
        setupControlButtons(); // Refresh to hide bulk indicator
        player.sendMessage(ChatColor.YELLOW + "🔥 Bulk mode deactivated");
    }
    
    private void openCategorySelectorForBulk(int slot) {
        plugin.getLogger().info("[DEBUG] Opening category selector for bulk mode");
        
        // Set navigation state to prevent cleanup
        isNavigating = true;
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new BulkCategorySelectorGUI(plugin, player, this, slot).open();
        }, 1L);
    }
    
    private void openCategorySelector(int slot) {
        plugin.getLogger().info("[DEBUG] Opening category selector for slot " + slot);
        
        // Set navigation state to prevent cleanup
        isNavigating = true;
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new CategorySelectorGUI(plugin, player, this, slot).open();
        }, 1L);
    }
    
    private void openArmorSelector(int slot) {
        plugin.getLogger().info("[DEBUG] Opening armor selector for slot " + slot);
        
        // Set navigation state to prevent cleanup
        isNavigating = true;
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new ArmorSelectorGUI(plugin, player, this, slot).open();
        }, 1L);
    }
    
    private void openItemModificationMenu(int slot, ItemStack item) {
        plugin.getLogger().info("[DEBUG] Opening item modification menu for slot " + slot);
        
        // Set navigation state to prevent cleanup
        isNavigating = true;
        
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            new ItemModificationGUI(plugin, player, this, slot, item).open();
        }, 1L);
    }
    
    private ItemStack getCurrentItemInSlot(int slot) {
        if (slot < 36) {
            return kitContents[slot];
        } else if (slot < 40) {
            return kitArmor[slot - 36];
        } else if (slot == 40) {
            return offhandItem;
        }
        return null;
    }
    
    private boolean isPlaceholderItem(ItemStack item) {
        if (item == null) return true;
        Material type = item.getType();
        return type == Material.PURPLE_STAINED_GLASS_PANE || 
               type == Material.GREEN_STAINED_GLASS_PANE || 
               type == Material.ORANGE_STAINED_GLASS_PANE;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player closer = (Player) event.getPlayer();
        
        if (closer.getUniqueId().equals(player.getUniqueId()) && event.getInventory().equals(gui)) {
            plugin.getLogger().info("[DEBUG] KitEditorGUI inventory closed by " + player.getName() + ", Active: " + isActive + ", Navigating: " + isNavigating);
            
            // Only cleanup if this is a final close (not navigation)
            if (isActive && !isNavigating) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (isActive && !isNavigating) {
                        plugin.getLogger().info("[DEBUG] Final cleanup for " + player.getName());
                        forceCleanup();
                    }
                }, 5L);
            }
        }
    }
    
    private void forceCleanup() {
        plugin.getLogger().info("[DEBUG] Force cleanup for " + player.getName());
        isActive = false;
        isBulkMode = false;
        bulkItem = null;
        isNavigating = false;
        activeGuis.remove(player.getUniqueId());
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        player.closeInventory();
    }
    
    public void setSlotItem(int slot, ItemStack item) {
        plugin.getLogger().info("[DEBUG] Setting slot " + slot + " to item " + (item != null ? item.getType() : "null") + " for " + player.getName());
        
        if (slot < 36) {
            kitContents[slot] = item;
            updateSlot(slot);
        } else if (slot < 40) {
            kitArmor[slot - 36] = item;
            updateArmorSlot(slot - 36);
        } else if (slot == 40) {
            offhandItem = item;
            updateOffhandSlot();
        }
    }
    
    public void clearSlot(int slot) {
        plugin.getLogger().info("[DEBUG] Clearing slot " + slot + " for " + player.getName());
        setSlotItem(slot, null);
    }
    
    private void clearAllSlots() {
        plugin.getLogger().info("[DEBUG] Clearing all slots for " + player.getName());
        for (int i = 0; i < 36; i++) {
            kitContents[i] = null;
            updateSlot(i);
        }
        for (int i = 0; i < 4; i++) {
            kitArmor[i] = null;
            updateArmorSlot(i);
        }
        offhandItem = null;
        updateOffhandSlot();
        exitBulkMode(); // Exit bulk mode when clearing
        player.sendMessage(ChatColor.YELLOW + "All slots cleared!");
    }
    
    private void saveKit() {
        plugin.getLogger().info("[DEBUG] Saving kit " + kitName + " for " + player.getName());
        
        // Create extended contents array to include offhand
        ItemStack[] extendedContents = new ItemStack[37];
        System.arraycopy(kitContents, 0, extendedContents, 0, 36);
        extendedContents[36] = offhandItem;
        
        Kit kit = new Kit(kitName, kitName, extendedContents, kitArmor.clone());
        plugin.getKitManager().saveKit(player.getUniqueId(), kit);
        
        player.sendMessage(ChatColor.GREEN + "Kit '" + kitName + "' saved successfully!");
        forceCleanup();
    }
    
    public void refreshAndReopen() {
        plugin.getLogger().info("[DEBUG] Refreshing and reopening GUI for " + player.getName());
        
        // Reset navigation state
        isNavigating = false;
        
        // Refresh the GUI content
        setupGUI();
        
        // If the player doesn't have this inventory open, open it
        if (!player.getOpenInventory().getTopInventory().equals(gui)) {
            player.openInventory(gui);
        }
    }
    
    public void setBulkItem(ItemStack item) {
        enterBulkMode(item);
    }
    
    public boolean isActive() {
        return isActive;
    }
}