package cam72cam.immersiverailroading.util;

import java.util.ArrayList;
import java.util.List;
import cam72cam.immersiverailroading.Config.ConfigBalance;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class BurnUtil {
	
	public static int getBurnTime(ItemStack stack) {
		return TileEntityFurnace.getItemBurnTime(stack);
	}
	
	public static int getBurnTime(Fluid fluid) {
		if (ConfigBalance.dieselFuels.containsKey(fluid.getName())) {
			return ConfigBalance.dieselFuels.get(fluid.getName());
		}
		return 0;
	}
	
	public static int getFissionTime(ItemStack stack) {
		if (stack.isEmpty()) return 0;
		if (ConfigBalance.nuclearFuels.containsKey(stack.getItem().getUnlocalizedName())) {
			return ConfigBalance.nuclearFuels.get(stack.getItem().getUnlocalizedName());
		}
		for (int id : OreDictionary.getOreIDs(stack))
		{
			String oreName = OreDictionary.getOreName(id);
			if (ConfigBalance.nuclearFuels.containsKey(oreName)) return ConfigBalance.nuclearFuels.get(oreName);
		}
		return 0;
	}
	
	public static List<Fluid> burnableFluids() {
		List<Fluid> values = new ArrayList<Fluid>();
		for (String name : ConfigBalance.dieselFuels.keySet()) {
			Fluid found = FluidRegistry.getFluid(name);
			if (found != null) {
				values.add(found);
			}
		}
		return values;
	}
}
