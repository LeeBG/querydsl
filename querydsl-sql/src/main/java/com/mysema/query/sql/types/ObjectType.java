/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.query.sql.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author tiwe
 *
 */
public class ObjectType extends AbstractType<Object> {
    
    public ObjectType() {
        super(Types.OTHER);
    }
    
    public ObjectType(int type) {
        super(type);
    }
    
    @Override
    public Object getValue(ResultSet rs, int startIndex) throws SQLException {
        return rs.getObject(startIndex);
    }

    @Override
    public Class<Object> getReturnedClass() {
        return Object.class;
    }

    @Override
    public void setValue(PreparedStatement st, int startIndex, Object value)
            throws SQLException {
        st.setObject(startIndex, value);        
    }
    
}
