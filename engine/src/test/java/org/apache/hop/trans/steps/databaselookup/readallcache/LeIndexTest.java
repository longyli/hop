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

package org.apache.hop.trans.steps.databaselookup.readallcache;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.apache.hop.core.row.ValueMetaInterface;

import java.util.BitSet;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author Andrey Khayrutdinov
 */
@RunWith( Parameterized.class )
public class LeIndexTest extends IndexTestBase<GtIndex> {

  @Parameterized.Parameters
  public static List<Object[]> createSampleData() {
    return IndexTestBase.createSampleData();
  }

  public LeIndexTest( Long[][] rows ) {
    super( GtIndex.class, rows );
  }

  @Override
  void doAssertMatches( BitSet candidates, long lookupValue, long actualValue ) {
    if ( !( actualValue <= lookupValue ) ) {
      fail( String.format( "All found values are expected to be less than [%d] or equal to it, but got [%d] among %s",
        lookupValue, actualValue, candidates ) );
    }
  }

  @Override
  GtIndex createIndexInstance( int column, ValueMetaInterface meta, int rowsAmount ) throws Exception {
    return (GtIndex) GtIndex.lessOrEqualCache( column, meta, rowsAmount );
  }


  @Override
  public void lookupFor_MinusOne() {
    testFindsNothing( -1 );
  }

  @Override
  public void lookupFor_Zero() {
    testFindsCorrectly( 0, 1 );
  }

  @Override
  public void lookupFor_One() {
    testFindsCorrectly( 1, 2 );
  }

  @Override
  public void lookupFor_Two() {
    testFindsCorrectly( 2, 4 );
  }

  @Override
  public void lookupFor_Three() {
    testFindsCorrectly( 3, 5 );
  }

  @Override
  public void lookupFor_Hundred() {
    testFindsCorrectly( 100, 5 );
  }
}
