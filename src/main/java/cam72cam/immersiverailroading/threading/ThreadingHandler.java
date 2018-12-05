package cam72cam.immersiverailroading.threading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import cam72cam.immersiverailroading.ImmersiveRailroading;

public class ThreadingHandler {
	/*public static final List<Callable<Void>> TASKS = new ArrayList<Callable<Void>>();
	public static IRThread thread;
	
	public static void addThread () {
		thread = new IRThread("IR-Thread");
		thread.start();
	}
	
	public static void killThread () {
		thread.kill();
		thread = null;
	}
	
	public static void registerTask (Callable<Void> task) {
		TASKS.add(task);
	}
	
	public static void removeTask (Callable<Void> task) {
		TASKS.remove(task);
	}*/
	
	public static void invokeTasksDirectly (List<Callable<Void>> tasks) {
		ImmersiveRailroading.THREADING_EXECUTOR.invokeAll(tasks);
	}
}
