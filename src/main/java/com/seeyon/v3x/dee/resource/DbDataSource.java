package com.seeyon.v3x.dee.resource;

import java.sql.Connection;

/**
 * @author zhangfb
 */
public interface DbDataSource extends DataSource {
    Connection getConnection() throws Exception;
}
