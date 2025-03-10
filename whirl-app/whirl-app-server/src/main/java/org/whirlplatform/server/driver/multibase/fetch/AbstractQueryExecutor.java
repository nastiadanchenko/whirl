package org.whirlplatform.server.driver.multibase.fetch;

import org.apache.empire.db.DBDatabaseDriver;
import org.whirlplatform.meta.shared.data.DataType;
import org.whirlplatform.meta.shared.data.DataValue;
import org.whirlplatform.meta.shared.data.DataValueImpl;
import org.whirlplatform.server.db.NamedParamResolver;
import org.whirlplatform.server.utils.TypesUtil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractQueryExecutor implements QueryExecutor {

    protected String prepareSql(DBDatabaseDriver driver, String sql, Map<String, DataValue> params) {
        NamedParamResolver changer = new NamedParamResolver(driver, sql, params);
        return changer.getResultSql();
    }

    protected Map<String, DataValue> collectResultSetValue(DBDatabaseDriver driver, ResultSet resultSet)
            throws SQLException {
        Map<String, DataValue> resultValues = new LinkedHashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String column = metaData.getColumnLabel(i);
            DataValue value = getResultSetValue(driver, resultSet, i);
            resultValues.put(column, value);
        }
        return resultValues;
    }

    protected DataValue getResultSetValue(DBDatabaseDriver driver, ResultSet resultSet, int column) throws SQLException {
        int sqlType = resultSet.getMetaData().getColumnType(column);
        org.apache.empire.data.DataType empType = TypesUtil.toEmpireType(sqlType);
        DataType type = TypesUtil.fromEimpireType(empType);

        Object value = getResultValue(driver, resultSet, column, empType);
        DataValue data = new DataValueImpl(type);
        data.setValue(value);
        return data;
    }

    private Object getResultValue(DBDatabaseDriver driver, ResultSet resultSet, int column,
                                  org.apache.empire.data.DataType empType) throws SQLException {
        if (empType == org.apache.empire.data.DataType.DATETIME) {
            return resultSet.getTimestamp(column);
        }
        return driver.getResultValue(resultSet, column, empType);
    }
}
