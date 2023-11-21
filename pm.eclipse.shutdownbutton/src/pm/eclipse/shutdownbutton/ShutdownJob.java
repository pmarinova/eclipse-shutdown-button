package pm.eclipse.shutdownbutton;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
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
		
		Map<String,String> variables = new HashMap<>();
		variables.put("pid", this.targetProcess.getAttribute(IProcess.ATTR_PROCESS_ID));
		
		CommandLine cmdLine = CommandLine.parse(this.shutdownCommand, variables);
		String cmdLineString = String.join(" ", cmdLine.toStrings());
		
		ExecuteWatchdog watchdog = new ExecuteWatchdog(this.shutdownTimeout);
		
		try {
			String taskName = String.format("Executing command '%s'", cmdLineString);
			monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
			
			Executor exec = new DefaultExecutor();
			exec.setWatchdog(watchdog);
			exec.execute(cmdLine);
			
		} catch (IOException e) {
			e.printStackTrace(); //TODO
		} finally {
			monitor.done();
		}
	}
	
	public static void run(
			IProcess targetProcess, 
			String shutdownCommand, 
			int shutdownTimeout) {
		Job job = Job.create("Shutting down...", new ShutdownJob(
				targetProcess, shutdownCommand, shutdownTimeout));
		job.setUser(true);
		job.schedule();
	}
}
