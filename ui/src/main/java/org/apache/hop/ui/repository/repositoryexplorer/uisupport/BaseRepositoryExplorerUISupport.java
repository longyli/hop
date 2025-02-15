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

package org.apache.hop.ui.repository.repositoryexplorer.uisupport;

import org.apache.hop.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.apache.hop.ui.repository.repositoryexplorer.controllers.ClustersController;
import org.apache.hop.ui.repository.repositoryexplorer.controllers.ConnectionsController;
import org.apache.hop.ui.repository.repositoryexplorer.controllers.PartitionsController;
import org.apache.hop.ui.repository.repositoryexplorer.controllers.SlavesController;

public class BaseRepositoryExplorerUISupport extends AbstractRepositoryExplorerUISupport {

  @Override
  protected void setup() {
    BrowseController browseController = new BrowseController();
    ConnectionsController connectionsController = new ConnectionsController();
    PartitionsController partitionsController = new PartitionsController();
    SlavesController slavesController = new SlavesController();
    ClustersController clustersController = new ClustersController();

    handlers.add( browseController );
    controllerNames.add( browseController.getName() );
    handlers.add( connectionsController );
    controllerNames.add( connectionsController.getName() );
    handlers.add( partitionsController );
    controllerNames.add( partitionsController.getName() );
    handlers.add( slavesController );
    controllerNames.add( slavesController.getName() );
    handlers.add( clustersController );
    controllerNames.add( clustersController.getName() );
  }

}
