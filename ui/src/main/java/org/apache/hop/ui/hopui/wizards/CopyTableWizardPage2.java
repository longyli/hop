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

package org.apache.hop.ui.hopui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.apache.hop.core.Const;
import org.apache.hop.core.database.Database;
import org.apache.hop.core.exception.HopDatabaseException;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.PropsUI;
import org.apache.hop.ui.core.dialog.ErrorDialog;

/**
 * This wizard page let's you select the table that need to be ripped.
 *
 * @author Matt
 * @since 29-mar-05
 */
public class CopyTableWizardPage2 extends WizardPage {
  private static Class<?> PKG = CopyTableWizard.class; // for i18n purposes, needed by Translator2!!

  private PropsUI props;

  private Shell shell;

  private String[] input;

  private List wListSource;
  private Label wlListSource;

  public CopyTableWizardPage2( String arg ) {
    super( arg );
    this.props = PropsUI.getInstance();

    setTitle( BaseMessages.getString( PKG, "CopyTableWizardPage2.Dialog.Title" ) );
    setDescription( BaseMessages.getString( PKG, "CopyTableWizardPage2.Dialog.Description" ) );
  }

  public void createControl( Composite parent ) {
    shell = parent.getShell();

    // create the composite to hold the widgets
    Composite composite = new Composite( parent, SWT.NONE );
    props.setLook( composite );

    FormLayout compLayout = new FormLayout();
    compLayout.marginHeight = Const.FORM_MARGIN;
    compLayout.marginWidth = Const.FORM_MARGIN;
    composite.setLayout( compLayout );

    // Source list to the left...
    wlListSource = new Label( composite, SWT.NONE );
    wlListSource.setText( BaseMessages.getString( PKG, "CopyTableWizardPage2.Dialog.TableList.Label" ) );
    props.setLook( wlListSource );
    FormData fdlListSource = new FormData();
    fdlListSource.left = new FormAttachment( 0, 0 );
    fdlListSource.top = new FormAttachment( 0, 0 );
    wlListSource.setLayoutData( fdlListSource );

    wListSource = new List( composite, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL );
    props.setLook( wListSource );
    wListSource.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        setPageComplete( canFlipToNextPage() );
      }
    } );

    FormData fdListSource = new FormData();
    fdListSource.left = new FormAttachment( 0, 0 );
    fdListSource.top = new FormAttachment( wlListSource, 0 );
    fdListSource.right = new FormAttachment( 100, 0 );
    fdListSource.bottom = new FormAttachment( 100, 0 );
    wListSource.setLayoutData( fdListSource );

    // Double click adds to destination.
    wListSource.addSelectionListener( new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        if ( canFinish() ) {
          getWizard().performFinish();
          shell.dispose();
        }
      }
    } );

    // set the composite as the control for this page
    setControl( composite );
  }

  public boolean getInputData() {
    // Get some data...
    CopyTableWizardPage1 page1 = (CopyTableWizardPage1) getPreviousPage();

    Database sourceDb = new Database( CopyTableWizard.loggingObject, page1.getSourceDatabase() );
    try {
      sourceDb.connect();
      input = sourceDb.getTablenames();
    } catch ( HopDatabaseException dbe ) {
      new ErrorDialog(
        shell, BaseMessages.getString( PKG, "CopyTableWizardPage2.ErrorGettingTables.DialogTitle" ),
        BaseMessages.getString( PKG, "CopyTableWizardPage2.ErrorGettingTables.DialogMessage" ), dbe );
      input = null;
      return false;
    } finally {
      sourceDb.disconnect();
    }
    return true;
  }

  public void getData() {
    wListSource.removeAll();

    if ( input != null ) {
      for ( int i = 0; i < input.length; i++ ) {
        wListSource.add( input[i] );
      }
    }
    setPageComplete( canFlipToNextPage() );
  }

  public boolean canFinish() {
    String[] sel = wListSource.getSelection();
    boolean canFlip = sel.length > 0;
    return canFlip;
  }

  public String getSelection() {
    return wListSource.getSelection()[0];
  }
}
