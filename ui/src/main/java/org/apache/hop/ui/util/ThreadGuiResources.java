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

package org.apache.hop.ui.util;

import org.eclipse.swt.widgets.Display;
import org.apache.hop.core.Const;
import org.apache.hop.core.gui.HopUiFactory;
import org.apache.hop.core.gui.ThreadDialogs;

public class ThreadGuiResources implements ThreadDialogs {

  public boolean threadMessageBox( final String message, final String text, boolean allowCancel, int type ) {

    final boolean[] result = new boolean[1];
    Display.getDefault().syncExec( new Runnable() {
      public void run() {
        result[0] = HopUiFactory.getInstance().messageBox( message, text, true, Const.INFO );
      }
    } );
    return result[0];
  }

}
