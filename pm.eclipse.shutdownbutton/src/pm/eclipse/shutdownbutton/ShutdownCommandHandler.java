package pm.eclipse.shutdownbutton;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.DebugCommandHandler;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import pm.eclipse.shutdownbutton.preferences.Preferences;

public class ShutdownCommandHandler extends DebugCommandHandler implements IElementUpdater {

	public static final String COMMAND_ID = "pm.eclipse.shutdownbutton.Shutdown";
	
	@Override
	protected Class<ITerminateHandler> getCommandType() {
		return ITerminateHandler.class;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			throw new ExecutionException("No active workbench window");
		}

		IProcess process = getSelectedProcess(window);
		if (process == null) {
			throw new ExecutionException("No selected process");
		}
		
		Preferences prefs = Preferences.getPreferences();
		ShutdownJob.run(process, prefs.getShutdownCommand(), prefs.getShutdownTimeout());

		return null;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ICommandService commandService = window.getService(ICommandService.class);
		commandService.refreshElements(ShutdownCommandHandler.COMMAND_ID, null); // this will trigger a call to updateElement()
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void updateElement(UIElement element, Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IProcess process = getSelectedProcess(window);
		String tooltip = process != null ? "Shutdown process " + process.getLabel() : "Shutdown";
		element.setTooltip(tooltip);
	}
	
	private IProcess getSelectedProcess(IWorkbenchWindow window) {
		ISelection selection = getContextService(window).getActiveContext();
		if (selection instanceof IStructuredSelection && isEnabled()) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			Object element = ss.getFirstElement();
			if (element instanceof ILaunch) {
				ILaunch launch = (ILaunch)element;
				return launch.getProcesses().length > 0 ? launch.getProcesses()[0] : null;
			} else if (element instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable)ss.getFirstElement();
				return adaptable.getAdapter(IProcess.class);
			}
		}
		return null;
	}
	
	private IDebugContextService getContextService(IWorkbenchWindow window) {
		return DebugUITools.getDebugContextManager().getContextService(window);
	}
}
