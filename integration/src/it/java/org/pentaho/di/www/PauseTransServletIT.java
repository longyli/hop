/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.www;

import static junit.framework.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.apache.hop.core.gui.Point;
import org.apache.hop.core.logging.HopLogStore;
import org.apache.hop.core.logging.LogChannelInterface;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;

public class PauseTransServletIT {
  private TransformationMap mockTransformationMap;

  private PauseTransServlet pauseTransServlet;

  @Before
  public void setup() {
    mockTransformationMap = mock( TransformationMap.class );
    pauseTransServlet = new PauseTransServlet( mockTransformationMap );
  }

  @Test
  public void testPauseTransServletEscapesHtmlWhenTransNotFound() throws ServletException, IOException {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );

    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( PauseTransServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    pauseTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( ServletTestUtils.hasBadText( ServletTestUtils.getInsideOfTag( "H1", out.toString() ) ) );
  }

  @Test
  public void testPauseTransServletEscapesHtmlWhenTransFound() throws ServletException, IOException {
    HopLogStore.init();
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    Trans mockTrans = mock( Trans.class );
    TransMeta mockTransMeta = mock( TransMeta.class );
    LogChannelInterface mockChannelInterface = mock( LogChannelInterface.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( PauseTransServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( anyString() ) ).thenReturn( ServletTestUtils.BAD_STRING );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );
    when( mockTransformationMap.getTransformation( any( CarteObjectEntry.class ) ) ).thenReturn( mockTrans );
    when( mockTrans.getLogChannel() ).thenReturn( mockChannelInterface );
    when( mockTrans.getTransMeta() ).thenReturn( mockTransMeta );
    when( mockTransMeta.getMaximum() ).thenReturn( new Point( 10, 10 ) );

    pauseTransServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    assertFalse( out.toString().contains( ServletTestUtils.BAD_STRING ) );
  }
}
