package pm.eclipse.shutdownbutton;

import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

public class ShutdownJob implements ICoreRunnable {
	
	private final String shutdownCommand;
	
	private final int shutdownTimeout;
	
	public ShutdownJob(String shutdownCommand, int shutdownTimeout) {
		this.shutdownCommand = shutdownCommand;
		this.shutdownTimeout = shutdownTimeout;
	}
	
	public String getShutdownCommand() {
		return this.shutdownCommand;
	}
	
	public int getShutdownTimeout() {
		return this.shutdownTimeout;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		
		String taskName = String.format("Executing command '%s'", this.shutdownCommand);
		monitor.beginTask(taskName, IProgressMonitor.UNKNOWN);
		
		try {
			Executor exec = new DefaultExecutor();
			exec.setWatchdog(new ExecuteWatchdog(this.shutdownTimeout));
			exec.execute(CommandLine.parse(this.shutdownCommand));
			
		} catch (IOException e) {
			e.printStackTrace(); //TODO
		} finally {
			monitor.done();
		}
	}
	
	public static void run(String shutdownCommand, int shutdownTimeout) {
		Job job = Job.create("Shutting down...", new ShutdownJob(shutdownCommand, shutdownTimeout));
		job.setUser(true);
		job.schedule();
	}
}
