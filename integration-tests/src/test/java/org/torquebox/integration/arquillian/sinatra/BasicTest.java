/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.integration.arquillian.sinatra;

import static org.junit.Assert.*;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.torquebox.integration.arquillian.AbstractIntegrationTestCase;

@Run(RunModeType.AS_CLIENT)
public class BasicTest extends AbstractIntegrationTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        return createDeployment( "sinatra/basic-sinatra-knob.yml" );
    }

    @Test
    public void testSirPostALot() throws Exception {
        int numIterations = 500;
        for (int i = 0; i < numIterations; ++i) {
            if (i % 10 == 0) {
                System.err.print( "." );
            }
            try {
                postCycle();
            } catch (Exception e) {
                System.err.println( "\n\nFailed at iteration " + i + ": " + e.getMessage() + "\n" );
                throw e;
            }
        }
        System.err.println( "\nCompleted " + numIterations );
    }

    protected void postCycle() {
        driver.get( "http://localhost:8080/basic-sinatra/poster" );
        WebElement submit = driver.findElementById( "submit" );
        submit.click();
        WebElement success = driver.findElementById( "success" );
        assertNotNull( success );
        assertEquals( "you posted nothing", success.getText() );
    }

}
