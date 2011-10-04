package org.objectweb.proactive.core.component.adl;

import java.util.Map;

import org.objectweb.fractal.task.core.BasicScheduler;
import org.objectweb.fractal.task.core.Task;
import org.objectweb.fractal.task.core.TaskExecutionException;

/**
 * The DebugScheduler just prints the ordered list of tasks to execute
 * before executing them.
 * In all other aspects is the same as the {@link BasicScheduler} and it can be safely replaced.
 */

public class DebugScheduler extends BasicScheduler {

	@Override
	protected void doSchedule(final Task[] tasks,
			final Map<Object, Object> context) throws TaskExecutionException {
		for (final Task currentTask : tasks) {
			try {
				System.out.println("Executing task: "+ currentTask);
				currentTask.execute(context);
			} catch (final Exception e) {
				throw new TaskExecutionException(currentTask, e);
			}
		}
	}
}
