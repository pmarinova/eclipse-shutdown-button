package pm.eclipse.shutdownbutton;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.DebugCommandHandler;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import pm.eclipse.shutdownbutton.preferences.Preferences;

public class ShutdownCommandHandler extends DebugCommandHandler {

	@Override
	protected Class<ITerminateHandler> getCommandType() {
		return ITerminateHandler.class;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			throw new ExecutionException("No active workbench window."); //$NON-NLS-1$
		}

		ISelection selection = getContextService(window).getActiveContext();
		if (selection instanceof IStructuredSelection && isEnabled()) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.getFirstElement() instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable)ss.getFirstElement();
				IProcess process = adaptable.getAdapter(IProcess.class);
				shutdownProcess(process);
			}
		}

		return null;
	}
	
	private void shutdownProcess(IProcess process) throws ExecutionException {
		Preferences prefs = Preferences.getPreferences();
		String shutdownCommand = prefs.getShutdownCommand();
		int shutdownTimeout = prefs.getShutdownTimeout();
		
		if (shutdownCommand.isEmpty())
			throw new ExecutionException("Shutdown command not defined.");
		
		String processId = process.getAttribute(IProcess.ATTR_PROCESS_ID);
		shutdownCommand = shutdownCommand.replace("${pid}", processId); //TODO
		
		ShutdownJob.run(shutdownCommand, shutdownTimeout);
	}
	
	private IDebugContextService getContextService(IWorkbenchWindow window) {
		return DebugUITools.getDebugContextManager().getContextService(window);
	}
}
