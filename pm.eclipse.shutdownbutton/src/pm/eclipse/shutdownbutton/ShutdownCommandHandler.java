package pm.eclipse.shutdownbutton;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextManager;
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

public class ShutdownCommandHandler extends AbstractHandler implements IElementUpdater {
	
	public static final String COMMAND_ID = "pm.eclipse.shutdownbutton.Shutdown";
	
	private final IDebugContextListener debugContextListener = (DebugContextEvent e) -> {	
		updateEnabledState(e.getContext());
		updateCommand();
	};
	
	public ShutdownCommandHandler() {
		setBaseEnabled(false);
		getDebugContextManager().addDebugContextListener(this.debugContextListener);
	}
	
	@Override
	public void dispose() {
		getDebugContextManager().removeDebugContextListener(this.debugContextListener);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			throw new ExecutionException("No active workbench window");
		}
		
		ISelection selection = getDebugContextService(window).getActiveContext();
		IProcess process = getSelectedProcess(selection);
		if (process == null) {
			throw new ExecutionException("No selected process");
		}
		
		Preferences prefs = Preferences.getPreferences();
		ShutdownJob.run(process, prefs.getShutdownCommand(), prefs.getShutdownTimeout());
		
		return null;
	}

	private void updateEnabledState(ISelection selection) {
		IProcess process = getSelectedProcess(selection);
		boolean enabled = (process != null) ? process.canTerminate() : false;
		setBaseEnabled(enabled);
	}
	
	private void updateCommand() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ICommandService commandService = window.getService(ICommandService.class);
		commandService.refreshElements(ShutdownCommandHandler.COMMAND_ID, null); // this will trigger a call to updateElement()
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void updateElement(UIElement element, Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ISelection selection = getDebugContextService(window).getActiveContext();
		IProcess process = getSelectedProcess(selection);
		String tooltip = (process != null && process.canTerminate()) ? "Shutdown process " + process.getLabel() : "Shutdown";
		element.setTooltip(tooltip);
	}
	
	private IProcess getSelectedProcess(ISelection selection) {
		if ((selection instanceof IStructuredSelection)) {
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
	
	private static IDebugContextManager getDebugContextManager() {
		return DebugUITools.getDebugContextManager();
	}
	
	private static IDebugContextService getDebugContextService(IWorkbenchWindow window) {
		return getDebugContextManager().getContextService(window);
	}
}
