package cam72cam.immersiverailroading.ai;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.entity.Freight;
import cam72cam.immersiverailroading.entity.NpcConveyor;
import cam72cam.immersiverailroading.library.ChatText;
import cam72cam.immersiverailroading.util.VecUtil;
import ibxm.Player;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class NpcAIConveyor extends EntityAIRailroadVillager<NpcConveyor> {

	protected BlockPos pathDestination;
	private int updateTimer = 0;
	//Flow direction reverser
	protected boolean unloadFromStock = true;
	
	Capability<IItemHandler> item_cap = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
	private IItemHandlerModifiable dest;
	private IItemHandlerModifiable mid;
	private IItemHandlerModifiable src;
	
	public enum Tasks {
		IDLE, REST, PICKUP, DEPOSIT, FOLLOW
	}
	private Tasks currentTask = Tasks.IDLE;
	
	
    public NpcAIConveyor(NpcConveyor npc, double speedIn, int length)
    {
    	super(npc);
        this.setMutexBits(5);
    }
    
	@Override
	public boolean shouldExecute() {
		if (npc.targetFreight != null && npc.inventoryPosition != null && npc.unloadDirection != null && !npc.world.isRemote) {
			return true;
		}
		return false;
	}

	public boolean shouldContinueExecuting()
    {
		if (this.unloadFromStock != npc.unloadDirection) return false;
		return shouldExecute();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
    	ImmersiveRailroading.info("Conveyor AI init");
    	currentTask = Tasks.PICKUP;
    	this.unloadFromStock = npc.unloadDirection;
    	mid = (IItemHandlerModifiable) npc.getCapability(item_cap, null);
    	
    	TileEntity targetTile = npc.world.getTileEntity(npc.inventoryPosition);
		if(targetTile == null) {
			npc.inventoryPosition = null;
			return;
		}
		if(!targetTile.hasCapability(item_cap, null)) {
			npc.inventoryPosition = null;
			return;
		}
		
    	if (unloadFromStock) {
    		src = (IItemHandlerModifiable) npc.targetFreight.getCapability(item_cap, null);
    		dest = (IItemHandlerModifiable) targetTile.getCapability(item_cap, null);
        	pathDestination = npc.getPositionBesideStock(npc.targetFreight);
    	}
    	else {
    		src = (IItemHandlerModifiable) targetTile.getCapability(item_cap, null);
    		dest = (IItemHandlerModifiable) npc.targetFreight.getCapability(item_cap, null);
        	pathDestination = npc.inventoryPosition;
    	}
    	ImmersiveRailroading.info("Conveyor AI start");
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
    	
    	if (npc.targetFreight == null || npc.inventoryPosition == null) return;
    	
		if (npc.followingOwner && npc.getOwner() != null) {
			currentTask = Tasks.FOLLOW;
		}

    	updateTimer--;
    	if (updateTimer <= 0) {
    		ImmersiveRailroading.info("Conveyor currently %s, dest %s", currentTask.toString(), pathDestination.toString());
			switch (currentTask) {
			case IDLE:
				break;
			case FOLLOW:
				break;
			case PICKUP:
				if (canInteractDestination()) {
					transferItem(src, mid, 1);
					ImmersiveRailroading.info("Conveyor pulled, has %s", npc.getHeldItemMainhand().toString());
					if (!npc.getHeldItemMainhand().isEmpty())npc.world.playSound((EntityPlayer)null, npc.posX, npc.posY, npc.posZ, SoundEvents.ENTITY_ITEM_PICKUP, npc.getSoundCategory(), 1.0F, 0.8F);
				}
				if (!npc.getHeldItemMainhand().isEmpty()){
					currentTask = Tasks.DEPOSIT;
		        	if (!unloadFromStock) {
		        		pathDestination = npc.getPositionBesideStock(npc.targetFreight);
		        	} else {
		        		pathDestination = npc.inventoryPosition;
		        	}
				}
				break;
			case DEPOSIT:
				if (canInteractDestination()) {
					transferItem(mid, dest, 1);
					ImmersiveRailroading.info("Conveyor pushed, has %s", npc.getHeldItemMainhand().toString());
				}
				if (npc.getHeldItemMainhand().isEmpty()){
					currentTask = Tasks.PICKUP;
					if (unloadFromStock) {
		        		pathDestination = npc.getPositionBesideStock(npc.targetFreight);
		        	} else {
		        		pathDestination = npc.inventoryPosition;
		        	}
				}
				break;
			default:
        	}
    		updateTimer = 20;
    	}
        if (!getIsAboveDestination() || lastDestination != pathDestination) {
        	moveToPosition(pathDestination);
        }
    }
	
	public boolean canInteractDestination() {
		switch (currentTask) {
		case IDLE:
		case FOLLOW:
			return false;
		case PICKUP:
	        	if (unloadFromStock) {
	        		return npc.getDistanceSq(pathDestination) < 3.0;
	        	} else {
	        		return npc.getDistanceSq(npc.inventoryPosition) < 16.0;
	        	}
		case DEPOSIT:
				if (!unloadFromStock) {
					return npc.getDistanceSq(pathDestination) < 3.0;
	        	} else {
	        		return npc.getDistanceSq(npc.inventoryPosition) < 16.0;
	        	}
		default:
			return false;
		}
	}
    
	//Reserved for lockable stock
	protected boolean canAccessStock() {
		return true;
	}
	
	protected boolean transferItem(IItemHandler source, IItemHandler dest, int numstacks) {
		if (!canAccessStock()) return false;
		npc.transferAllItems(source, dest, numstacks);
		return true;
	}
	
	protected boolean transferFluid() {
		if (!canAccessStock()) return false;
		return false;
	}
}
