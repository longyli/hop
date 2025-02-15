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

package org.apache.hop.core.exception;

import org.junit.ClassRule;
import org.junit.Test;
import org.apache.hop.junit.rules.RestoreHopEnvironment;
import org.apache.hop.repository.RepositoryObjectType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Yury Bakhmutski
 * @since 02-10-2015
 */
public class IdNotFoundExceptionTest {
  @ClassRule public static RestoreHopEnvironment env = new RestoreHopEnvironment();

  private String message = "messageStub";
  String expectedNullMessage = System.lineSeparator() + "null" + System.lineSeparator();
  private String causeExceptionMessage = "Cause Exception";
  private Throwable cause = new RuntimeException( causeExceptionMessage );
  private String objectName = "Trans.ktr";
  private String pathToObject = "/pathToTrans/";
  private RepositoryObjectType objectType = RepositoryObjectType.TRANSFORMATION;

  @Test
  public void testConstructor1() {
    try {
      throw new IdNotFoundException( objectName, pathToObject, objectType );
    } catch ( IdNotFoundException testedException ) {
      assertEquals( expectedNullMessage, testedException.getMessage() );
      assertEquals( null, testedException.getCause() );
      assertEquals( objectName, testedException.getObjectName() );
      assertEquals( pathToObject, testedException.getPathToObject() );
      assertEquals( objectType, testedException.getObjectType() );
    }
  }

  @Test
  public void testConstructor2() {
    try {
      throw new IdNotFoundException( message, objectName, pathToObject, objectType );
    } catch ( IdNotFoundException testedException ) {
      assertTrue( testedException.getMessage().contains( message ) );
      assertEquals( null, testedException.getCause() );
      assertEquals( objectName, testedException.getObjectName() );
      assertEquals( pathToObject, testedException.getPathToObject() );
      assertEquals( objectType, testedException.getObjectType() );
    }
  }

  @Test
  public void testConstructor3() {
    try {
      throw new IdNotFoundException( cause, objectName, pathToObject, objectType );
    } catch ( IdNotFoundException testedException ) {
      assertEquals( this.cause, testedException.getCause() );
      //check that cause exception's message is not lost
      assertTrue( testedException.getMessage().contains( causeExceptionMessage ) );
      assertEquals( objectName, testedException.getObjectName() );
      assertEquals( pathToObject, testedException.getPathToObject() );
      assertEquals( objectType, testedException.getObjectType() );
    }
  }

  @Test
  public void testConstructor4() {
    try {
      throw new IdNotFoundException( message, cause, objectName, pathToObject, objectType );
    } catch ( IdNotFoundException testedException ) {
      //check that message is not lost
      assertTrue( testedException.getMessage().contains( message ) );
      //check that cause exception's message is not lost
      assertTrue( testedException.getMessage().contains( causeExceptionMessage ) );
      assertEquals( cause, testedException.getCause() );
      assertEquals( objectName, testedException.getObjectName() );
      assertEquals( pathToObject, testedException.getPathToObject() );
      assertEquals( objectType, testedException.getObjectType() );
    }
  }
}
