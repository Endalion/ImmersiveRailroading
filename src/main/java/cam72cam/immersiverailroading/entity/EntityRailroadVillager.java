package cam72cam.immersiverailroading.entity;

import java.util.UUID;

import javax.annotation.Nullable;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.util.BlockUtil;
import cam72cam.immersiverailroading.util.VecUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class EntityRailroadVillager extends EntityCreature {
	
    protected UUID OwnerID = new UUID(0,0);
    public static final UUID EMPTYUUID = new UUID(0,0);
    protected UUID RailroadUUID;
    public boolean followingOwner;
    
    private int professionLevel = 0;
    private int railroadProfession = 0;
    
	protected ItemStackHandler handInventory = new ItemStackHandler(1);
	
	public EntityRailroadVillager(World worldIn) {
		super(worldIn);
        this.setSize(0.6F, 1.95F);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setTag("HandItems", handInventory.serializeNBT());
		//ImmersiveRailroading.info("Write NBT was %s", nbttagcompound.getUniqueId("RailOwner"));
		nbttagcompound.setUniqueId("RailOwner", this.OwnerID);
		//ImmersiveRailroading.info("Write NBT now %s", nbttagcompound.getUniqueId("RailOwner"));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("HandItems")) {
			ItemStackHandler temp = new ItemStackHandler();
			temp.deserializeNBT((NBTTagCompound) nbttagcompound.getTag("HandItems"));
			handInventory.setStackInSlot(0, temp.getStackInSlot(0));
		}
		//ImmersiveRailroading.info("Read NBT was %s", this.OwnerID);
		UUID tempUUID = nbttagcompound.getUniqueId("RailOwner");
		if (tempUUID != null && !tempUUID.equals(EMPTYUUID))this.OwnerID = nbttagcompound.getUniqueId("RailOwner");
		//ImmersiveRailroading.info("Read NBT now %s", this.OwnerID);
	}
    
    @Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {
    	if (!this.world.isRemote && this.OwnerID.equals(EMPTYUUID) && player.getHeldItemMainhand().getItem() == Items.EMERALD) {
    		player.getHeldItemMainhand().shrink(1);
    		this.setCustomNameTag("Porter [" + player.getName() + "]");
    		OwnerID = player.getGameProfile().getId();
    		return true;
    	}
    	else if(this.isOwner(player) && player.isSneaking()) {
    		//followingOwner = true;
    		return true;
    	}
		return false;
	}


	@Override
	public void onUpdate() {
		super.onUpdate();
	}
    
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn)
    {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LootTableList.ENTITIES_VILLAGER;
    }
    
    /**
     * Called only once on an entity when first time spawned, via egg, mob spawner, natural spawning etc, but not called
     * when entity is reloaded from nbt. Mainly used for initializing attributes and inventory
     */
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata)
    {
        return this.finalizeMobSpawn(difficulty, livingdata, true);
    }

    public IEntityLivingData finalizeMobSpawn(DifficultyInstance p_190672_1_, @Nullable IEntityLivingData p_190672_2_, boolean p_190672_3_)
    {
        return p_190672_2_;
    }
    
    @Override
    public float getBlockPathWeight(BlockPos pos)
    {
        return BlockUtil.isIRRail(world, pos) ? -10.0F : 0.0F;
    }
    
    @Override
    protected boolean canDespawn()
    {
        return false;
    }
    
    public boolean isPassive()
    {
    	return true;
    }
    
    @Override
    public boolean canBeLeashedTo(EntityPlayer player)
    {
        return false;
    }
    
    public double getAngleFromStock(EntityRollingStock stock) {
    	double angleVelocityToBlock = VecUtil.toYaw(this.getPositionVector().subtract(stock.getPositionVector())) - stock.rotationYaw;
		angleVelocityToBlock = (angleVelocityToBlock + 180) % 360 - 180;
		return angleVelocityToBlock;
    }
    
    public BlockPos getPositionBesideStock(EntityRollingStock stock) {
		double sideOfStock = this.getAngleFromStock(stock);
		Vec3d delta = VecUtil.fromWrongYaw(stock.getDefinition().getPassengerCompartmentWidth(stock.gauge)/2 + 2.0 * stock.gauge.scale(), stock.rotationYaw + (sideOfStock > 0 ? 90 : -90));
		return new BlockPos(stock.getPositionVector().add(delta));
    }
    
    public UUID getOwner() {
    	return OwnerID;
    }
    
    public boolean isOwner(EntityPlayer player) {
    	return player.getGameProfile().getId().equals(OwnerID);
    }
    
	public void transferAllItems(IItemHandler source, IItemHandler dest, int numstacks) {
		for (int slot = 0; slot < source.getSlots(); slot++) {
			ItemStack stack = source.getStackInSlot(slot).copy();
			if (stack.isEmpty()) {
				continue;
			}
			int orig_count = stack.getCount();
			stack = ItemHandlerHelper.insertItem(dest, stack, false);
			if (stack.getCount() != orig_count) {
				source.extractItem(slot, orig_count - stack.getCount(), false);
				numstacks--;
			}
			if (numstacks <= 0) {
				return;
			}
		}
	}
	
	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slot)
	{
		if (slot == EntityEquipmentSlot.HEAD) {
			return getHeldItemMainhand();
		}
		return ItemStack.EMPTY;
	}
	
	@Override
	public ItemStack getHeldItemMainhand()
    {
		return this.handInventory.getStackInSlot(0).copy();
    }
	
	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		if (world.isRemote) {
			return;
		}
		
        this.world.spawnEntity(new EntityItem(this.world, this.posX, this.posY, this.posZ, this.getHeldItemMainhand().copy()));
	}
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }
    
	@SuppressWarnings("unchecked")
	@Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) handInventory;
        }
        return super.getCapability(capability, facing);
    }
	
	@Override
	public boolean attackEntityAsMob(Entity entityIn)
    {
        float f = (float)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        int i = 0;

        if (entityIn instanceof EntityLivingBase)
        {
            f += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase)entityIn).getCreatureAttribute());
            i += EnchantmentHelper.getKnockbackModifier(this);
        }

        boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

        if (flag)
        {
            if (i > 0 && entityIn instanceof EntityLivingBase)
            {
                ((EntityLivingBase)entityIn).knockBack(this, (float)i * 0.5F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }

            int j = EnchantmentHelper.getFireAspectModifier(this);

            if (j > 0)
            {
                entityIn.setFire(j * 4);
            }

            if (entityIn instanceof EntityPlayer)
            {
                EntityPlayer entityplayer = (EntityPlayer)entityIn;
                ItemStack itemstack = this.getHeldItemMainhand();
                ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

                if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem().canDisableShield(itemstack, itemstack1, entityplayer, this) && itemstack1.getItem().isShield(itemstack1, entityplayer))
                {
                    float f1 = 0.25F + (float)EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

                    if (this.rand.nextFloat() < f1)
                    {
                        entityplayer.getCooldownTracker().setCooldown(itemstack1.getItem(), 100);
                        this.world.setEntityState(entityplayer, (byte)30);
                    }
                }
            }

            this.applyEnchantments(this, entityIn);
        }

        return flag;
    }
}
