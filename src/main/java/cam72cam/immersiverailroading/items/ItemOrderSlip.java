package cam72cam.immersiverailroading.items;

import java.util.List;

import javax.annotation.Nullable;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.library.ChatText;
import cam72cam.immersiverailroading.util.BlockUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

public class ItemOrderSlip extends Item{
	public static final String NAME = "item_order_slip";

	public ItemOrderSlip() {
		super();
		setUnlocalizedName(ImmersiveRailroading.MODID + ":" + NAME);
		setRegistryName(new ResourceLocation(ImmersiveRailroading.MODID, NAME));
        this.setCreativeTab(ItemTabs.MAIN_TAB);
	}

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack held = player.getHeldItem(hand);
		TileEntity targetTile = world.getTileEntity(pos);
		
		NBTTagCompound nbt = held.getTagCompound();
		if (nbt == null) { 
			player.getHeldItem(hand).setTagCompound(new NBTTagCompound());
			nbt = player.getHeldItem(hand).getTagCompound();
		}
		if (BlockUtil.isIRRail(world, pos)) {
			nbt.setLong("stock", pos.toLong());
			player.sendMessage(ChatText.RADIO_LINK.getMessage());
			return EnumActionResult.SUCCESS;
		}
		if (targetTile == null) {
			if (!nbt.hasKey("direction")) {
				nbt.setBoolean("direction", false);
			} else {
				boolean currDir = nbt.getBoolean("direction");
				nbt.setBoolean("direction", !currDir);
			}
			
			player.sendMessage(ChatText.RADIO_RELINK.getMessage());
			return EnumActionResult.SUCCESS;
		}
		if (targetTile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
			nbt.setLong("inventory", pos.toLong());
			player.sendMessage(ChatText.RADIO_LINK.getMessage());
			return EnumActionResult.SUCCESS;
		}
		
		return EnumActionResult.SUCCESS;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (stack.getTagCompound() == null) {
			return;
		}
		if (stack.getTagCompound().hasKey("inventory")) {
			BlockPos pos = BlockPos.fromLong(stack.getTagCompound().getLong("inventory"));
			tooltip.add("Inventory at: " + pos.toString());
		}
		if (stack.getTagCompound().hasKey("stock")) {
			BlockPos pos = BlockPos.fromLong(stack.getTagCompound().getLong("stock"));
			tooltip.add("Track at: " + pos.toString());
		}
		if (stack.getTagCompound().hasKey("direction")) {
			Boolean direction = stack.getTagCompound().getBoolean("direction");
			tooltip.add(direction ? "Unloading from stock" : "Loading onto stock");
		}
	}
}
