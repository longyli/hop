/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.job;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.base.PrivateDatabasesTestTemplate;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.Props;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.shared.SharedObjects;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * @author Andrey Khayrutdinov
 */
public class JobMetaPrivateDbTest extends PrivateDatabasesTestTemplate<JobMeta>  {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Override
  public JobMeta createMeta() {
    return new JobMeta();
  }

  @Override
  public JobMeta fromXml( String xml, final SharedObjects fakeSharedObjects ) throws Exception {
    JobMeta meta = spy( new JobMeta() );
    doAnswer( createInjectingAnswer( meta, fakeSharedObjects ) ).when( meta ).readSharedObjects();

    Document doc = XMLHandler.loadXMLFile( new ByteArrayInputStream( xml.getBytes() ), null, false, false );
    meta.loadXML( XMLHandler.getSubNode( doc, JobMeta.XML_TAG ), null, null );

    return meta;
  }

  @Override
  public String toXml( JobMeta meta ) {
    return meta.getXML();
  }


  @BeforeClass
  public static void initHop() throws Exception {
    if ( Props.isInitialized() ) {
      Props.getInstance().setOnlyUsedConnectionsSavedToXML( false );
    }
    HopEnvironment.init();
  }


  @Test
  public void onePrivate_TwoShared() throws Exception {
    doTest_OnePrivate_TwoShared();
  }

  @Test
  public void noPrivate() throws Exception {
    doTest_NoPrivate();
  }

  @Test
  public void onePrivate_NoShared() throws Exception {
    doTest_OnePrivate_NoShared();
  }
}
