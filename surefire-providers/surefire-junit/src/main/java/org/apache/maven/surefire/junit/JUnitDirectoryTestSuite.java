package org.apache.maven.surefire.junit;

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

import org.apache.maven.surefire.suite.AbstractDirectoryTestSuite;
import org.apache.maven.surefire.testset.SurefireTestSet;

import java.io.File;
import java.util.ArrayList;

/**
 * TODO: Description.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class JUnitDirectoryTestSuite
    extends AbstractDirectoryTestSuite
{
    public JUnitDirectoryTestSuite( File basedir, ArrayList includes, ArrayList excludes )
    {
        super( basedir, includes, excludes );
    }

    protected Object[] createConstructorArguments( String className )
    {
        return new Object[]{JUnitTestSet.class.getName(), new Object[]{className}};
    }

    protected SurefireTestSet createTestSet( String className, ClassLoader classLoader )
        throws ClassNotFoundException
    {
        return new JUnitTestSet( className, classLoader );
    }
}
