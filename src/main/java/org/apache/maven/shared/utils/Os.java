package org.apache.maven.shared.utils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.Locale;

/**
 * Condition that tests the OS type.
 *
 * @author Stefan Bodewig
 * @author Magesh Umasankar
 * @author Brian Fox
 * @since 1.0
 * @version $Revision$
 */
public class Os
{
    // define the families for easier reference
    public static final String FAMILY_DOS = "dos";

    public static final String FAMILY_MAC = "mac";

    public static final String FAMILY_NETWARE = "netware";

    public static final String FAMILY_OS2 = "os/2";

    public static final String FAMILY_TANDEM = "tandem";

    public static final String FAMILY_UNIX = "unix";

    public static final String FAMILY_WINDOWS = "windows";

    public static final String FAMILY_WIN9X = "win9x";

    public static final String FAMILY_ZOS = "z/os";

    public static final String FAMILY_OS400 = "os/400";

    public static final String FAMILY_OPENVMS = "openvms";

    // get the current info
    private static final String PATH_SEP = System.getProperty( "path.separator" );

    public static final String OS_NAME = System.getProperty( "os.name" ).toLowerCase( Locale.US );

    public static final String OS_ARCH = System.getProperty( "os.arch" ).toLowerCase( Locale.US );

    public static final String OS_VERSION = System.getProperty( "os.version" ).toLowerCase( Locale.US );

    private String family;

    private String name;

    private String version;

    private String arch;

    /**
     * Default constructor
     */
    public Os()
    {
    }

    /**
     * Constructor that sets the family attribute
     * 
     * @param family a String value
     */
    public Os( String family )
    {
        setFamily( family );
    }

    /**
     * Sets the desired OS family type
     * 
     * @param f The OS family type desired<br />
     *            Possible values:<br />
     *            <ul>
     *            <li>dos</li>
     *            <li>mac</li>
     *            <li>netware</li>
     *            <li>os/2</li>
     *            <li>tandem</li>
     *            <li>unix</li>
     *            <li>windows</li>
     *            <li>win9x</li>
     *            <li>z/os</li>
     *            <li>os/400</li>
     *            <li>openvms</li>
     *            </ul>
     */
    public void setFamily( String f )
    {
        family = f.toLowerCase( Locale.US );
    }

    /**
     * Sets the desired OS name
     * 
     * @param name The OS name
     */
    public void setName( String name )
    {
        this.name = name.toLowerCase( Locale.US );
    }

    /**
     * Sets the desired OS architecture
     * 
     * @param arch The OS architecture
     */
    public void setArch( String arch )
    {
        this.arch = arch.toLowerCase( Locale.US );
    }

    /**
     * Sets the desired OS version
     * 
     * @param version The OS version
     */
    public void setVersion( String version )
    {
        this.version = version.toLowerCase( Locale.US );
    }

    /**
     * Determines if the current OS matches the type of that
     * set in setFamily.
     * 
     * @see Os#setFamily(String)
     */
    public boolean eval()
        throws Exception
    {
        return isOs( family, name, arch, version );
    }

    /**
     * Determines if the current OS matches the given OS
     * family.
     * 
     * @param family the family to check for
     * @return true if the OS matches
     * @since 1.0
     */
    public static boolean isFamily( String family )
    {
        return isOs( family, null, null, null );
    }

    /**
     * Determines if the current OS matches the given OS
     * name.
     * 
     * @param name the OS name to check for
     * @return true if the OS matches
     * @since 1.0
     */
    public static boolean isName( String name )
    {
        return isOs( null, name, null, null );
    }

    /**
     * Determines if the current OS matches the given OS
     * architecture.
     * 
     * @param arch the OS architecture to check for
     * @return true if the OS matches
     * @since 1.0
     */
    public static boolean isArch( String arch )
    {
        return isOs( null, null, arch, null );
    }

