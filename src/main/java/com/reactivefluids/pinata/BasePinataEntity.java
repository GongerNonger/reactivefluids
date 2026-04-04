package com.reactivefluids.pinata;

import com.reactivefluids.ReactiveFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.reactivefluids.pinata.ai.PinataRomanceGoal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Base class for all Viva Piñata creatures.
 * Handles the core piñata lifecycle: wild → visitor → resident, happiness,
 * candy drops on death (breaking the piñata), and romance mechanics.
 */
public abstract class BasePinataEntity extends Animal {

    // --- Synched Data ---
    private static final EntityDataAccessor<Integer> DATA_HAPPINESS =
            SynchedEntityData.defineId(BasePinataEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIFECYCLE =
            SynchedEntityData.defineId(BasePinataEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SOUR =
            SynchedEntityData.defineId(BasePinataEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(BasePinataEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ROMANCING =
            SynchedEntityData.defineId(BasePinataEntity.class, EntityDataSerializers.BOOLEAN);

    // Lifecycle states
    public static final int LIFECYCLE_WILD = 0;
    public static final int LIFECYCLE_VISITOR = 1;
    public static final int LIFECYCLE_RESIDENT = 2;

    // Happiness bounds
    public static final int MIN_HAPPINESS = 0;
    public static final int MAX_HAPPINESS = 100;
    public static final int DEFAULT_HAPPINESS = 50;

    // Romance cooldown
    public int romanceCooldown = 0;

    // Candy drop config
    protected int baseCandyCount = 3;

    // Accessories
    private java.util.Map<PinataAccessory.Slot, String> equippedAccessories = new java.util.EnumMap<>(PinataAccessory.Slot.class);

    // Sickness timer (ticks at happiness <= 10)
    private int sickTimer = 0;

    public BasePinataEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    // --- Attributes ---

    public static AttributeSupplier.Builder createBasePinataAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    // --- Synched Data Setup ---

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HAPPINESS, DEFAULT_HAPPINESS);
        builder.define(DATA_LIFECYCLE, LIFECYCLE_WILD);
        builder.define(DATA_SOUR, false);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_ROMANCING, false);
    }

    // --- Getters / Setters ---

    public int getHappiness() { return entityData.get(DATA_HAPPINESS); }
    public void setHappiness(int val) { entityData.set(DATA_HAPPINESS, Math.max(MIN_HAPPINESS, Math.min(MAX_HAPPINESS, val))); }
    public void addHappiness(int amount) { setHappiness(getHappiness() + amount); }

    public int getLifecycle() { return entityData.get(DATA_LIFECYCLE); }
    public void setLifecycle(int state) { entityData.set(DATA_LIFECYCLE, state); }
    public boolean isWild() { return getLifecycle() == LIFECYCLE_WILD; }
    public boolean isVisitor() { return getLifecycle() == LIFECYCLE_VISITOR; }
    public boolean isResident() { return getLifecycle() == LIFECYCLE_RESIDENT; }

    public boolean isSour() { return entityData.get(DATA_SOUR); }
    public void setSour(boolean sour) { entityData.set(DATA_SOUR, sour); }

    public int getPinataVariant() { return entityData.get(DATA_VARIANT); }
    public void setPinataVariant(int variant) { entityData.set(DATA_VARIANT, variant); }

    public boolean isRomancing() { return entityData.get(DATA_ROMANCING); }
    public void setRomancing(boolean romancing) { entityData.set(DATA_ROMANCING, romancing); }

    // --- Abstract methods for subclasses ---

    /** The display name for this piñata species (e.g., "Whirlm") */
    public abstract String getPinataSpeciesName();

    /** Items that count as food for this piñata to become a visitor */
    public abstract List<ItemStack> getVisitFoods();

    /** Items that count as food to make this piñata a resident */
    public abstract List<ItemStack> getResidentFoods();

    /** Items needed for romance */
    public abstract List<ItemStack> getRomanceFoods();

    /** Number of variants this piñata has (color/pattern variants) */
    public int getVariantCount() { return 1; }

    /** Get candy items dropped when this piñata is broken */
    public abstract List<ItemStack> getCandyDrops();

    // --- AI Goals ---

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        goalSelector.addGoal(2, new PinataRomanceGoal(this, 1.0)); // Romance has high priority
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        registerPinataGoals();
    }

    /** Override to add species-specific AI goals */
    protected void registerPinataGoals() {}

