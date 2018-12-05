package cam72cam.immersiverailroading.threading.tasks;

import java.util.concurrent.Callable;

import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;

public class StockHeightmapTask implements Callable<Void> {
	private EntityRollingStockDefinition def;
	
	public StockHeightmapTask (EntityRollingStockDefinition def) {
		this.def = def;
	}

	@Override
	public Void call() throws Exception {
		def.initHeightMap();
		return null;
	}
}
