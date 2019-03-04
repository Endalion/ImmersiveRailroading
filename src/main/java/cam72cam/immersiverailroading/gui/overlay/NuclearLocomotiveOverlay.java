package cam72cam.immersiverailroading.gui.overlay;

import cam72cam.immersiverailroading.entity.LocomotiveNuclear;
import cam72cam.immersiverailroading.library.GuiText;
import net.minecraft.entity.Entity;
import net.minecraftforge.fluids.Fluid;

public class NuclearLocomotiveOverlay extends LocomotiveOverlay {
	public void draw() {
		Entity riding = mc.player.getRidingEntity();
		if (riding == null) {
			return;
		}
		if (!(riding instanceof LocomotiveNuclear)) {
			return;
		}
		LocomotiveNuclear loco = (LocomotiveNuclear) riding;
		drawBackground(loco);
		drawGauge(0xAA0F0FFF, ((float)loco.getLiquidAmount())/Fluid.BUCKET_VOLUME, loco.getTankCapacity().Buckets(), "B");
		drawGauge(0x99DDDDDD, loco.getBoilerPressure(), loco.getDefinition().getMaxPSI(), "PSI");
		
		int boilerColor = 0x99d1c715;
		if (loco.getBoilerTemperature() > 95) {
			boilerColor = 0x99d16c15;
		}
		if (loco.getBoilerTemperature() > 280) {
			boilerColor = 0x99a21010;
		}
		
		drawGauge(boilerColor, loco.getBoilerTemperature(), 315 , "C");
		
		drawScalar(GuiText.LABEL_BRAKE.toString(), loco.getAirBrake()*10, 0, 10);
		drawScalar(GuiText.LABEL_THROTTLE.toString(), loco.getThrottle()*10, -10, 10);
		
		drawSpeedDisplay(loco, 20);
	}
}
