package org.whirlplatform.server.form;

import org.apache.commons.lang.NotImplementedException;
import org.apache.empire.commons.ObjectUtils;
import org.apache.empire.commons.StringUtils;
import org.apache.empire.db.DBDatabaseDriver;
import org.whirlplatform.meta.shared.AppConstant;
import org.whirlplatform.meta.shared.component.ComponentModel;
import org.whirlplatform.meta.shared.component.ComponentProperties;
import org.whirlplatform.meta.shared.component.ComponentType;
import org.whirlplatform.meta.shared.component.PropertyType;
import org.whirlplatform.meta.shared.data.*;
import org.whirlplatform.meta.shared.editor.CellElement;
import org.whirlplatform.meta.shared.editor.RowElement;
import org.whirlplatform.meta.shared.editor.db.AbstractTableElement;
import org.whirlplatform.server.db.ConnectException;
import org.whirlplatform.server.db.ConnectionProvider;
import org.whirlplatform.server.db.ConnectionWrapper;
import org.whirlplatform.server.db.DBConnection;
import org.whirlplatform.server.driver.multibase.fetch.AbstractQueryExecutor;
import org.whirlplatform.server.log.Logger;
import org.whirlplatform.server.log.LoggerFactory;
import org.whirlplatform.server.log.Profile;
import org.whirlplatform.server.log.impl.ProfileImpl;
import org.whirlplatform.server.log.impl.QueryMessage;
import org.whirlplatform.server.login.ApplicationUser;
import org.whirlplatform.server.monitor.RunningEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public abstract class FormWriter extends AbstractQueryExecutor {
    Logger _log = LoggerFactory.getLogger(FormWriter.class);

    private SimpleDateFormat sdf = new SimpleDateFormat(AppConstant.DATE_FORMAT_LONGEST);

    private DecimalFormat decimalFmt = new DecimalFormat("#");

    protected ConnectionProvider connectionProvider;

    protected FormElementWrapper form;

    protected Map<String, DataValue> startParams;

    protected ApplicationUser user;

    protected boolean refresh;

    private boolean maxRowsReached = false;

    protected FormWriter(ConnectionProvider connectionProvider, FormElementWrapper form,
                         Collection<DataValue> startParams, ApplicationUser user) {
        this.connectionProvider = connectionProvider;
        this.form = form;
        this.startParams = new HashMap<>();
        for (DataValue v : startParams) {
            if (v.getCode() != null && !v.getCode().trim().isEmpty()) {
                this.startParams.put(v.getCode(), v);
            }
        }
        this.user = user;
        decimalFmt.setMaximumFractionDigits(17);
    }



    /**
     * Выполняет предзапрос на форме. Предзапрос должен лежать в диапазоне
     * -1:-1.
     *
     * @throws ConnectException
     * @throws SQLException
     */
    protected void prepareForm() throws ConnectException, SQLException {
        Sql sql = form.getBeforeSql();
        if (sql != null && sql.getDataSourceAlias() != null && !sql.getDataSourceAlias().isEmpty()) {
            try (ConnectionWrapper connection = connectionProvider.getConnection(sql.getDataSourceAlias(), user)) {

                Map<String, DataValue> params = new HashMap<String, DataValue>();
                addParams(params, startParams);

                String query = prepareSql(connection.getDatabaseDriver(), sql.getSql(), startParams);

                ResultSet resultSet = connection.getDatabaseDriver().executeQuery(query, null, false, connection);
                if (resultSet.next()) {
                    Map<String, DataValue> resultValues = collectResultSetValue(connection.getDatabaseDriver(),
                            resultSet);

                    addResultPramsWhilePrepare(resultValues, startParams);
                    Map<Integer, RowElementWrapper> rows = form.getSubRowsInclude(0, form.getRows());
                    for (RowElementWrapper row : rows.values()) {
                        changeRowValues(row, dataValuesToString(connection.getDatabaseDriver(), resultValues));
                    }
                }
            }
        }
    }

    private Map<String, String> dataValuesToString(DBDatabaseDriver driver, Map<String, DataValue> data) {
        Map<String, String> result = new HashMap<String, String>();
        if (data != null) {
            for (Entry<String, DataValue> e : data.entrySet()) {
                if (DataType.DATE == e.getValue().getType()) {
                    if (e.getValue().getDate() == null) {
                        result.put(e.getKey(), null);
                    } else {
                        result.put(e.getKey(), sdf.format(e.getValue().getDate()));
                    }
                } else if (DataType.NUMBER == e.getValue().getType()) {
                    // Чтобы числа записывались в нормальном формате, а не в
                    // 0.#E
//					DecimalFormat formatter = new DecimalFormat("#");
//					formatter.setMaximumFractionDigits(17);
                    try {
                        result.put(e.getKey(), decimalFmt.format(e.getValue().getDouble()));
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        result.put(e.getKey(), StringUtils.toString(e.getValue().getDouble(), ""));
                    }
                } else {
                    result.put(e.getKey(), StringUtils.toString(e.getValue().getObject(), ""));
                }
            }
        }
        return result;
    }

    protected void addResultPramsWhilePrepare(Map<String, DataValue> dest, Map<String, DataValue> params) {

    }

    protected void addParams(Map<String, DataValue> dest, Map<String, DataValue> params) {
        dest.putAll(params);
    }

    protected abstract int nextRow();

    protected abstract void writeRow(RowElementWrapper row) throws IOException;

    private void changeRowValues(RowElementWrapper row, Map<String, String> params) {
        for (CellElementWrapper cell : row.getCells()) {
            changeCellValues(cell, params);
        }
    }

    private void changeCellValues(CellElementWrapper cell, Map<String, String> params) {
        if (cell.getComponentElement() != null && cell.getComponent() != null) {
            changeComponentValues(cell.getComponent(), params);
        }
    }

    private void changeComponentValues(ComponentModel component, Map<String, String> params) {
        if (component == null || !component.hasValues() || params == null) {
            return;
        }

        Set<String> properties = new HashSet<String>();
        Set<String> others = ComponentProperties.getReplaceableProperties(component.getType());
        if (others != null) {
            properties.addAll(others);
        }

        component.setValue("Type", new DataValueImpl(DataType.STRING, component.getType().toString()));

        Iterator<String> iter = properties.iterator();
        while (iter.hasNext()) {
            String p = iter.next();
            if (!component.containsValue(p) || !component.isReplaceableProperty(p)) {
                continue;
            }
            String original = component.getValue(p).getString();
            String result = changeParameter(p, original, params);

            if (PropertyType.parse(p, component.getType()).getType() == DataType.BOOLEAN) {
                component.setValue(p, new DataValueImpl(DataType.BOOLEAN, ObjectUtils.getBoolean(result)));
            } else if (PropertyType.parse(p, component.getType()).getType() == DataType.DATE) {
                Date date = null;
                if (!StringUtils.isEmpty(result)) {
                    try {
                        date = sdf.parse(result);
                    } catch (ParseException e) {
                        _log.error("Error parse date");
                    }
                }
                component.setValue(p, new DataValueImpl(DataType.DATE, date));
            } else if (PropertyType.parse(p, component.getType()).getType() == DataType.NUMBER) {
                Double doubleVal = null;
                if (!StringUtils.isEmpty(result)) {
                    try {
                        doubleVal = decimalFmt.parse(result).doubleValue();
                    } catch (ParseException e) {
                        _log.error("Error parse double");
                    }
                }
                component.setValue(p, new DataValueImpl(DataType.NUMBER, doubleVal));
            } else if (PropertyType.parse(p, component.getType()).getType() == DataType.LIST) {
                ListModelData data = new ListModelDataImpl();
                data.setId(result);
                component.setValue(p, new DataValueImpl(DataType.LIST, data));
            } else {
                component.setValue(p, new DataValueImpl(DataType.STRING, result));
            }
        }

        // во всех внутренних компонентах кроме подчиненных компонентов форм
        // тоже меняем
        if (component.getType() == ComponentType.FormBuilderType || component.getType() == ComponentType.ReportType) {
            return;
        }
        for (ComponentModel child : component.getChildren()) {
            changeComponentValues(child, params);
        }
    }

    protected String changeParameter(String property, String value, Map<String, String> params) {
        String result = replace(value, params);
        if (!StringUtils.isEmpty(result) && PropertyType.DataSource.getCode().equalsIgnoreCase(property)) {
            for (AbstractTableElement t : user.getApplication().getAvailableTables()) {
                if (result.trim().equals(t.getCode())) {
                    result = t.getId();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Замена параметров на значения в source, вхождения параметров в строку
     */
    protected String replace(String source, Map<String, String> params) {
        if (source == null) {
            return "";
        } else {
            return TemplateProcessor.get().replace(source, params);
        }
    }

    /**
     * Из исходной карты параметров делает новую, где все ключи приведены к
     * верхнему регистру, добавлен pfuser, pfip, все ключи, состоящие только из
     * цифр, слева дополнены приставкой PF
     *
     * @param paramMap исходная карта параметров
     * @return новая карта параметров
     */
    protected Map<String, DataValue> processStartParams(Map<String, DataValue> paramMap) {
        Map<String, DataValue> result = new HashMap<String, DataValue>();

        DataValue data = new DataValueImpl(DataType.STRING);
        data.setCode(AppConstant.WHIRL_USER);
        data.setValue(user.getId());
        result.put(data.getCode(), data);

        data = new DataValueImpl(DataType.STRING);
        data.setCode(AppConstant.WHIRL_IP);
        data.setValue(user.getIp());
        result.put(data.getCode(), data);

        data = new DataValueImpl(DataType.STRING);
        data.setCode(AppConstant.WHIRL_APPLICATION);
        data.setValue(user.getApplication().getCode());
        result.put(data.getCode(), data);

        data = new DataValueImpl(DataType.STRING);
        data.setCode(AppConstant.WHIRL_USER_GROUPS);
        data.setValue(StringUtils.arrayToString(user.getGroups().toArray(), ";"));
        result.put(data.getCode(), data);

        data = new DataValueImpl(DataType.STRING);
        data.setCode(AppConstant.WHIRL_FORM_RELOAD);
        data.setValue(String.valueOf(refresh));
        result.put(data.getCode(), data);

        for (Entry<String, DataValue> entry : paramMap.entrySet()) {
            entry.getValue().setCode(entry.getValue().getCode());
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private void writeTopNonSql() throws IOException {
        Map<Integer, RowElementWrapper> rows = form.getTopNonSql();
        writeNonSqlRows(rows);
    }

    private void writeNonSqlRows(Map<Integer, RowElementWrapper> rows) throws IOException {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (RowElementWrapper r : rows.values()) {
            RowElementWrapper clone = r.clone();
            clone.setFinalRow(nextRow());
            writeRow(clone);
        }
    }

    /**
     * Пишет нижнюю часть формы не содержащую запросов.
     *
     * @throws IOException
     */
    protected void writeBottomNonSql() throws IOException {
        Map<Integer, RowElementWrapper> rows = form.getBottomNonSql();
        writeNonSqlRows(rows);
    }

    /**
     * Пишет часть формы между запросами.
     *
     * @throws IOException
     */
    private void writeBetweenNonSql(Sql from, Sql to) throws IOException {
        if (from == null || to == null || from.getBottom().equals(to.getTop())) {
            return;
        }
        Map<Integer, RowElementWrapper> rows = form.getSubRowsExclude(from.getBottom().getRow(), to.getTop().getRow());
        writeNonSqlRows(rows);
    }

    protected void build() throws SQLException, ConnectException, IOException {
        writeTopNonSql();
        writeSql();
        writeBottomNonSql();
    }

    private void writeSql() throws SQLException, ConnectException, IOException {
        Sql last = null;
        for (Sql sql : form.getRootSqls()) {
            writeBetweenNonSql(last, sql);
            executeSql(new HashMap<String, ConnectionWrapper>(), sql, startParams);
            last = sql;
        }
    }

    /**
     * Выполняет запрос на форме.
     *
     * @param connMap
     * @param sql
     * @param params
     * @throws SQLException
     * @throws ConnectException
     * @throws IOException
     */
    private void executeSql(Map<String, ConnectionWrapper> connMap, Sql sql, Map<String, DataValue> params)
            throws SQLException, ConnectException, IOException {
        ConnectionWrapper connection = connMap.get(sql.getDataSourceAlias());

        boolean closeConnection = false;
        try {
            if (connection == null) {
                closeConnection = true;
                connection = connectionProvider.getConnection(sql.getDataSourceAlias(), user);
                connMap.put(sql.getDataSourceAlias(), connection);
            }
            String query = prepareSql(connection.getDatabaseDriver(), sql.getSql(), params);

            QueryMessage msg = new QueryMessage(user, query);

            RunningEvent ev = new RunningEvent(RunningEvent.Type.FORMREQUEST, "", query, user.getLogin()) {

                @Override
                public void onStop() {
                    // Есть возможность остановить только для Oracle
                }
            };
            try (Profile p = new ProfileImpl(msg, ev)) {
                ResultSet resultSet = connection.getDatabaseDriver().executeQuery(query, null, false, connection);
                boolean hasResult = false;
                while (resultSet.next()) {
                    if (isMaxRowsReached()) {
                        break;
                    }
                    hasResult = true;
                    Map<String, DataValue> resultValues = collectResultSetValue(connection.getDatabaseDriver(),
                            resultSet);

                    addParams(resultValues, params);

                    // выполняем подзапросы
                    if (sql.hasChildren()) {

                        // выполняем подзапросы
                        boolean firstSubSql = true;
                        Sql lastSubSql = null;
                        for (Sql s : sql.getChildren()) {

                            // пишем строки от начала основного запроса до
                            // первого
                            // подзапроса
                            if (firstSubSql) {
                                Map<Integer, RowElementWrapper> rows = form.getSubRowsInclude(sql.getTop().getRow(), s
                                        .getTop().getRow() - 1);
                                output(rows, dataValuesToString(connection.getDatabaseDriver(), resultValues));
                                firstSubSql = false;
                            }

                            // выводим строки между подзапросами
                            if (lastSubSql != null) {
                                Map<Integer, RowElementWrapper> rows = form.getSubRowsExclude(lastSubSql.getBottom()
                                        .getRow(), s.getTop().getRow());
                                output(rows, dataValuesToString(connection.getDatabaseDriver(), resultValues));
                            }

                            // выполняем подзапрос
                            executeSql(connMap, s, resultValues);
                            lastSubSql = s;
                        }

                        // выводим строки от окончания последнего подзапроса до
                        // окончания основного запроса
                        if (lastSubSql != null) {
                            Map<Integer, RowElementWrapper> rows = form.getSubRowsExclude(lastSubSql.getBottom()
                                    .getRow(), sql.getBottom().getRow() + 1);
                            output(rows, dataValuesToString(connection.getDatabaseDriver(), resultValues));
                        }
                    } else {
                        Map<Integer, RowElementWrapper> rows = form.getSubRowsInclude(sql.getTop().getRow(), sql
                                .getBottom().getRow());
                        output(rows, dataValuesToString(connection.getDatabaseDriver(), resultValues));
                    }

                    Thread.yield();
                }

                if (!hasResult) {
                    ouputEmpty(sql);
                }

                DBConnection.closeResources(resultSet);
            }
        } finally {
            if (closeConnection) {
                DBConnection.closeResources(connection);
                connMap.remove(sql.getDataSourceAlias());
            }
        }
    }

    protected void output(Map<Integer, RowElementWrapper> rows, Map<String, String> resultValues) throws IOException {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        Map<String, String> rowValues = new HashMap<String, String>();
//		Map<String, String> sqlValues = new HashMap<String, String>();

        rowValues.putAll(resultValues);
//		sqlValues.putAll(resultValues);

        // выводим данные по строкам
        for (Integer i : rows.keySet()) {
            RowElementWrapper original = rows.get(i);
            RowElementWrapper clone = original.clone();
            clone.setFinalRow(nextRow());

            changeRowValues(clone, rowValues);

            writeRow(clone);
        }
    }

    protected void ouputEmpty(Sql sql) throws IOException {
        if (sql.getTextNoData() != null) {
            RowElementWrapper empty = createEmptyRow(sql.getTop(), sql.getTextNoData());
            writeRow(empty);
        }
    }

    private RowElementWrapper createEmptyRow(RowElementWrapper original, String text) {
        RowElementWrapper empty = new RowElementWrapper(original.getRow());
        RowElement e = new RowElement();
        e.setRow(original.getRow());
        e.setHeight(28d);
        empty.setElement(e);
        empty.setFinalRow(nextRow());
        ColumnElementWrapper col = new ColumnElementWrapper(0);
        CellElementWrapper cell = new CellElementWrapper(empty, col);
        CellElement ce = new CellElement();
        ce.setColSpan(form.getCols());
        ce.setRowSpan(1);
        cell.setElement(ce);

        ComponentModel component = new ComponentModel(ComponentType.LabelType);
        Map<String, DataValue> values = new HashMap<String, DataValue>();
        values.put(PropertyType.Html.getCode(), new DataValueImpl(DataType.STRING, text));
        component.setValues(values);
        cell.setComponent(component);

        empty.addCell(cell);
        return empty;
    }

    @Override
    public Map<String, DataValue> executeQuery(String sql, Map<String, DataValue> params) {
        throw new NotImplementedException("executeQuery not implemented for " + getClass());
    }

    protected boolean isMaxRowsReached() {
        return maxRowsReached;
    }

    protected void setMaxRowsReached(boolean maxRowsReached) {
        this.maxRowsReached = maxRowsReached;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public abstract void write(OutputStream stream) throws IOException, SQLException, ConnectException;

    public abstract void close() throws IOException;

}