    // --- Tick Logic ---

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            // Romance cooldown
            if (romanceCooldown > 0) romanceCooldown--;

            // Lifecycle transitions
            tickLifecycle();
        }

        // Client-side particles
        if (level().isClientSide()) {
            tickClientParticles();
        }
    }

    /** Server-side lifecycle checks */
    protected void tickLifecycle() {
        // Sour piñatas can cause trouble
        if (isSour()) {
            tickSourBehavior();
        }

        // Sickness: happiness <= 10 means sick
        if (isResident() && getHappiness() <= 10) {
            sickTimer++;
            // Sick particles
            if (sickTimer % 40 == 0 && level() instanceof ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ColorParticleOption.create(
                                net.minecraft.core.particles.ParticleTypes.ENTITY_EFFECT, 0.4F, 0.8F, 0.2F),
                        getX(), getY() + getBbHeight() + 0.3, getZ(),
                        3, 0.2, 0.1, 0.2, 0);
            }
            // Auto-heal if accessory provides it
            if (PinataAccessory.hasEffect(equippedAccessories, PinataAccessory.SpecialEffect.AUTO_HEAL)) {
                addHappiness(5);
                sickTimer = 0;
            }
        } else {
            sickTimer = 0;
        }
    }

    /** Ticks this piñata has been sick (happiness <= 10). Used by GardenTickHandler for Dastardos spawning. */
    public int getSickTimer() { return sickTimer; }

    /** Override for sour piñata behavior */
    protected void tickSourBehavior() {
        // Base: sour piñatas wander and occasionally cause mischief
    }

    /** Client particles — override for species-specific effects */
    protected void tickClientParticles() {
        // Happy piñatas emit occasional note particles
        if (isResident() && getHappiness() > 75 && random.nextInt(40) == 0) {
            double px = getX() + (random.nextDouble() - 0.5) * getBbWidth();
            double py = getY() + getBbHeight() + 0.2;
            double pz = getZ() + (random.nextDouble() - 0.5) * getBbWidth();
            level().addParticle(ParticleTypes.NOTE, px, py, pz,
                    random.nextDouble(), 0, 0);
        }

        // Sour piñatas emit smoke
        if (isSour() && random.nextInt(10) == 0) {
            double px = getX() + (random.nextDouble() - 0.5) * getBbWidth();
            double py = getY() + random.nextDouble() * getBbHeight();
            double pz = getZ() + (random.nextDouble() - 0.5) * getBbWidth();
            level().addParticle(ParticleTypes.SMOKE, px, py, pz, 0, 0.02, 0);
        }
    }

    // --- Interaction ---

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Feeding logic
        if (!stack.isEmpty() && !level().isClientSide()) {
            // Check evolution first
            var evolvedType = PinataEvolution.checkEvolution(this, stack);
            if (evolvedType != null) {
                consumeItem(player, hand, stack);
                PinataEvolution.evolve(this, evolvedType);
                return InteractionResult.SUCCESS;
            }

            // Check sour taming (feeding taming item to sour piñata)
            if (isSour() && isMatchingFood(stack, getResidentFoods())) {
                consumeItem(player, hand, stack);
                setSour(false);
                setLifecycle(LIFECYCLE_RESIDENT);
                setHappiness(60);
                spawnHappyParticles();
                playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                return InteractionResult.SUCCESS;
            }

            // Check visit foods
            if (isWild() && isMatchingFood(stack, getVisitFoods())) {
                consumeItem(player, hand, stack);
                setLifecycle(LIFECYCLE_VISITOR);
                addHappiness(10);
                spawnHappyParticles();
                return InteractionResult.SUCCESS;
            }

            // Check resident foods
            if (isVisitor() && isMatchingFood(stack, getResidentFoods())) {
                consumeItem(player, hand, stack);
                setLifecycle(LIFECYCLE_RESIDENT);
                addHappiness(20);
                spawnHappyParticles();
                playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.2F);
                return InteractionResult.SUCCESS;
            }

            // Check romance foods
            if (isResident() && romanceCooldown <= 0 && isMatchingFood(stack, getRomanceFoods())) {
                consumeItem(player, hand, stack);
                setRomancing(true);
                addHappiness(15);
                spawnLoveParticles();
                return InteractionResult.SUCCESS;
            }

            // Check accessory equipping
            if (stack.getItem() instanceof AccessoryItem accessoryItem) {
                PinataAccessory acc = accessoryItem.getAccessory();
                if (acc != null) {
                    equippedAccessories.put(acc.getSlot(), acc.getId());
                    addHappiness(acc.getHappinessBonus());
                    consumeItem(player, hand, stack);
                    spawnHappyParticles();
                    if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                        sp.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                getPinataSpeciesName() + " equipped " + acc.getDisplayName() + "!"));
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // Generic feeding — any food item increases happiness slightly
            if (isFood(stack)) {
                consumeItem(player, hand, stack);
                addHappiness(5);
                spawnHappyParticles();
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    private boolean isMatchingFood(ItemStack stack, List<ItemStack> foods) {
        for (ItemStack food : foods) {
            if (ItemStack.isSameItem(stack, food)) return true;
        }
        return false;
    }

    private void consumeItem(Player player, InteractionHand hand, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    // --- Death / Breaking ---

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
        // Drop candy when broken!
        List<ItemStack> candy = getCandyDrops();
        for (ItemStack drop : candy) {
            spawnAtLocation(drop);
        }

        // Confetti explosion
        spawnBreakParticles(level);
    }

    protected void spawnBreakParticles(ServerLevel level) {
        // Burst of colorful particles when piñata breaks
        for (int i = 0; i < 30; i++) {
            level.sendParticles(ParticleTypes.FIREWORK,
                    getX(), getY() + getBbHeight() / 2, getZ(),
                    1,
                    (random.nextDouble() - 0.5) * 0.5,
                    random.nextDouble() * 0.5,
                    (random.nextDouble() - 0.5) * 0.5,
                    0.1);
        }
        level.sendParticles(ParticleTypes.CLOUD,
                getX(), getY() + getBbHeight() / 2, getZ(),
                10, getBbWidth() / 2, getBbHeight() / 2, getBbWidth() / 2, 0.05);
    }

    public void spawnHappyParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    getX(), getY() + getBbHeight(), getZ(),
                    5, 0.3, 0.2, 0.3, 0.02);
        }
    }

    protected void spawnLoveParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                    getX(), getY() + getBbHeight(), getZ(),
                    7, 0.3, 0.3, 0.3, 0.02);
        }
    }

    // --- Breeding (vanilla hook — we use our own romance system) ---

    @Override
    public boolean isFood(ItemStack stack) {
        return isMatchingFood(stack, getVisitFoods()) ||
               isMatchingFood(stack, getResidentFoods()) ||
               isMatchingFood(stack, getRomanceFoods());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        // Subclasses override to produce babies
        return null;
    }

    @Override
    public boolean canMate(Animal other) {
        if (other.getClass() != this.getClass()) return false;
        BasePinataEntity otherPinata = (BasePinataEntity) other;
        return this.isRomancing() && otherPinata.isRomancing()
                && this.isResident() && otherPinata.isResident();
    }

    // --- Persistence ---

    @Override
    public boolean isPersistenceRequired() {
        return isResident() || isVisitor();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return isWild() && distance > 128.0;
    }

    // --- Save / Load ---

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PinataHappiness", getHappiness());
        tag.putInt("PinataLifecycle", getLifecycle());
        tag.putBoolean("PinataSour", isSour());
        tag.putInt("PinataVariant", getPinataVariant());
        tag.putBoolean("PinataRomancing", isRomancing());
        tag.putInt("RomanceCooldown", romanceCooldown);
        tag.put("Accessories", PinataAccessory.saveAccessories(equippedAccessories));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PinataHappiness")) setHappiness(tag.getInt("PinataHappiness"));
        if (tag.contains("PinataLifecycle")) setLifecycle(tag.getInt("PinataLifecycle"));
        if (tag.contains("PinataSour")) setSour(tag.getBoolean("PinataSour"));
        if (tag.contains("PinataVariant")) setPinataVariant(tag.getInt("PinataVariant"));
        if (tag.contains("PinataRomancing")) setRomancing(tag.getBoolean("PinataRomancing"));
        if (tag.contains("RomanceCooldown")) romanceCooldown = tag.getInt("RomanceCooldown");
        if (tag.contains("Accessories")) equippedAccessories = PinataAccessory.loadAccessories(tag.getCompound("Accessories"));
    }

    public java.util.Map<PinataAccessory.Slot, String> getEquippedAccessories() {
        return equippedAccessories;
    }
}