    /**
     * Determines if the current OS matches the given OS
     * version.
     * 
     * @param version the OS version to check for
     * @return true if the OS matches
     * @since 1.0
     */
    public static boolean isVersion( String version )
    {
        return isOs( null, null, null, version );
    }

    /**
     * Determines if the current OS matches the given OS
     * family, name, architecture and version.
     * 
     * The name, archictecture and version are compared to
     * the System properties os.name, os.version and os.arch
     * in a case-independent way.
     * 
     * @param family The OS family
     * @param name The OS name
     * @param arch The OS architecture
     * @param version The OS version
     * @return true if the OS matches
     * @since 1.0
     */
    public static boolean isOs( String family, String name, String arch, String version )
    {
        boolean retValue = false;

        if ( family != null || name != null || arch != null || version != null )
        {

            boolean isFamily = true;
            boolean isName = true;
            boolean isArch = true;
            boolean isVersion = true;

            if ( family != null )
            {
                if ( family.equalsIgnoreCase( FAMILY_WINDOWS ) )
                {
                    isFamily = OS_NAME.indexOf( FAMILY_WINDOWS ) > -1;
                }
                else if ( family.equalsIgnoreCase( FAMILY_OS2 ) )
                {
                    isFamily = OS_NAME.indexOf( FAMILY_OS2 ) > -1;
                }
                else if ( family.equalsIgnoreCase( FAMILY_NETWARE ) )
                {
                    isFamily = OS_NAME.indexOf( FAMILY_NETWARE ) > -1;
                }
                else if ( family.equalsIgnoreCase( FAMILY_DOS ) )
                {
                    isFamily = PATH_SEP.equals( ";" ) && !isFamily( FAMILY_NETWARE );
                }
                else if ( family.equalsIgnoreCase( FAMILY_MAC ) )
                {
                    isFamily = OS_NAME.indexOf( FAMILY_MAC ) > -1;
                }
                else if ( family.equalsIgnoreCase( FAMILY_TANDEM ) )
                {
                    isFamily = OS_NAME.indexOf( "nonstop_kernel" ) > -1;
                }
                else if ( family.equalsIgnoreCase( FAMILY_UNIX ) )
                {
                    isFamily = PATH_SEP.equals( ":" ) && !isFamily( FAMILY_OPENVMS )
                        && ( !isFamily( FAMILY_MAC ) || OS_NAME.endsWith( "x" ) );
                }
                else if ( family.equalsIgnoreCase( FAMILY_WIN9X ) )
                {
                    isFamily = isFamily( FAMILY_WINDOWS )
                        && ( OS_NAME.indexOf( "95" ) >= 0 || OS_NAME.indexOf( "98" ) >= 0
                            || OS_NAME.indexOf( "me" ) >= 0 || OS_NAME.indexOf( "ce" ) >= 0 );
                }
                else if ( family.equalsIgnoreCase( FAMILY_ZOS ) )
                {
                    isFamily = OS_NAME.indexOf( FAMILY_ZOS ) > -1 || OS_NAME.indexOf( "os/390" ) > -1;
                }
                else if ( family.equalsIgnoreCase( FAMILY_OS400 ) )
                {
                    isFamily = OS_NAME.indexOf( FAMILY_OS400 ) > -1;
                }
                else if ( family.equalsIgnoreCase( FAMILY_OPENVMS ) )
                {
                    isFamily = OS_NAME.indexOf( FAMILY_OPENVMS ) > -1;
                }
                else
                {
                    isFamily = OS_NAME.indexOf( family.toLowerCase( Locale.US ) ) > -1;
                }
            }
            if ( name != null )
            {
                isName = name.toLowerCase( Locale.US ).equals( OS_NAME );
            }
            if ( arch != null )
            {
                isArch = arch.toLowerCase( Locale.US ).equals( OS_ARCH );
            }
            if ( version != null )
            {
                isVersion = version.toLowerCase( Locale.US ).equals( OS_VERSION );
            }
            retValue = isFamily && isName && isArch && isVersion;
        }
        return retValue;
    }

}
