/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.metadata.pgcatalog;

import com.google.common.collect.ImmutableMap;
import io.crate.action.sql.SessionContext;
import io.crate.analyze.WhereClause;
import io.crate.execution.engine.collect.NestableCollectExpression;
import io.crate.metadata.ColumnIdent;
import io.crate.metadata.RelationName;
import io.crate.metadata.Routing;
import io.crate.metadata.RoutingProvider;
import io.crate.metadata.RowGranularity;
import io.crate.metadata.expressions.RowCollectExpressionFactory;
import io.crate.metadata.table.ColumnRegistrar;
import io.crate.metadata.table.StaticTableInfo;
import io.crate.types.ArrayType;
import io.crate.types.DataTypes;
import org.elasticsearch.cluster.ClusterState;

import java.util.Collections;
import java.util.Map;

public class PgDatabaseTable extends StaticTableInfo  {

    public static final RelationName NAME = new RelationName(PgCatalogSchemaInfo.NAME, "pg_database");

    static class Columns {
        static final ColumnIdent OID = new ColumnIdent("oid");
        static final ColumnIdent DATNAME = new ColumnIdent("datname");
        static final ColumnIdent DATDBA = new ColumnIdent("datdba");
        static final ColumnIdent ENCODING = new ColumnIdent("encoding");
        static final ColumnIdent DATCOLLATE = new ColumnIdent("datcollate");
        static final ColumnIdent DATCTYPE = new ColumnIdent("datctype");
        static final ColumnIdent DATISTEMPLATE = new ColumnIdent("datistemplate");
        static final ColumnIdent DATALLOWCONN = new ColumnIdent("datallowconn");
        static final ColumnIdent DATCONNLIMIT = new ColumnIdent("datconnlimit");
        static final ColumnIdent DATLASTSYSOID = new ColumnIdent("datlastsysoid");
        static final ColumnIdent DATFROZENXID = new ColumnIdent("datfrozenxid");
        static final ColumnIdent DATMINMXID = new ColumnIdent("datminmxid");
        static final ColumnIdent DATTABLESPACE = new ColumnIdent("dattablespace");
        static final ColumnIdent DATACL = new ColumnIdent("datacl");
    }

    public static Map<ColumnIdent, RowCollectExpressionFactory<Object>> expressions() {
        return ImmutableMap.<ColumnIdent, RowCollectExpressionFactory<Object>>builder()
            .put(Columns.OID, () -> NestableCollectExpression.constant(0))
            .put(Columns.DATNAME, () -> NestableCollectExpression.constant("CrateDB"))
            .put(Columns.DATDBA, () -> NestableCollectExpression.constant(null))
            .put(Columns.ENCODING, () -> NestableCollectExpression.constant(null))
            .put(Columns.DATCOLLATE, () -> NestableCollectExpression.constant(null))
            .put(Columns.DATCTYPE, () -> NestableCollectExpression.constant(null))
            .put(Columns.DATISTEMPLATE, () -> NestableCollectExpression.constant(false))
            .put(Columns.DATALLOWCONN, () -> NestableCollectExpression.constant(true))
            .put(Columns.DATCONNLIMIT, () -> NestableCollectExpression.constant(null))
            .put(Columns.DATLASTSYSOID, () -> NestableCollectExpression.constant(null))
            .put(Columns.DATFROZENXID, () -> NestableCollectExpression.constant(null))
            .put(Columns.DATMINMXID, () -> NestableCollectExpression.constant(null))
            .put(Columns.DATTABLESPACE, () -> NestableCollectExpression.constant(null))
            .put(Columns.DATACL, () -> NestableCollectExpression.constant(null))
            .build();
    }


    public PgDatabaseTable() {
        super(NAME, new ColumnRegistrar(NAME, RowGranularity.DOC)
            .register(Columns.OID, DataTypes.INTEGER)
            .register(Columns.DATNAME, DataTypes.STRING)
            .register(Columns.DATDBA, DataTypes.INTEGER)
            .register(Columns.ENCODING, DataTypes.INTEGER)
            .register(Columns.DATCOLLATE, DataTypes.STRING)
            .register(Columns.DATCTYPE, DataTypes.STRING)
            .register(Columns.DATISTEMPLATE, DataTypes.BOOLEAN)
            .register(Columns.DATALLOWCONN, DataTypes.BOOLEAN)
            .register(Columns.DATCONNLIMIT, DataTypes.INTEGER)
            .register(Columns.DATLASTSYSOID, DataTypes.INTEGER)
            .register(Columns.DATFROZENXID, DataTypes.INTEGER)
            .register(Columns.DATMINMXID, DataTypes.INTEGER)
            .register(Columns.DATTABLESPACE, DataTypes.INTEGER)
            .register(Columns.DATACL, new ArrayType(DataTypes.STRING))
            , Collections.emptyList()
        );
    }

    @Override
    public Routing getRouting(ClusterState state, RoutingProvider routingProvider, WhereClause whereClause, RoutingProvider.ShardSelection shardSelection, SessionContext sessionContext) {
        return Routing.forTableOnSingleNode(NAME, state.getNodes().getLocalNodeId());
    }

    @Override
    public RowGranularity rowGranularity() {
        return RowGranularity.DOC;
    }
}
