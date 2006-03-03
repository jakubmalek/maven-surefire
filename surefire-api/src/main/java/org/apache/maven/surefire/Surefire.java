package org.apache.maven.surefire;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.surefire.report.Reporter;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.suite.SurefireTestSuite;
import org.apache.maven.surefire.testset.TestSetFailedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Jason van Zyl
 * @version $Id$
 */
public class Surefire
{
    private ResourceBundle bundle = ResourceBundle.getBundle( "org.apache.maven.surefire.surefire" );

    public boolean run( List reportDefinitions, Object[] testSuiteDefinition, String testSetName,
                        ClassLoader surefireClassLoader, ClassLoader testsClassLoader )
    {
        ReporterManager reporterManager =
            new ReporterManager( instantiateReports( reportDefinitions, surefireClassLoader ) );

        int totalTests = 0;

        SurefireTestSuite suite =
            createSuiteFromDefinition( testSuiteDefinition, surefireClassLoader, testsClassLoader );

        int testCount = suite.getNumTests();
        if ( testCount > 0 )
        {
            totalTests += testCount;
        }

        reporterManager.runStarting( totalTests );

        if ( totalTests == 0 )
        {
            reporterManager.writeMessage( "There are no tests to run." );
        }
        else
        {
            suite.execute( testSetName, reporterManager, testsClassLoader );
        }

        reporterManager.runCompleted();

        return reporterManager.getNumErrors() == 0 && reporterManager.getNumFailures() == 0;
    }

    public boolean run( List reportDefinitions, List testSuiteDefinitions, ClassLoader surefireClassLoader,
                        ClassLoader testsClassLoader )
    {
        ReporterManager reporterManager =
            new ReporterManager( instantiateReports( reportDefinitions, surefireClassLoader ) );

        List suites = new ArrayList();

        int totalTests = 0;
        for ( Iterator i = testSuiteDefinitions.iterator(); i.hasNext(); )
        {
            Object[] definition = (Object[]) i.next();

            SurefireTestSuite suite = createSuiteFromDefinition( definition, surefireClassLoader, testsClassLoader );

            int testCount = suite.getNumTests();
            if ( testCount > 0 )
            {
                suites.add( suite );
                totalTests += testCount;
            }
        }

        reporterManager.runStarting( totalTests );

        if ( totalTests == 0 )
        {
            reporterManager.writeMessage( "There are no tests to run." );
        }
        else
        {
            for ( Iterator i = suites.iterator(); i.hasNext(); )
            {
                SurefireTestSuite suite = (SurefireTestSuite) i.next();
                suite.execute( reporterManager, testsClassLoader );
            }
        }

        reporterManager.runCompleted();

        return reporterManager.getNumErrors() == 0 && reporterManager.getNumFailures() == 0;
    }

    private SurefireTestSuite createSuiteFromDefinition( Object[] definition, ClassLoader surefireClassLoader,
                                                         ClassLoader testsClassLoader )
    {
        String suiteClass = (String) definition[0];
        Object[] params = (Object[]) definition[1];

        SurefireTestSuite suite = instantiateSuite( suiteClass, params, surefireClassLoader );

        try
        {
            suite.locateTestSets( testsClassLoader );
        }
        catch ( ClassNotFoundException e )
        {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch ( TestSetFailedException e )
        {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return suite;
    }

    /*
                    //TestNG needs a little config love -- TODO
//                    if ( testset instanceof TestNGXMLBattery )
//                    {
//                        TestNGXMLBattery xbat = (TestNGXMLBattery) testset;
//                        xbat.setOutputDirectory( reportsDirectory );
//                        xbat.setReporter( new TestNGReporter( reporterManager, this ) );
//                        xbat.execute( reporterManager );
//                        nbTests += xbat.getTestCount();
//                    }
    }
*/
    private List instantiateReports( List reportDefinitions, ClassLoader classLoader )
    {
        List reports = new ArrayList();

        for ( Iterator i = reportDefinitions.iterator(); i.hasNext(); )
        {
            Object[] definition = (Object[]) i.next();

            String className = (String) definition[0];
            Object[] params = (Object[]) definition[1];

            Reporter report = instantiateReport( className, params, classLoader );

            reports.add( report );
        }

        return reports;
    }

    private static Reporter instantiateReport( String className, Object[] params, ClassLoader classLoader )
    {
        return (Reporter) instantiateObject( className, params, classLoader );
    }

    public static Object instantiateObject( String className, Object[] params, ClassLoader classLoader )
    {
        Object object = null;
        try
        {
            Class clazz = classLoader.loadClass( className );

            if ( params != null )
            {
                Class[] paramTypes = new Class[params.length];

                for ( int j = 0; j < params.length; j++ )
                {
                    if ( params[j] == null )
                    {
                        paramTypes[j] = String.class;
                    }
                    else
                    {
                        paramTypes[j] = params[j].getClass();
                    }
                }

                Constructor constructor = clazz.getConstructor( paramTypes );

                object = constructor.newInstance( params );
            }
            else
            {
                object = clazz.newInstance();
            }
        }
        catch ( ClassNotFoundException e )
        {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch ( NoSuchMethodException e )
        {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch ( InstantiationException e )
        {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch ( IllegalAccessException e )
        {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch ( InvocationTargetException e )
        {
            // TODO
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return object;
    }

    private static SurefireTestSuite instantiateSuite( String suiteClass, Object[] params, ClassLoader classLoader )
    {
        return (SurefireTestSuite) instantiateObject( suiteClass, params, classLoader );
    }

    public String getResourceString( String key )
    {
        return bundle.getString( key );
    }

/* TODO: should catch this elsewhere
    if ( Modifier.isAbstract( testClass.getModifiers() ) )
    {
        return null;
    }
*/
}
