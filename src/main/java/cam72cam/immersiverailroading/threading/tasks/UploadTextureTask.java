package cam72cam.immersiverailroading.threading.tasks;

import java.util.concurrent.Callable;

import cam72cam.immersiverailroading.ConfigGraphics;
import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.proxy.ClientProxy;
import cam72cam.immersiverailroading.render.StockRenderCache;
import cam72cam.immersiverailroading.render.entity.StockModel;

public class UploadTextureTask implements Callable<Void> {
	private String def;
	
	public UploadTextureTask(String def) {
		this.def = def;
	}

	@Override
	public Void call() throws Exception {
		ImmersiveRailroading.info(def);
		StockModel renderer = StockRenderCache.getRender(def);
		if (ConfigGraphics.enableItemRenderPriming) {
			renderer.bindTexture();
			renderer.draw();
			renderer.restoreTexture();
			ClientProxy.renderCacheLimiter.reset();
		}
		return null;
	}

}
