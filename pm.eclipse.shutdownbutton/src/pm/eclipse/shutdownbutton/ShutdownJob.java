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
			Executor exec = new DefaultExecutor();
			exec.setWatchdog(new ExecuteWatchdog(this.shutdownTimeout));
			exec.execute(cmdLine);
			
		} catch (IOException e) {
			String message = "Shutdown command failed";
			throw new CoreException(Status.error(message, e));
		} finally {
			monitor.done();
		}
	}
	
	public static void run(
			IProcess targetProcess, 
			String shutdownCommand, 
			int shutdownTimeout) {
		Job job = Job.create("Process shutdown", new ShutdownJob(
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
}
