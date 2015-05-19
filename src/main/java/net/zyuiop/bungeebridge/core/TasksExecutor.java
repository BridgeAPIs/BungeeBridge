package net.zyuiop.bungeebridge.core;

import java.util.concurrent.LinkedBlockingQueue;

public class TasksExecutor implements Runnable {

	private LinkedBlockingQueue<PendingTask> pending = new LinkedBlockingQueue<>();

	public void addTask(PendingTask message) {
		pending.add(message);
	}

	@Override
	public void run() {
		while (true) {
			try {
				pending.take().run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
