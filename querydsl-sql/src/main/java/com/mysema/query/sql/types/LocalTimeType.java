package com.mysema.query.sql.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

import org.joda.time.LocalTime;

public class LocalTimeType extends AbstractType<LocalTime> {
    
    public LocalTimeType() {
        super(Types.TIME);
    }
    
    public LocalTimeType(int type) {
        super(type);
    }

    @Override
    public Class<LocalTime> getReturnedClass() {
        return LocalTime.class;
    }

    @Override
    public LocalTime getValue(ResultSet rs, int startIndex) throws SQLException {
        Time time = rs.getTime(startIndex);
        return time != null ? new LocalTime(time.getTime()) : null;
    }

    @Override
    public void setValue(PreparedStatement st, int startIndex, LocalTime value) throws SQLException {
        st.setTime(startIndex, new Time(value.toDateTimeToday().getMillis()));        
    }

}
