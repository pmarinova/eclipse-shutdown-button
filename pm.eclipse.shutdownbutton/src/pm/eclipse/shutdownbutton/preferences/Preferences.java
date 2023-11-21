package pm.eclipse.shutdownbutton.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class Preferences {
	
	public static final String PREFS_QUALIFIER = "pm.eclipse.shutdownbutton";	
	public static final String PREF_SHUTDOWN_COMMAND = "shutdown_command";
	public static final String PREF_SHUTDOWN_TIMEOUT = "shutdown_timeout";
	
	private static final Preferences INSTANCE = new Preferences();
	
	private final IPreferenceStore prefs = getPreferenceStore();
	
	private Preferences() { }
	
	public String getShutdownCommand() {
		return this.prefs.getString(PREF_SHUTDOWN_COMMAND);
	}
	
	public int getShutdownTimeout() {
		return this.prefs.getInt(PREF_SHUTDOWN_TIMEOUT);
	}
	
	public static Preferences getPreferences() {
		return INSTANCE;
	}
	
	public static IPreferenceStore getPreferenceStore() {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, PREFS_QUALIFIER);
	}
}
