package vn.fpt.models.type;

import io.vertx.core.json.JsonObject;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.converter.spi.BasicValueConverter;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 7/10/24
 * Time: 2:08 PM
 */
public class JsonObjectType implements UserType<JsonObject> {

    @Override
    public int getSqlType() {
        return 0;
    }

    @Override
    public Class<JsonObject> returnedClass() {
        return JsonObject.class;
    }

    @Override
    public boolean equals(JsonObject x, JsonObject y) {
        if (x == null) {
            return y == null;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(JsonObject x) {
        return x.hashCode();
    }

    @Override
    public JsonObject nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
            throws SQLException {
        return rs.getObject(position, JsonObject.class);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, JsonObject value, int index, SharedSessionContractImplementor session)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value, Types.OTHER);
        }
    }

    @Override
    public JsonObject deepCopy(JsonObject value) {
        return value != null ? value.copy() : null;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(JsonObject value) {
        return value == null ? null : value.encode();
    }

    @Override
    public JsonObject assemble(Serializable cached, Object owner) {
        String cachedStringValue = (String) cached;
        return deepCopy(cachedStringValue != null ? new JsonObject(cachedStringValue) : null);
    }

    @Override
    public JsonObject replace(JsonObject original, JsonObject target, Object owner) {
        return deepCopy(original);
    }

    @Override
    public long getDefaultSqlLength(Dialect dialect, JdbcType jdbcType) {
        return UserType.super.getDefaultSqlLength(dialect, jdbcType);
    }

    @Override
    public int getDefaultSqlPrecision(Dialect dialect, JdbcType jdbcType) {
        return UserType.super.getDefaultSqlPrecision(dialect, jdbcType);
    }

    @Override
    public int getDefaultSqlScale(Dialect dialect, JdbcType jdbcType) {
        return UserType.super.getDefaultSqlScale(dialect, jdbcType);
    }

    @Override
    public JdbcType getJdbcType(TypeConfiguration typeConfiguration) {
        return UserType.super.getJdbcType(typeConfiguration);
    }

    @Override
    public BasicValueConverter<JsonObject, Object> getValueConverter() {
        return UserType.super.getValueConverter();
    }
}
