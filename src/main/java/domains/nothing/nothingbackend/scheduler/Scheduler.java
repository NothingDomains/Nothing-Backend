package domains.nothing.nothingbackend.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Arsen on 20.9.16..
 */
public class Scheduler {
	private static final ScheduledExecutorService timer = Executors.newScheduledThreadPool(10, r -> new Thread(r, "NothingBackend Scheduled Task"));
	private static final Map<String, ScheduledFuture<?>> tasks = new HashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(timer::shutdownNow));
	}

	public static boolean scheduleRepeating(Runnable task, String taskName, long delay, long interval) {
		if (tasks.containsKey(taskName)) {
			return false;
		}
		tasks.put(taskName,
			timer.scheduleAtFixedRate(() -> {
				try {
					task.run();
				} catch (Exception e) {
					LOGGER.error("Error in " + taskName + " task scheduler!", e);
				}
			}, delay, interval, TimeUnit.MILLISECONDS));
		return true;
	}

	public static void delayTask(Runnable task, long delay) {
		timer.schedule(task, delay, TimeUnit.MILLISECONDS);
	}

	public static boolean cancelTask(String taskName) {
		Iterator<Map.Entry<String, ScheduledFuture<?>>> i = tasks.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, ScheduledFuture<?>> next = i.next();
			if (next.getKey().equals(taskName)) {
				next.getValue().cancel(false);
				i.remove();
				return true;
			}
		}
		return false;
	}
}

