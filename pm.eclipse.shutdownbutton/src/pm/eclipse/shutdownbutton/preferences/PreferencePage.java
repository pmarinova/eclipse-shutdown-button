package pm.eclipse.shutdownbutton.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private StringFieldEditor shutdownCommand;
	private IntegerFieldEditor shutdownTimeout;
	
	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Preferences.getPreferenceStore());
		setDescription("Configures the shutdown button command");
	}
	
	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		
		this.shutdownCommand = new StringFieldEditor(
				Preferences.PREF_SHUTDOWN_COMMAND, 
				"Shutdown command:", 
				getFieldEditorParent());
		
		this.shutdownTimeout = new IntegerFieldEditor(
				Preferences.PREF_SHUTDOWN_TIMEOUT, 
				"Shutdown timeout:", 
				getFieldEditorParent());
		
		addField(this.shutdownCommand);
		addField(this.shutdownTimeout);
	}
}
