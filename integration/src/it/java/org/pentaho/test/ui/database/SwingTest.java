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

package org.pentaho.test.ui.database;

import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.exception.HopException;
import org.pentaho.ui.database.DatabaseConnectionDialog;
import org.pentaho.ui.database.Messages;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.swing.SwingXulLoader;

public class SwingTest {

  DatabaseMeta database = null;

  public static void main( String[] args ) {

    try {
      HopEnvironment.init();
    } catch ( HopException e ) {
      e.printStackTrace();
      System.exit( 1 );
    }

    SwingTest harness = new SwingTest();

    try {
      InputStream in =
        DatabaseDialogHarness.class.getClassLoader().getResourceAsStream(
          "org/pentaho/ui/database/databasedialog.xul" );
      if ( in == null ) {
        System.out.println( "Invalid Input" );
        return;
      }

      SAXReader rdr = new SAXReader();
      final Document doc = rdr.read( in );

      harness.showDialog( doc );

    } catch ( Exception e ) {
      e.printStackTrace();
    }

  }

  private void showDialog( final Document doc ) {

    XulDomContainer container = null;
    try {
      container =
        new SwingXulLoader().loadXul( DatabaseConnectionDialog.DIALOG_DEFINITION_FILE, Messages.getBundle() );
      if ( database != null ) {
        container.getEventHandler( "dataHandler" ).setData( database );
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    XulDialog dialog = (XulDialog) container.getDocumentRoot().getRootElement();
    container.initialize();
    dialog.show();
    try {
      @SuppressWarnings( "unused" )
      Object data = container.getEventHandler( "dataHandler" ).getData();
    } catch ( XulException e ) {
      System.out.println( "Error getting data" );
    }
  }

}
