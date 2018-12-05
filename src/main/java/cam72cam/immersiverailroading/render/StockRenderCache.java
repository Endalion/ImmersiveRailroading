package cam72cam.immersiverailroading.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import cam72cam.immersiverailroading.registry.DefinitionManager;
import cam72cam.immersiverailroading.registry.EntityRollingStockDefinition;
import cam72cam.immersiverailroading.render.entity.StockModel;
import cam72cam.immersiverailroading.threading.ThreadingHandler;
import cam72cam.immersiverailroading.threading.tasks.UploadTextureTask;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

public class StockRenderCache {
	private static Map<String, StockModel> render_cache = new HashMap<String, StockModel>();
	private static boolean isCachePrimed = false;

	public static void clearRenderCache() {
		for (StockModel model : render_cache.values()) {
			model.freeGL();
		}
		render_cache = new HashMap<String, StockModel>();
		isCachePrimed = false;
	}

	private static void tryPrimeRenderCache() {
		if(isCachePrimed) {
			return;
		}
		isCachePrimed = true;
		
		ProgressBar origBar = null;
		Iterator<ProgressBar> itr = ProgressManager.barIterator();
		while (itr.hasNext()) {
			origBar = itr.next();
		}
		
		//This is terrible, I am sorry
		ProgressManager.pop(origBar);
		
		List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		DefinitionManager.getDefinitionNames().forEach((e) -> tasks.add(new UploadTextureTask(e)));
		ThreadingHandler.invokeTasksDirectly(tasks);
	}

	public static StockModel getRender(String defID) {
		if (!render_cache.containsKey(defID)) {
			EntityRollingStockDefinition def = DefinitionManager.getDefinition(defID);
			if (def != null) {
				render_cache.put(defID, new StockModel(def.getModel(), def.textureNames.keySet()));
			}
		}
		return render_cache.get(defID);
	}

	public static void tryPrime() {
		tryPrimeRenderCache();
	}
}
