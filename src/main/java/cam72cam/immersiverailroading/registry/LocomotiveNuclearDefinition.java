package cam72cam.immersiverailroading.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.entity.LocomotiveDiesel;
import cam72cam.immersiverailroading.entity.LocomotiveNuclear;
import cam72cam.immersiverailroading.entity.LocomotiveSteam;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.RenderComponentType;
import cam72cam.immersiverailroading.library.ValveGearType;
import cam72cam.immersiverailroading.model.RenderComponent;
import cam72cam.immersiverailroading.util.FluidQuantity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class LocomotiveNuclearDefinition extends LocomotiveDefinition {
	
	// Shared Fields
	public ResourceLocation idle;
	public enum Transmissions { MECHANICAL , ELECTRICAL , UNDEFINED };
	private Transmissions transmission;
	
	// Mechanical Fields
	private FluidQuantity tankCapacity;
	private int maxPSI;
	private ValveGearType valveGear;
	private int numSlots;
	private int width;
	
	public Quilling quill;
	public ResourceLocation whistle;
	public ResourceLocation chuff;
	public ResourceLocation pressure;
	
	// Electrical Fields
	private FluidQuantity fuelCapacity;
	private int fuelEfficiency;
	public ResourceLocation horn;
	public boolean muliUnitCapable;
	
	public LocomotiveNuclearDefinition(String defID, JsonObject data) throws Exception {
		super(defID, data);
		
		// Handle null data
		if (tankCapacity == null) {
			tankCapacity = FluidQuantity.ZERO;
		}
	}
	
	@Override
	public void parseJson(JsonObject data) throws Exception {
		super.parseJson(data);
		JsonObject properties = data.get("properties").getAsJsonObject();
		if (properties.has("mechanical")) {
			transmission = Transmissions.MECHANICAL;
			tankCapacity = FluidQuantity.FromLiters((int) Math.ceil(properties.get("water_capacity_l").getAsInt() * internal_inv_scale));
			maxPSI = (int) Math.ceil(properties.get("max_psi").getAsInt() * internal_inv_scale);
			valveGear = ValveGearType.valueOf(properties.get("valve_gear").getAsString().toUpperCase());
			JsonObject firebox = data.get("firebox").getAsJsonObject();
			this.numSlots = (int) Math.ceil(firebox.get("slots").getAsInt() * internal_inv_scale);
			this.width = (int) Math.ceil(firebox.get("width").getAsInt() * internal_inv_scale);

			JsonObject sounds = data.has("sounds") ? data.get("sounds").getAsJsonObject() : null;

			whistle = new ResourceLocation(ImmersiveRailroading.MODID, "sounds/steam/default/whistle.ogg");
			idle = new ResourceLocation(ImmersiveRailroading.MODID, "sounds/steam/default/idle.ogg");
			chuff = new ResourceLocation(ImmersiveRailroading.MODID, "sounds/steam/default/chuff.ogg");
			pressure = new ResourceLocation(ImmersiveRailroading.MODID, "sounds/steam/default/pressure.ogg");

			boolean whistleSet = false;

			if (sounds != null) {
				if (sounds.has("whistle")) {
					whistle = new ResourceLocation(ImmersiveRailroading.MODID, sounds.get("whistle").getAsString());
					whistleSet = true;
				}

				if (sounds.has("idle")) {
					idle = new ResourceLocation(ImmersiveRailroading.MODID, sounds.get("idle").getAsString());
				}

				if (sounds.has("chuff")) {
					chuff = new ResourceLocation(ImmersiveRailroading.MODID, sounds.get("chuff").getAsString());
				}

				if (sounds.has("pressure")) {
					pressure = new ResourceLocation(ImmersiveRailroading.MODID, sounds.get("pressure").getAsString());
				}

				if (sounds.has("quilling")) {
					quill = new Quilling(sounds.get("quilling").getAsJsonArray());
					whistleSet = true;
				}
			}
			if (!whistleSet) {
				quill = new Quilling(new ResourceLocation(ImmersiveRailroading.MODID, "sounds/steam/default/quill.ogg"));
			}
		} else if (properties.has("electrical")) {
			transmission = Transmissions.ELECTRICAL;
			fuelCapacity = FluidQuantity.FromLiters((int)Math.ceil(properties.get("fuel_capacity_l").getAsInt() * internal_inv_scale * 10));
			fuelEfficiency = properties.get("fuel_efficiency_%").getAsInt();
			muliUnitCapable = properties.has("multi_unit_capable") ? properties.get("multi_unit_capable").getAsBoolean() : true;
			
			JsonObject sounds = data.has("sounds") ? data.get("sounds").getAsJsonObject() : null;
			
			if (sounds != null && sounds.has("idle")) {
				idle = new ResourceLocation(ImmersiveRailroading.MODID, sounds.get("idle").getAsString());
			} else {
				idle = new ResourceLocation(ImmersiveRailroading.MODID, "sounds/diesel/default/idle.ogg");
			}
			
			if (sounds != null && sounds.has("horn")) {
				horn = new ResourceLocation(ImmersiveRailroading.MODID, sounds.get("horn").getAsString());
			} else {
				horn = new ResourceLocation(ImmersiveRailroading.MODID, "sounds/diesel/default/horn.ogg");
			}
		} else {
			throw new NullPointerException("Nuclear locomotive has no transmission!");
		}
	}

	@Override
	public EntityRollingStock instance(World world) {
		return new LocomotiveNuclear(world, defID, transmission);
	}
	
	@Override
	protected boolean unifiedBogies() {
		return false;
	}

	@Override
	protected Set<String> parseComponents() {
		Set<String> groups = super.parseComponents();
		
		if (transmission == Transmissions.MECHANICAL) {
			switch (this.valveGear) {
			case STEPHENSON:
			case WALSCHAERTS:
			case TRI_WALSCHAERTS:
			case HIDDEN:
				for (int i = 0; i < 10; i++) {
					addComponentIfExists(RenderComponent.parseID(RenderComponentType.WHEEL_DRIVER_X, this, groups, i),
							true);
				}
				break;
			case T1:
			case MALLET_WALSCHAERTS:
				for (int i = 0; i < 10; i++) {
					addComponentIfExists(
							RenderComponent.parseID(RenderComponentType.WHEEL_DRIVER_FRONT_X, this, groups, i), true);
					addComponentIfExists(
							RenderComponent.parseID(RenderComponentType.WHEEL_DRIVER_REAR_X, this, groups, i), true);
				}
				;
				addComponentIfExists(RenderComponent.parse(RenderComponentType.FRONT_LOCOMOTIVE, this, groups), true);
				break;
			case CLIMAX:
				break;
			case SHAY:
				break;
			}

			for (int i = 0; i < 20; i++) {
				addComponentIfExists(RenderComponent.parseID(RenderComponentType.BOILER_SEGMENT_X, this, groups, i),
						true);
			}

			for (int i = 0; i < 20; i++) {
				addComponentIfExists(RenderComponent.parseID(RenderComponentType.PARTICLE_CHIMNEY_X, this, groups, i),
						false);
				addComponentIfExists(RenderComponent.parseID(RenderComponentType.PRESSURE_VALVE_X, this, groups, i),
						false);
			}

			addComponentIfExists(RenderComponent.parse(RenderComponentType.FIREBOX, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.SMOKEBOX, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.STEAM_CHEST_FRONT, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.STEAM_CHEST_REAR, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.STEAM_CHEST, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.PIPING, this, groups), true);

			List<String> sides = new ArrayList<String>();

			switch (this.valveGear) {
			case TRI_WALSCHAERTS:
				sides.add("CENTER");
			case STEPHENSON:
			case WALSCHAERTS:
				sides.add("RIGHT");
				sides.add("LEFT");
			case T1:
			case MALLET_WALSCHAERTS:
				if (sides.size() == 0) {
					sides.add("LEFT_FRONT");
					sides.add("RIGHT_FRONT");
					sides.add("LEFT_REAR");
					sides.add("RIGHT_REAR");
				}

				RenderComponentType[] components = new RenderComponentType[] { RenderComponentType.SIDE_ROD_SIDE,
						RenderComponentType.MAIN_ROD_SIDE, RenderComponentType.PISTON_ROD_SIDE,
						RenderComponentType.CYLINDER_SIDE,
						RenderComponentType.UNION_LINK_SIDE, RenderComponentType.COMBINATION_LEVER_SIDE,
						RenderComponentType.VALVE_STEM_SIDE, RenderComponentType.RADIUS_BAR_SIDE,
						RenderComponentType.EXPANSION_LINK_SIDE, RenderComponentType.ECCENTRIC_ROD_SIDE,
						RenderComponentType.ECCENTRIC_CRANK_SIDE, RenderComponentType.REVERSING_ARM_SIDE,
						RenderComponentType.LIFTING_LINK_SIDE, RenderComponentType.REACH_ROD_SIDE, };

				for (String side : sides) {
					for (RenderComponentType name : components) {
						addComponentIfExists(RenderComponent.parseSide(name, this, groups, side), true);
					}
				}
			case CLIMAX:
				break;
			case SHAY:
				break;
			case HIDDEN:
				break;
			}
		} else if (transmission == Transmissions.ELECTRICAL) {
			addComponentIfExists(RenderComponent.parse(RenderComponentType.FUEL_TANK, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.ALTERNATOR, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.ENGINE_BLOCK, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.CRANKSHAFT, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.GEARBOX, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.FLUID_COUPLING, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.FINAL_DRIVE, this, groups), true);
			addComponentIfExists(RenderComponent.parse(RenderComponentType.TORQUE_CONVERTER, this, groups), true);
			for (int i = 0; i < 20; i++) {
				addComponentIfExists(RenderComponent.parseID(RenderComponentType.PISTON_X, this, groups, i), true);
				addComponentIfExists(RenderComponent.parseID(RenderComponentType.DIESEL_EXHAUST_X, this, groups, i),
						false);
				addComponentIfExists(RenderComponent.parseID(RenderComponentType.FAN_X, this, groups, i), true);
				addComponentIfExists(RenderComponent.parseID(RenderComponentType.DRIVE_SHAFT_X, this, groups, i), true);
			}
		} else {
			throw new NullPointerException("Transmission definition invalid");
		}
		
		return groups;
	}

	
	public FluidQuantity getFuelCapacity(Gauge gauge) {
		return this.fuelCapacity.scale(gauge.scale()).min(FluidQuantity.FromBuckets(1)).roundBuckets();
	}
	
	public FluidQuantity getTankCapacity(Gauge gauge) {
		return this.tankCapacity.scale(gauge.scale()).min(FluidQuantity.FromBuckets(1)).roundBuckets();
	}
	
	public int getMaxPSI() {
		return (int) this.maxPSI;
	}
	public ValveGearType getValveGear() {
		return valveGear;
	}
	
	public int getInventorySize(Gauge gauge) {
		return MathHelper.ceil(numSlots * gauge.scale());
	}

	public int getInventoryWidth(Gauge gauge) {
		return Math.max(3, MathHelper.ceil(width * gauge.scale()));
	}

	public int getFuelEfficiency() {
		return this.fuelEfficiency;
	}
	
	public Transmissions getTransmission() {
		return this.transmission;
	}
}
