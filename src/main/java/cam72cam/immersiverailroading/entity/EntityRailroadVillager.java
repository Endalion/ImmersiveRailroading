package cam72cam.immersiverailroading.entity;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import cam72cam.immersiverailroading.util.BlockUtil;
import cam72cam.immersiverailroading.util.VecUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class EntityRailroadVillager extends EntityCreature {
	
	private static final DataParameter<ItemStack> CARRYING = EntityDataManager.<ItemStack>createKey(EntityRailroadVillager.class, DataSerializers.ITEM_STACK);
    public static final UUID EMPTYUUID = new UUID(0,0);
    @Nonnull
    protected UUID OwnerID = EMPTYUUID;
    public boolean followingOwner;
    
    //private int professionLevel = 0;
    //private int railroadProfession = 0;
    
	protected ItemStackHandler handInventory = new ItemStackHandler(1){
        @Override
        protected void onContentsChanged(int slot) {
            // We need to tell the client that something has changed so
            // that the render is updated
        	EntityRailroadVillager.this.onInventoryChanged();
        }
    };
	
	public EntityRailroadVillager(World worldIn) {
		super(worldIn);
        this.setSize(0.6F, 1.95F);
	}
	
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(CARRYING, ItemStack.EMPTY);
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
    		player.inventoryContainer.detectAndSendChanges();
    		this.setCustomNameTag("Porter [" + player.getName() + "]");
    		OwnerID = player.getGameProfile().getId();
    		return true;
    	}
    	else if (this.isOwner(player) && player.isSneaking()) {
    		//followingOwner = true;
    		return true;
    	}
		return false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
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
    
    private void onInventoryChanged() {
    	 if(!this.world.isRemote) dataManager.set(CARRYING, this.getHeldItemMainhand());
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
    public void notifyDataManagerChange(DataParameter<?> key)
    {
        super.notifyDataManagerChange(key);
        if (this.world.isRemote && key.equals(CARRYING))
        {
            handInventory.setStackInSlot(0, dataManager.get(CARRYING));
        }
    }
	
	@Override
	public ItemStack getHeldItemMainhand()
    {
		return this.handInventory.getStackInSlot(0).copy();
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
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		if (world.isRemote) {
			return;
		}
		
        this.world.spawnEntity(new EntityItem(this.world, this.posX, this.posY, this.posZ, this.getHeldItemMainhand().copy()));
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
    
    @Override
    public boolean canBeLeashedTo(EntityPlayer player)
    {
        return false;
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
	
    @Override
	public boolean attackEntityAsMob(Entity entityIn)
    {
    	return false;
    }
}
