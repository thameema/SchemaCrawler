/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package schemacrawler.tools.commandline.utility;

import schemacrawler.schemacrawler.Config;
import schemacrawler.schemacrawler.InformationSchemaKey;
import schemacrawler.schemacrawler.InformationSchemaViewsBuilder;
import schemacrawler.schemacrawler.MetadataRetrievalStrategy;
import schemacrawler.schemacrawler.SchemaInfoMetadataRetrievalStrategy;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;

public final class SchemaRetrievalOptionsConfig
{

  public static SchemaRetrievalOptionsBuilder fromConfig(
      final SchemaRetrievalOptionsBuilder providedBuilder, final Config config)
  {
    final SchemaRetrievalOptionsBuilder builder;
    if (providedBuilder == null)
    {
      builder = SchemaRetrievalOptionsBuilder.builder();
    } 
    else
    {
      builder = providedBuilder;
    }

    final Config configProperties;
    if (config == null)
    {
      configProperties = new Config();
    } 
    else
    {
      configProperties = new Config(config);
    }

    final InformationSchemaViewsBuilder informationSchemaViewsBuilder =
        InformationSchemaViewsBuilder
            .builder(builder.getInformationSchemaViews());
    SchemaRetrievalOptionsConfig.fromConfig(informationSchemaViewsBuilder,
        configProperties);
    builder
        .withInformationSchemaViews(informationSchemaViewsBuilder.toOptions());

    for (final SchemaInfoMetadataRetrievalStrategy key : SchemaInfoMetadataRetrievalStrategy
        .values())
    {
      final MetadataRetrievalStrategy currentValue = builder.get(key);
      final MetadataRetrievalStrategy configValue =
          configProperties.getEnumValue(key.getConfigKey(), currentValue);
      builder.with(key, configValue);
    }

    return builder;
  }

  /**
   * Information schema views from a map.
   *
   * @param informationSchemaViewsSql
   *   Map of information schema view definitions.
   */
  public static InformationSchemaViewsBuilder fromConfig(final InformationSchemaViewsBuilder providedBuilder,
      final Config informationSchemaViewsSql)
  {
    final InformationSchemaViewsBuilder builder;
    if (providedBuilder == null) {
      builder = InformationSchemaViewsBuilder.builder();
    }
    else 
    {
      builder = providedBuilder;
    }
  
    if (informationSchemaViewsSql == null)
    {
      return builder;
    }
  
    for (final InformationSchemaKey key : InformationSchemaKey.values())
    {
      if (informationSchemaViewsSql.containsKey(key.getLookupKey()))
      {
        try
        {
          builder.withSql(key,
              informationSchemaViewsSql.get(key.getLookupKey()));
        } catch (final IllegalArgumentException e)
        {
          // Ignore
        }
      }
    }
  
    return builder;
  }

}
