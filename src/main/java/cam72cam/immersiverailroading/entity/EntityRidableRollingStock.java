package cam72cam.immersiverailroading.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import cam72cam.immersiverailroading.entity.EntityCoupleableRollingStock.CouplerType;
import cam72cam.immersiverailroading.library.KeyTypes;
import cam72cam.immersiverailroading.net.PassengerPositionsPacket;
import cam72cam.immersiverailroading.util.BufferUtil;
import cam72cam.immersiverailroading.util.VecUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class EntityRidableRollingStock extends EntityBuildableRollingStock {
	public EntityRidableRollingStock(World world, String defID) {
		super(world, defID);
	}
	
	@Override
	public void readSpawnData(ByteBuf additionalData) {
		passengerPositions = BufferUtil.readPlayerPositions(additionalData);
		super.readSpawnData(additionalData);
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		BufferUtil.writePlayerPositions(buffer, passengerPositions);
		super.writeSpawnData(buffer);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		
		if (passengerPositions.size() > 0) {
			NBTTagCompound offsetTag = nbttagcompound.getCompoundTag("passengerOffsets");
			List<String> passengers = new ArrayList<String>();
			for (UUID passenger : passengerPositions.keySet()) {
				passengers.add(passenger.toString());
				offsetTag.setDouble(passenger.toString() + ".x", passengerPositions.get(passenger).xCoord);
				offsetTag.setDouble(passenger.toString() + ".y", passengerPositions.get(passenger).yCoord);
				offsetTag.setDouble(passenger.toString() + ".z", passengerPositions.get(passenger).zCoord);
			}
			offsetTag.setString("passengers", String.join("|", passengers));
			nbttagcompound.setTag("passengerOffsets", offsetTag);
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		
		if (nbttagcompound.hasKey("passengerOffsets")) {
			NBTTagCompound offsetTag = nbttagcompound.getCompoundTag("passengerOffsets");
			for (String passenger : offsetTag.getString("passengers").split("\\|")) {
				Vec3d pos = new Vec3d(offsetTag.getDouble(passenger + ".x"), offsetTag.getDouble(passenger + ".y"), offsetTag.getDouble(passenger + ".z"));
				passengerPositions.put(UUID.fromString(passenger), pos);
			}
		}
	}
	

	@Override
	public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
		if (super.processInitialInteract(player, stack, hand)) {
			return true;
		}
		
		if (player.isSneaking()) {
			return false;
		} else if (player.isRiding() && player.getRidingEntity().getPersistentID() == this.getPersistentID()) {
			return false;
		} else {
			if (!this.worldObj.isRemote) {
				passengerPositions.put(player.getPersistentID(), new Vec3d(0, 0, 0));
				player.startRiding(this);
			}

			return true;
		}
	}
	
	@Override
	protected boolean canFitPassenger(Entity passenger) {
		return this.getPassengers().size() < this.getDefinition().getMaxPassengers();
	}
	
	@Override
	public boolean canRiderInteract() {
		return false;
	}

	@Override
	public boolean shouldRiderSit() {
		return false;
	}

	public Map<UUID, Vec3d> passengerPositions = new HashMap<UUID, Vec3d>();
	private final double pressDist = 0.05;
	public void handleKeyPress(Entity source, KeyTypes key) {
		Vec3d movement = null;
		switch (key) {
		case PLAYER_FORWARD:
			movement = new Vec3d(pressDist, 0, 0);
			break;
		case PLAYER_BACKWARD:
			movement = new Vec3d(-pressDist, 0, 0);
			break;
		case PLAYER_LEFT:
			movement = new Vec3d(0, 0, -pressDist);
			break;
		case PLAYER_RIGHT:
			movement = new Vec3d(0, 0, pressDist);
			break;
		default:
			//ignore key
			return;
		}
		if (source.getRidingEntity() == this) {
			movement = VecUtil.rotateYaw(movement, source.getRotationYawHead());
			movement = VecUtil.rotateYaw(movement, 180-this.rotationYaw);
			
			Vec3d pos = passengerPositions.get(source.getPersistentID()).add(movement);

			
			if (this instanceof EntityCoupleableRollingStock) {
				if (this.getDefinition().isAtFront(gauge, pos) && ((EntityCoupleableRollingStock)this).isCoupled(CouplerType.FRONT)) {
					source.startRiding(((EntityCoupleableRollingStock)this).getCoupled(CouplerType.FRONT));
					return;
				}
				if (this.getDefinition().isAtRear(gauge, pos) && ((EntityCoupleableRollingStock)this).isCoupled(CouplerType.BACK)) {
					source.startRiding(((EntityCoupleableRollingStock)this).getCoupled(CouplerType.BACK));
					return;
				}
			}
			
			pos = this.getDefinition().correctPassengerBounds(gauge, pos);
			
			passengerPositions.put(source.getPersistentID(), pos);
			sendToObserving(new PassengerPositionsPacket(this));
		}
	}
	
	@Override
	protected void addPassenger(Entity passenger) {
		Vec3d ppos = new Vec3d(passenger.lastTickPosX, passenger.lastTickPosY, passenger.lastTickPosZ);
		super.addPassenger(passenger);
		
		if (!worldObj.isRemote) {
			Vec3d center = this.getDefinition().getPassengerCenter(gauge);
			center = VecUtil.rotateYaw(center, this.rotationYaw);
			center = center.add(this.getPositionVector());
			Vec3d off = VecUtil.rotateYaw(center.subtract(ppos), -this.rotationYaw);
			
			off = this.getDefinition().correctPassengerBounds(gauge, off);
			off = off.addVector(0, -off.yCoord, 0);
			
			passengerPositions.put(passenger.getPersistentID(), off);
			updatePassenger(passenger);
			sendToObserving(new PassengerPositionsPacket(this));
		}
	}
	
	@Override
	public void updatePassenger(Entity passenger) {
		if (this.isPassenger(passenger) && passengerPositions.containsKey(passenger.getPersistentID())) {
			Vec3d pos = this.getDefinition().getPassengerCenter(gauge);
			pos = pos.add(passengerPositions.get(passenger.getPersistentID()));
			pos = VecUtil.rotateYaw(pos, this.rotationYaw);
			pos = pos.add(this.getPositionVector());
			passenger.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
			
			passenger.prevRotationYaw = passenger.rotationYaw;
			passenger.rotationYaw += (this.rotationYaw - this.prevRotationYaw);
		}
	}
	
	@Override
	public void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		
		if (passengerPositions.containsKey(passenger.getPersistentID()) ) {
			Vec3d ppos = passengerPositions.get(passenger.getPersistentID());
			
			Vec3d delta = VecUtil.fromYaw(this.getDefinition().getPassengerCompartmentWidth(gauge)/2 + 1.3 * gauge.scale(), this.rotationYaw + (ppos.zCoord > 0 ? 90 : -90));
			
			ppos = ppos.add(this.getDefinition().getPassengerCenter(gauge));
			Vec3d offppos = VecUtil.rotateYaw(ppos, this.rotationYaw);
			
			delta = delta.addVector(offppos.xCoord, offppos.yCoord, 0);
			delta = delta.add(this.getPositionVector());
			
			passengerPositions.remove(passenger.getPersistentID());
			passenger.setPositionAndUpdate(delta.xCoord, passenger.posY, delta.zCoord);
		}
	}

	public void handlePassengerPositions(Map<UUID, Vec3d> passengerPositions) {
		this.passengerPositions = passengerPositions;
		for (UUID id : passengerPositions.keySet()) {
			for (Entity ent : worldObj.loadedEntityList) {
				if (ent.getPersistentID().equals(id)) {
					Vec3d pos = this.getDefinition().getPassengerCenter(gauge);
					pos = pos.add(passengerPositions.get(id));
					pos = VecUtil.rotateYaw(pos, this.rotationYaw);
					pos = pos.add(this.getPositionVector());
					ent.setPosition(pos.xCoord, pos.yCoord, pos.zCoord);
					break;
				}
			}
		}
	}
}
