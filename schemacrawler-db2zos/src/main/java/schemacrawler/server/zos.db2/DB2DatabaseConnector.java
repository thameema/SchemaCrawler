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
package schemacrawler.server.zos.db2;


import static schemacrawler.schemacrawler.MetadataRetrievalStrategy.data_dictionary_all;
import static schemacrawler.schemacrawler.SchemaInfoMetadataRetrievalStrategy.tableColumnsRetrievalStrategy;
import java.io.IOException;
import schemacrawler.schemacrawler.DatabaseServerType;
import schemacrawler.tools.databaseconnector.DatabaseConnectionUrlBuilder;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.executable.commandline.PluginCommand;

public final class DB2DatabaseConnector
  extends DatabaseConnector
{

  public DB2DatabaseConnector() throws IOException
  {
    super(new DatabaseServerType("db2zos", "IBM z/OS DB2"),
        url -> url != null && url.startsWith("jdbc:db2:"),
        (informationSchemaViewsBuilder,
            connection) -> informationSchemaViewsBuilder
                .fromResourceFolder("/db2zos.information_schema"),
        (schemaRetrievalOptionsBuilder, connection) -> schemaRetrievalOptionsBuilder.with(tableColumnsRetrievalStrategy,
            data_dictionary_all),
        (limitOptionsBuilder) -> {},
        () -> DatabaseConnectionUrlBuilder.builder(
            "jdbc:db2://${host}:${port}/${database}:retrieveMessagesFromServerOnGetMessage=true;")
            .withDefaultPort(50000));
  }

  @Override
  public PluginCommand getHelpCommand()
  {
    final PluginCommand pluginCommand = super.getHelpCommand();
    pluginCommand
      .addOption("server",
                 String.class,
                 "--server=db2zos%n" + "Loads SchemaCrawler plug-in for IBM z/OS DB2")
      .addOption("host",
                 String.class,
                 "Host name%n" + "Optional, defaults to localhost")
      .addOption("port",
                 Integer.class,
                 "Port number%n" + "Optional, defaults to 50000")
      .addOption("database", String.class, "Database name");
    return pluginCommand;
  }

}
