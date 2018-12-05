package cam72cam.immersiverailroading.threading;

import com.google.common.collect.Lists;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class IRThread extends Thread {
	
	private static final long MS_PER_TICK = 50000000;
	private static final long MAX_LOST_TIME = 2000000000;
	private long lostTickTime;
	private boolean threadRunning;
	
	public IRThread (String name) {
		this.lostTickTime = 0;
		this.threadRunning = true;
		this.setName(name);
	}
	
	@Override
	public void run () {
		while (threadRunning) {
			long preTickNanos = System.nanoTime();
			if (lostTickTime > MAX_LOST_TIME) lostTickTime %= MAX_LOST_TIME;
			
			tick();
			
			long postTickNanos = System.nanoTime();
			long tickTime = postTickNanos - preTickNanos;
			long sleepTime = MS_PER_TICK - tickTime;
			
			try {
				if (sleepTime > 0) {
					if (sleepTime > lostTickTime) {
						sleepTime -= lostTickTime;
						lostTickTime = 0;
						sleep(sleepTime / 1000000L);
					} else {
						lostTickTime -= sleepTime;
					}
				} else {
					lostTickTime -= sleepTime;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		ImmersiveRailroading.info("Killed " + this.getName());
	}
	
	private void executeGameLooop () {
		MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
		if (mcServer.isServerRunning()) {
			if (mcServer.isDedicatedServer()) {
				tick();
			} else {
				if (!Minecraft.getMinecraft().isGamePaused()) {
					tick();
				}
			}
		}
	}
	
	private void tick () {
		/*try {
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}
	
	public void kill () {
		this.threadRunning = false;
	}
}
