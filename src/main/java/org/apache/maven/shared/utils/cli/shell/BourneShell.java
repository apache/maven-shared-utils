package org.apache.maven.shared.utils.cli.shell;

/**
 * @author Jason van Zyl
 */
public class BourneShell
    extends Shell
{
    public BourneShell()
    {
        setShellCommand( "/bin/sh" );
        setShellArgs( new String[]{"-c"} );
    }
}
