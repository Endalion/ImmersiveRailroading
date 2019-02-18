package cam72cam.immersiverailroading.ai;

import java.util.UUID;

import cam72cam.immersiverailroading.entity.EntityRailroadVillager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;

public abstract class EntityAIRailroadVillager<T extends EntityRailroadVillager> extends EntityAIBase {
	
    private double movementSpeed = 1.d;
    private int timeoutCounter;
    protected BlockPos lastDestination = BlockPos.ORIGIN;
    private boolean isAboveDestination = false;
    private double maxFollowDist, maxFollowDistSq;

	protected T npc;

	public EntityAIRailroadVillager(T npc) {
		this.npc = npc;
		maxFollowDist = npc.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue() * 0.90d;
		maxFollowDistSq = maxFollowDist * maxFollowDist;
	}
    
	public void moveToPosition(BlockPos pos)
	{
		this.moveToPosition(pos, 1.0D);
	}
	
    public void moveToPosition(BlockPos pos, Double proximity)
    {
    	if (pos == null) {
    		return;
    	}
        lastDestination = pos;
        if (npc.getDistanceSqToCenter(pos.up()) > proximity)
        {
            this.isAboveDestination = false;
            ++this.timeoutCounter;
            
            if (this.timeoutCounter % 20 == 0) {
            	npc.getNavigator().tryMoveToXYZ((double)((float)pos.getX()) + 0.5D, (double)(pos.getY() + 1), (double)((float)pos.getZ()) + 0.5D, this.movementSpeed);
            }
        }
        else
        {
            this.isAboveDestination = true;
            --this.timeoutCounter;
        }
    }
    
    public void returnHome() {
    	this.moveToPosition(npc.getHomePosition());
    }

    protected boolean getIsAboveDestination()
    {
        return this.isAboveDestination;
    }
}
