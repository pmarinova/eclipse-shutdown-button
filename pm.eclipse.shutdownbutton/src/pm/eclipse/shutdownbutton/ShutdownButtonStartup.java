package pm.eclipse.shutdownbutton;

import org.eclipse.ui.IStartup;

public class ShutdownButtonStartup implements IStartup {

	@Override
	public void earlyStartup() {
		// Nothing to do here.
		// If we don't register for early startup, the shutdown command handler will not 
		// be loaded and the shutdown command enabled state will be wrong at startup.
	}

}
