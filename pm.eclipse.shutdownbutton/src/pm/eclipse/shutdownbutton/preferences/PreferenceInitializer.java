package pm.eclipse.shutdownbutton.preferences;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultPrefs = DefaultScope.INSTANCE.getNode(Preferences.PREFS_QUALIFIER);
		defaultPrefs.put(Preferences.PREF_SHUTDOWN_COMMAND, getDefaultShutdownCommand());
		defaultPrefs.putInt(Preferences.PREF_SHUTDOWN_TIMEOUT, 3000);
	}

	private static String getDefaultShutdownCommand() {
		switch (Platform.getOS()) {
			case Platform.OS_LINUX:
			case Platform.OS_MACOSX:
				return "kill -INT ${pid}";
			default:
				return "";
		}
	}
}
