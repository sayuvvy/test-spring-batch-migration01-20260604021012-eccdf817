package com.migration.batch.mapping;

import org.springframework.jdbc.core.RowMapper;
import com.migration.batch.model.SourceEntity;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SourceRowMapper implements RowMapper<SourceEntity> {

    @Override
    public SourceEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        SourceEntity sourceEntity = new SourceEntity();
        
        // Map fields from ResultSet to SourceEntity properties
        sourceEntity.setId(rs.getInt("id"));
        sourceEntity.setName(rs.getString("name"));
        sourceEntity.setCategory(rs.getString("category"));
        sourceEntity.setUnitPrice(rs.getBigDecimal("unit_price"));
        sourceEntity.setQuantityOnHand(rs.getInt("quantity_on_hand"));
        sourceEntity.setSupplierId(rs.getInt("supplier_id"));
        sourceEntity.setModifiedDate(rs.getTimestamp("modified_date"));
        
        return sourceEntity;
    }
}
