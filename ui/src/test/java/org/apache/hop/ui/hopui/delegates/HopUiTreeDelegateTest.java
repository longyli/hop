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

package org.apache.hop.ui.hopui.delegates;

import org.apache.hop.ui.hopui.HopUi;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.apache.hop.core.plugins.PluginInterface;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.core.plugins.StepPluginType;
import org.apache.hop.ui.core.ConstUI;
import org.apache.hop.ui.hopui.TreeSelection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { ConstUI.class, PluginRegistry.class } )
public class HopUiTreeDelegateTest {

  private HopUi hopUi = mock( HopUi.class );

  @Before
  public void setup() {
    mockStatic( ConstUI.class );
    mockStatic( PluginRegistry.class );
  }

  @Test
  public void getTreeObjects_getStepByName() {
    HopUiTreeDelegate std = spy( new HopUiTreeDelegate( hopUi ) );

    Tree selection = mock( Tree.class );
    Tree core = mock( Tree.class );
    TreeItem item = mock( TreeItem.class );
    PluginInterface step = mock( PluginInterface.class );
    PluginRegistry registry = mock( PluginRegistry.class );

    TreeItem[] items = new TreeItem[] { item };

    when( ConstUI.getTreeStrings( item ) ).thenReturn( new String[] { "Output", "Delete" } );
    when( PluginRegistry.getInstance() ).thenReturn( registry );

    doReturn( items ).when( core ).getSelection();
    doReturn( null ).when( item ).getData( anyString() );
    doReturn( step ).when( registry ).findPluginWithName( StepPluginType.class, "Delete" );

    hopUi.showJob = false;
    hopUi.showTrans = true;

    TreeSelection[] ts = std.getTreeObjects( core, selection, core );

    assertEquals( 1, ts.length );
    assertEquals( step, ts[ 0 ].getSelection() );
  }

  @Test
  public void getTreeObjects_getStepById() {
    HopUiTreeDelegate std = spy( new HopUiTreeDelegate( hopUi ) );

    Tree selection = mock( Tree.class );
    Tree core = mock( Tree.class );
    TreeItem item = mock( TreeItem.class );
    PluginInterface step = mock( PluginInterface.class );
    PluginRegistry registry = mock( PluginRegistry.class );

    TreeItem[] items = new TreeItem[] { item };

    when( ConstUI.getTreeStrings( item ) ).thenReturn( new String[] { "Output", "Avro Output" } );
    when( PluginRegistry.getInstance() ).thenReturn( registry );

    doReturn( items ).when( core ).getSelection();
    doReturn( "AvroOutputPlugin" ).when( item ).getData( anyString() );
    doReturn( step ).when( registry ).findPluginWithId( StepPluginType.class, "AvroOutputPlugin" );

    hopUi.showJob = false;
    hopUi.showTrans = true;

    TreeSelection[] ts = std.getTreeObjects( core, selection, core );

    assertEquals( 1, ts.length );
    assertEquals( step, ts[ 0 ].getSelection() );
  }
}
