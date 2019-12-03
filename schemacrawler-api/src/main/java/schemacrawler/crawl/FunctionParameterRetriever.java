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

package schemacrawler.crawl;


import static java.util.Objects.requireNonNull;
import static sf.util.Utility.isBlank;

import java.sql.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;

import schemacrawler.filter.InclusionRuleFilter;
import schemacrawler.schema.FunctionParameter;
import schemacrawler.schema.ParameterModeType;
import schemacrawler.schema.RoutineType;
import schemacrawler.schemacrawler.*;
import schemacrawler.utility.Query;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;

/**
 * A retriever uses database metadata to get the details about the
 * database function parameters.
 *
 * @author Sualeh Fatehi
 */
final class FunctionParameterRetriever
  extends AbstractRetriever
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(FunctionParameterRetriever.class.getName());

  FunctionParameterRetriever(final RetrieverConnection retrieverConnection,
                             final MutableCatalog catalog,
                             final SchemaCrawlerOptions options)
  {
    super(retrieverConnection, catalog, options);
  }

  void retrieveFunctionParameters(final NamedObjectList<MutableRoutine> allRoutines,
                                  final InclusionRule parameterInclusionRule)
    throws SQLException
  {
    requireNonNull(allRoutines, "No functions provided");

    final InclusionRuleFilter<FunctionParameter> parameterFilter = new InclusionRuleFilter<>(
      parameterInclusionRule,
      true);
    if (parameterFilter.isExcludeAll())
    {
      LOGGER.log(Level.INFO,
                 "Not retrieving function parameters, since this was not requested");
      return;
    }

    final MetadataRetrievalStrategy functionParameterRetrievalStrategy = getRetrieverConnection()
      .getFunctionColumnRetrievalStrategy();
    switch (functionParameterRetrievalStrategy)
    {
      case data_dictionary_all:
        LOGGER.log(Level.INFO,
                   "Retrieving function parameters, using fast data dictionary retrieval");
        retrieveFunctionParametersFromDataDictionary(allRoutines, parameterFilter);
        break;

      case metadata_all:
        LOGGER.log(Level.INFO,
                   "Retrieving function parameters, using fast meta-data retrieval");
        retrieveFunctionParametersFromMetadataForAllFunctions(allRoutines,
                                                              parameterFilter);
        break;

      case metadata:
        LOGGER.log(Level.INFO, "Retrieving function parameters");
        retrieveFunctionParametersFromMetadata(allRoutines, parameterFilter);
        break;

      default:
        break;
    }

  }

  private void createFunctionParameter(final MetadataResultSet results,
                                       final NamedObjectList<MutableRoutine> allRoutines,
                                       final InclusionRuleFilter<FunctionParameter> parameterFilter)
  {
    final String columnCatalogName = normalizeCatalogName(results.getString(
      "FUNCTION_CAT"));
    final String schemaName = normalizeSchemaName(results
                                                    .getString("FUNCTION_SCHEM"));
    final String functionName = results.getString("FUNCTION_NAME");
    String columnName = results.getString("COLUMN_NAME");
    final String specificName = results.getString("SPECIFIC_NAME");

    final ParameterModeType parameterMode = getFunctionParameterMode(results
                                                                       .getInt(
                                                                         "COLUMN_TYPE",
                                                                         DatabaseMetaData.functionColumnUnknown));

    LOGGER.log(Level.FINE,
               new StringFormat("Retrieving function column <%s.%s.%s.%s.%s>",
                                columnCatalogName,
                                schemaName,
                                functionName,
                                specificName,
                                columnName));
    if (isBlank(columnName) && parameterMode == ParameterModeType.result)
    {
      columnName = "<return value>";
    }
    if (isBlank(columnName))
    {
      return;
    }

    final Optional<MutableRoutine> optionalRoutine = allRoutines.lookup(Arrays
                                                                          .asList(
                                                                            columnCatalogName,
                                                                            schemaName,
                                                                            functionName,
                                                                            specificName));
    if (!optionalRoutine.isPresent())
    {
      return;
    }

    final MutableRoutine routine = optionalRoutine.get();
    if (routine.getRoutineType() != RoutineType.function)
    {
      return;
    }

    final MutableFunction function = (MutableFunction) routine;
    final MutableFunctionParameter parameter = lookupOrCreateFunctionParameter(
      function,
      columnName);
    if (parameterFilter.test(parameter) && belongsToSchema(function,
                                                     columnCatalogName,
                                                     schemaName))
    {
      final int ordinalPosition = results.getInt("ORDINAL_POSITION", 0);
      final int dataType = results.getInt("DATA_TYPE", 0);
      final String typeName = results.getString("TYPE_NAME");
      final int length = results.getInt("LENGTH", 0);
      final int precision = results.getInt("PRECISION", 0);
      final boolean isNullable = results.getShort("NULLABLE",
                                                  (short) DatabaseMetaData.functionNullableUnknown)
                                 == (short) DatabaseMetaData.functionNullable;
      final String remarks = results.getString("REMARKS");
      parameter.setOrdinalPosition(ordinalPosition);
      parameter.setParameterMode(parameterMode);
      parameter
        .setColumnDataType(lookupOrCreateColumnDataType(function.getSchema(),
                                                        dataType,
                                                        typeName));
      parameter.setSize(length);
      parameter.setPrecision(precision);
      parameter.setNullable(isNullable);
      parameter.setRemarks(remarks);

      parameter.addAttributes(results.getAttributes());

      LOGGER.log(Level.FINER,
                 new StringFormat("Adding parameter to function <%s>",
                                  parameter.getFullName()));
      function.addParameter(parameter);
    }

  }

  private ParameterModeType getFunctionParameterMode(final int columnType)
  {
    switch (columnType)
    {
      case DatabaseMetaData.functionColumnIn:
        return ParameterModeType.in;
      case DatabaseMetaData.functionColumnInOut:
        return ParameterModeType.inOut;
      case DatabaseMetaData.functionColumnOut:
        return ParameterModeType.out;
      case DatabaseMetaData.functionColumnResult:
        return ParameterModeType.result;
      case DatabaseMetaData.functionReturn:
        return ParameterModeType.returnValue;
      default:
        return ParameterModeType.unknown;
    }
  }

  private MutableFunctionParameter lookupOrCreateFunctionParameter(final MutableFunction function,
                                                                   final String columnName)
  {
    final Optional<MutableFunctionParameter> columnOptional = function
      .lookupParameter(columnName);
    final MutableFunctionParameter column;
    if (columnOptional.isPresent())
    {
      column = columnOptional.get();
    }
    else
    {
      column = new MutableFunctionParameter(function, columnName);
    }
    return column;
  }

  private void retrieveFunctionParametersFromDataDictionary(final NamedObjectList<MutableRoutine> allRoutines,
                                                            final InclusionRuleFilter<FunctionParameter> parameterFilter)
    throws SQLException
  {
    final InformationSchemaViews informationSchemaViews = getRetrieverConnection()
      .getInformationSchemaViews();
    if (!informationSchemaViews.hasQuery(InformationSchemaKey.FUNCTION_COLUMNS))
    {
      throw new SchemaCrawlerSQLException("No function columns SQL provided",
                                          null);
    }
    final Query functionColumnsSql = informationSchemaViews
      .getQuery(InformationSchemaKey.FUNCTION_COLUMNS);
    final Connection connection = getDatabaseConnection();
    try (final Statement statement = connection.createStatement();
      final MetadataResultSet results = new MetadataResultSet(functionColumnsSql,
                                                              statement,
                                                              getSchemaInclusionRule());)
    {
      results.setDescription("retrieveFunctionColumnsFromDataDictionary");
      while (results.next())
      {
        createFunctionParameter(results, allRoutines, parameterFilter);
      }
    }
  }

  private void retrieveFunctionParametersFromMetadata(final NamedObjectList<MutableRoutine> allRoutines,
                                                      final InclusionRuleFilter<FunctionParameter> parameterFilter)
  {
    for (final MutableRoutine routine : allRoutines)
    {
      if (routine.getRoutineType() != RoutineType.function)
      {
        continue;
      }
      final MutableFunction function = (MutableFunction) routine;

      LOGGER.log(Level.FINE, "Retrieving function parameters for " + function);
      try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
                                                                     .getFunctionColumns(
                                                                       function
                                                                         .getSchema()
                                                                         .getCatalogName(),
                                                                       function
                                                                         .getSchema()
                                                                         .getName(),
                                                                       function
                                                                         .getName(),
                                                                       null));)
      {
        while (results.next())
        {
          createFunctionParameter(results, allRoutines, parameterFilter);
        }
      }
      catch (final AbstractMethodError | SQLFeatureNotSupportedException e)
      {
        logSQLFeatureNotSupported(new StringFormat(
          "Could not retrieve parameters for function %s",
          function), e);
      }
      catch (final SQLException e)
      {
        logPossiblyUnsupportedSQLFeature(new StringFormat(
          "Could not retrieve parameters for function %s",
          function), e);
      }
    }
  }

  private void retrieveFunctionParametersFromMetadataForAllFunctions(final NamedObjectList<MutableRoutine> allRoutines,
                                                                     final InclusionRuleFilter<FunctionParameter> parameterFilter)
  {
    try (final MetadataResultSet results = new MetadataResultSet(getMetaData()
                                                                   .getFunctionColumns(
                                                                     null,
                                                                     null,
                                                                     "%",
                                                                     "%"));)
    {
      while (results.next())
      {
        createFunctionParameter(results, allRoutines, parameterFilter);
      }
    }
    catch (final AbstractMethodError | SQLFeatureNotSupportedException e)
    {
      logSQLFeatureNotSupported(new StringFormat(
        "Could not retrieve parameters for functions"), e);
    }
    catch (final SQLException e)
    {
      logPossiblyUnsupportedSQLFeature(new StringFormat(
        "Could not retrieve parameters for functions"), e);
    }
  }

}