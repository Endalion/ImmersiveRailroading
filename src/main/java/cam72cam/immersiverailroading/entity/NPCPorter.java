package cam72cam.immersiverailroading.entity;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import cam72cam.immersiverailroading.Config.ConfigDamage;
import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.ai.NpcAIConveyor;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class NPCPorter extends EntityRailroadVillager {

	public Freight targetFreight;
	public BlockPos inventoryPosition;
	public BlockPos tracksidePosition;
	public Boolean unloadDirection;
	
	private int freightUpdateTimer = 0;
	
	public NPCPorter(World worldIn) {
		super(worldIn);
		((PathNavigateGround)this.getNavigator()).setBreakDoors(true);
	}
	
	@Override
	protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityZombie>(this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityEvoker>(this, EntityEvoker.class, 12.0F, 0.8D, 0.8D));
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityVindicator>(this, EntityVindicator.class, 8.0F, 0.8D, 0.8D));
        this.tasks.addTask(1, new EntityAIAvoidEntity<EntityVex>(this, EntityVex.class, 8.0F, 0.6D, 0.6D));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new NpcAIConveyor(this, 1.d, 64));
        //this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityPlayer.class, 3.0F, 1.0F));
    }

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setLong("invPos", inventoryPosition.toLong());
		nbttagcompound.setLong("trkPos", tracksidePosition.toLong());
		nbttagcompound.setBoolean("unlDir", unloadDirection);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		inventoryPosition = BlockPos.fromLong(nbttagcompound.getLong("invPos"));
		tracksidePosition = BlockPos.fromLong(nbttagcompound.getLong("trkPos"));
		unloadDirection = nbttagcompound.getBoolean("unlDir");
	}
	
	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
    	//ImmersiveRailroading.info("I am on %s, I have %s", this.world.isRemote, this.getHeldItemMainhand().toString());
		//ImmersiveRailroading.info("ext %s , %s", player.getGameProfile().getId(), this.OwnerID);
		if (this.world.isRemote || !this.isOwner(player)) {
			super.processInteract(player, hand);
		}
		if (this.isOwner(player)) {
			if (player.getHeldItem(hand).getItem() == IRItems.ITEM_HOOK) {
				return true;
			} else if (player.getHeldItem(hand).getItem() == IRItems.ITEM_ORDER_SLIP) {
				NBTTagCompound nbt = player.getHeldItem(hand).getTagCompound();
				if (nbt == null) {
					player.getHeldItem(hand).setTagCompound(new NBTTagCompound());
					nbt = player.getHeldItem(hand).getTagCompound();
				}
				if (nbt.hasKey("inventory")) {
					BlockPos pos = BlockPos.fromLong(nbt.getLong("inventory"));
					this.inventoryPosition = pos;
				}
				if (nbt.hasKey("stock")) {
					BlockPos pos = BlockPos.fromLong(nbt.getLong("stock"));
					this.tracksidePosition = pos;
				}
				if (nbt.hasKey("direction")) {
					this.unloadDirection = nbt.getBoolean("direction");
				}
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onUpdate() {
		if (tracksidePosition == null) {
			super.onUpdate();
			return;
		}
		freightUpdateTimer--;
		if (freightUpdateTimer <= 0) {
			if (targetFreight == null) {
				List<Freight> nearBy = world.getEntities(Freight.class, new Predicate<Freight>() {
					@Override
					public boolean apply(@Nullable Freight entity) {
						if (entity == null) {
							return false;
						}

						if (entity.isDead) {
							return false;
						}

						if (entity.getDistanceSq(NPCPorter.this.tracksidePosition) * 4 > entity.getDefinition().getLength(entity.gauge) * entity.getDefinition().getLength(entity.gauge)) {
							return false;
						}
						
						if (entity.getCurrentSpeed().metric()/ConfigDamage.entitySpeedDamage > 1) {
							return false;
						}

						return true;
					}
				});
				if (nearBy.size() > 0) {
					targetFreight = nearBy.get(0);
				}
			} else {
				double targetFreightLengthSq = targetFreight.getDefinition().getLength(targetFreight.gauge) * targetFreight.getDefinition().getLength(targetFreight.gauge);
				if (targetFreight.getDistanceSq(NPCPorter.this.tracksidePosition) * 4 > targetFreightLengthSq || targetFreight.getCurrentSpeed().metric()/ConfigDamage.entitySpeedDamage > 1 || targetFreight.isDead) {
					targetFreight = null;
				}
			}
			freightUpdateTimer = 40;
		}
		
		super.onUpdate();
	}
	
	@Override
	protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
    }
	
}
