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

package org.apache.hop.ui.hopui;

import java.util.Locale;

import org.apache.hop.core.EngineMetaInterface;
import org.apache.hop.core.exception.HopMissingPluginsException;
import org.w3c.dom.Node;

public interface FileListener {

  public boolean open( Node transNode, String fname, boolean importfile ) throws HopMissingPluginsException;

  public boolean save( EngineMetaInterface meta, String fname, boolean isExport );

  public void syncMetaName( EngineMetaInterface meta, String name );

  public boolean accepts( String fileName );

  public boolean acceptsXml( String nodeName );

  public String[] getSupportedExtensions();

  public String[] getFileTypeDisplayNames( Locale locale );

  public String getRootNodeName();
}
