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

package org.apache.hop.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.apache.hop.core.exception.HopException;

public class AboutDialog extends Dialog {

  public AboutDialog( Shell parent ) {
    super( parent );
  }

  public void open() throws HopException {
    Shell splashShell = new Shell( getParent(), SWT.APPLICATION_MODAL );
    final Splash splash = new Splash( getParent().getDisplay(), splashShell );
    splashShell.addMouseListener( new MouseAdapter() {

      public void mouseUp( MouseEvent mouseevent ) {
        splash.dispose();
      }

    } );
  }

}
