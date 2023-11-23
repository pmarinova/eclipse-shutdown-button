package pm.eclipse.shutdownbutton;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IProcess;

public class ShutdownJob implements ICoreRunnable {
	
	private final IProcess targetProcess;
	
	private final String shutdownCommand;
	
	private final int shutdownTimeout;
	
	public ShutdownJob(
			IProcess targetProcess, 
			String shutdownCommand, 
			int shutdownTimeout) {
		this.targetProcess = targetProcess;
		this.shutdownCommand = shutdownCommand;
		this.shutdownTimeout = shutdownTimeout;
	}
	
	public IProcess getTargetProcess() {
		return targetProcess;
	}
	
	public String getShutdownCommand() {
		return this.shutdownCommand;
	}
	
	public int getShutdownTimeout() {
		return this.shutdownTimeout;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		
		CommandLine cmdLine = processShutdownCommand();
		String cmdLineString = String.join(" ", cmdLine.toStrings());
		
		String taskName = String.format("Executing command '%s'", cmdLineString);
		monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
		
		try {
			Executor executor = new DefaultExecutor();
			ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
			
			executor.setWatchdog(watchdog);
			executor.execute(cmdLine, resultHandler);
			
			waitForShutdown(this.targetProcess, this.shutdownTimeout, monitor);	
			
			if (!this.targetProcess.isTerminated()) {
				if (monitor.isCanceled()) {
					// shutdown cancelled, kill shutdown process and finish job
					watchdog.destroyProcess();
					resultHandler.waitFor();
					return;
				} else {
					// shutdown timed out, show the timeout error to the user
					throw new CoreException(Status.error("Shutdown timeout"));
				} 
			}
			
		} catch (IOException e) {
			throw new CoreException(Status.error("Shutdown command failed", e));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			monitor.done();
		}
	}
	
	public static void run(
			IProcess targetProcess, 
			String shutdownCommand, 
			int shutdownTimeout) {
		Job job = Job.create("Shutdown process", new ShutdownJob(
				targetProcess, shutdownCommand, shutdownTimeout));
		job.setUser(true);
		job.schedule();
	}
	
	private CommandLine processShutdownCommand() throws CoreException {
		Map<String,String> variables = new HashMap<>();
		variables.put("pid", this.targetProcess.getAttribute(IProcess.ATTR_PROCESS_ID));
		
		try {
			CommandLine cmdLine = CommandLine.parse(this.shutdownCommand, variables);
			cmdLine.toStrings();
			return cmdLine;
		} catch (Exception e) {
			String message = String.format("Invalid shutdown command:\n\"%s\"", this.shutdownCommand);
			throw new CoreException(Status.error(message, e));
		}
	}
	
	private static void waitForShutdown(IProcess process, int timeout, IProgressMonitor monitor) throws InterruptedException {
		long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeout);
		long startTime = System.nanoTime();
		long remainingTime = timeoutNanos;
		while (!process.isTerminated() && !monitor.isCanceled() && remainingTime > 0) {
			Thread.sleep(100); // wait for graceful shutdown
			remainingTime = timeoutNanos - (System.nanoTime() - startTime);
		}
	}
}
