package domains.nothing.nothingbackend.scheduler;

public abstract class Task implements Runnable {

	private String taskName;

	private Task() {
	}

	public Task(String taskName) {
		this.taskName = taskName;
	}

	public boolean repeat(long delay, long interval) {
		return Scheduler.scheduleRepeating(this, taskName, delay, interval);
	}

	public void delay(long delay) {
		Scheduler.delayTask(this, delay);
	}

	public boolean cancel() {
		return Scheduler.cancelTask(taskName);
	}
}
