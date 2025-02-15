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

package org.apache.hop.job.entries.ftp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.enterprisedt.net.ftp.FTPClient;

public class MockedJobEntryFTP extends JobEntryFTP {

  @Override
  protected FTPClient initFTPClient() {
    return new MockedFTPClient();
  }

  @Override
  protected InetAddress getInetAddress( String realServername ) throws UnknownHostException {
    return null;
  }

}
