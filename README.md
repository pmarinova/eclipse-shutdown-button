# Shutdown button for Eclipse

A simple plug-in for Eclipse which adds a new "Shutdown" button next to the 
standard "Terminate" button used to terminate a run/debug launch.

## Why another button? 

The problem with the "Terminate" button is that it forcefully kills the current process,
which means that any graceful shutdown logic, like Java shutdown hooks, is not executed.
See [bug 38016](https://bugs.eclipse.org/bugs/show_bug.cgi?id=38016) for a long discussion
of the issue which is still open after 20 years. Long story short, there is no reliable 
way to implement a "soft terminate" button that will work across all platforms - 
Windows, Linux, etc., so we are stuck with the brute force kill behavior of the
"Terminate" command.

## How does the Shutdown button work?

What the "Shutdown" button does, is to add a configurable shutdown command, which can be
customized depending on the platform and/or project requirements:

![Screenshot of Preferences > Run/Debug > Shutdown Button.](/screenshots/shutdown_button_prefs.png)

The command works in the context of the currently selected process in the Debug view:

![Screenshot of Shutdown button on the toolbar.](/screenshots/shutdown_button_toolbar.png)

The process id is available via the `${pid}` variable substituted at the time the command is executed.

In the example above, the shutdown command is configured to execute [windows-kill](https://github.com/ElyDotDev/windows-kill),
an open source utility tool for Windows, which can be used to send an interrupt signal to the specified process id.
Under Linux and MacOS the default shutdown command is `kill -INT ${pid}`.

## Installation

The Shutdown button plug-in requires minimum Eclipse Platform v4.23 (2022-03).

Download the plug-in p2 repository from the [latest release](https://github.com/pmarinova/eclipse-shutdown-button/releases/latest)
and install the plug-ins from the repository archive:

![Screenshot of plug-in installation.](/screenshots/shutdown_button_installation.png)