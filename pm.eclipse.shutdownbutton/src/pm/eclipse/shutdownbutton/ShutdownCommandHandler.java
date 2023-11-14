package pm.eclipse.shutdownbutton;

import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class ShutdownCommandHandler extends DebugCommandHandler {

	@Override
	protected Class<ITerminateHandler> getCommandType() {
		return ITerminateHandler.class;
	}

}
