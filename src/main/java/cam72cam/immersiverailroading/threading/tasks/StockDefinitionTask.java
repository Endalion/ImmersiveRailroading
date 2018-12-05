package cam72cam.immersiverailroading.threading.tasks;

import java.util.concurrent.Callable;

import com.google.gson.JsonObject;

import cam72cam.immersiverailroading.registry.DefinitionManager;

public class StockDefinitionTask implements Callable<Void> {
	private String defID;
	private String defType;
	
	public StockDefinitionTask(String defID, String defType) {
		this.defID = defID;
		this.defType = defType;
	}

	@Override
	public Void call() throws Exception {
		DefinitionManager.definitions.put(defID, DefinitionManager.jsonLoaders.get(defType).apply(defID, DefinitionManager.getJsonData(defID)));
		return null;
	}

}
