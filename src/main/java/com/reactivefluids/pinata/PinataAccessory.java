package com.reactivefluids.pinata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Piñata Accessory system — each piñata has 9 accessory slots.
 *
 * VP slots: Head, Eyes, Ears, Nose, Mouth, Neck, Arms, Body, Feet
 *
 * Each accessory provides:
 * - Happiness bonus (passive candiosity increase)
 * - Optional special effect (auto-produce, auto-heal, etc.)
 * - Visual layer on the piñata model (render overlay)
 *
 * Accessories are stored in piñata NBT as a list of accessory IDs.
 */
public class PinataAccessory {

    public enum Slot {
        HEAD, EYES, EARS, NOSE, MOUTH, NECK, ARMS, BODY, FEET
    }

    public enum SpecialEffect {
        NONE,
        AUTO_PRODUCE,    // Buzzlegum Keeper Hat — auto-collect honey
        AUTO_HEAL,       // Halo of Hardness — prevents sickness
        SPEED_BOOST,     // Running Shoes — +10% speed
        ROMANCE_REQ,     // Required for certain species' romance
        VALUE_BOOST      // Increases piñata sale value
    }

    private final String id;
    private final String displayName;
    private final Slot slot;
    private final int happinessBonus;
    private final SpecialEffect effect;
    private final int cost; // In chocolate coins

    public PinataAccessory(String id, String displayName, Slot slot,
                           int happinessBonus, SpecialEffect effect, int cost) {
        this.id = id;
        this.displayName = displayName;
        this.slot = slot;
        this.happinessBonus = happinessBonus;
        this.effect = effect;
        this.cost = cost;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Slot getSlot() { return slot; }
    public int getHappinessBonus() { return happinessBonus; }
    public SpecialEffect getEffect() { return effect; }
    public int getCost() { return cost; }

    // === Registry ===

    private static final Map<String, PinataAccessory> REGISTRY = new LinkedHashMap<>();

    public static void register(PinataAccessory accessory) {
        REGISTRY.put(accessory.id, accessory);
    }

    public static PinataAccessory get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<PinataAccessory> getAll() {
        return REGISTRY.values();
    }

    static {
        // === Head Accessories ===
        register(new PinataAccessory("top_hat", "Top Hat", Slot.HEAD,
                5, SpecialEffect.NONE, 50));
        register(new PinataAccessory("crown", "Crown", Slot.HEAD,
                10, SpecialEffect.VALUE_BOOST, 200));
        register(new PinataAccessory("halo_of_hardness", "Halo of Hardness", Slot.HEAD,
                8, SpecialEffect.AUTO_HEAL, 500));
        register(new PinataAccessory("keeper_hat", "Keeper Hat", Slot.HEAD,
                5, SpecialEffect.AUTO_PRODUCE, 300));
        register(new PinataAccessory("party_hat", "Party Hat", Slot.HEAD,
                3, SpecialEffect.NONE, 20));
        register(new PinataAccessory("pirate_hat", "Pirate Hat", Slot.HEAD,
                5, SpecialEffect.NONE, 80));

        // === Eyes Accessories ===
        register(new PinataAccessory("glasses", "Glasses", Slot.EYES,
                3, SpecialEffect.NONE, 30));
        register(new PinataAccessory("monocle", "Monocle", Slot.EYES,
                5, SpecialEffect.VALUE_BOOST, 100));

        // === Ears Accessories ===
        register(new PinataAccessory("earrings", "Earrings", Slot.EARS,
                4, SpecialEffect.NONE, 60));
        register(new PinataAccessory("headphones", "Headphones", Slot.EARS,
                3, SpecialEffect.NONE, 40));

        // === Nose Accessories ===
        register(new PinataAccessory("mustache", "Mustache", Slot.NOSE,
                3, SpecialEffect.NONE, 30));

        // === Mouth Accessories ===
        register(new PinataAccessory("gold_teeth", "Gold Teeth", Slot.MOUTH,
                5, SpecialEffect.VALUE_BOOST, 150));

        // === Neck Accessories ===
        register(new PinataAccessory("bell_collar", "Bell Collar", Slot.NECK,
                4, SpecialEffect.NONE, 40));
        register(new PinataAccessory("bow_tie", "Bow Tie", Slot.NECK,
                3, SpecialEffect.NONE, 25));
        register(new PinataAccessory("necklace", "Necklace", Slot.NECK,
                5, SpecialEffect.NONE, 80));

        // === Body Accessories ===
        register(new PinataAccessory("backpack", "Backpack", Slot.BODY,
                3, SpecialEffect.NONE, 50));
        register(new PinataAccessory("cape", "Cape", Slot.BODY,
                5, SpecialEffect.NONE, 100));

        // === Feet Accessories ===
        register(new PinataAccessory("running_shoes", "Running Shoes", Slot.FEET,
                3, SpecialEffect.SPEED_BOOST, 120));
        register(new PinataAccessory("boots", "Boots", Slot.FEET,
                3, SpecialEffect.NONE, 60));
    }

    // === NBT Helper for storing accessories on piñata entities ===

    /** Save equipped accessories to NBT */
    public static CompoundTag saveAccessories(Map<Slot, String> equipped) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<Slot, String> entry : equipped.entrySet()) {
            tag.putString(entry.getKey().name(), entry.getValue());
        }
        return tag;
    }

    /** Load equipped accessories from NBT */
    public static Map<Slot, String> loadAccessories(CompoundTag tag) {
        Map<Slot, String> equipped = new EnumMap<>(Slot.class);
        for (Slot slot : Slot.values()) {
            if (tag.contains(slot.name())) {
                equipped.put(slot, tag.getString(slot.name()));
            }
        }
        return equipped;
    }

    /** Calculate total happiness bonus from equipped accessories */
    public static int calculateHappinessBonus(Map<Slot, String> equipped) {
        int total = 0;
        for (String id : equipped.values()) {
            PinataAccessory acc = get(id);
            if (acc != null) total += acc.happinessBonus;
        }
        return total;
    }

    /** Check if any equipped accessory has a specific effect */
    public static boolean hasEffect(Map<Slot, String> equipped, SpecialEffect effect) {
        for (String id : equipped.values()) {
            PinataAccessory acc = get(id);
            if (acc != null && acc.effect == effect) return true;
        }
        return false;
    }
}
