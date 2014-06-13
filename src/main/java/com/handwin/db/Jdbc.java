package com.handwin.db;

import com.handwin.util.Constants;
import com.mchange.v2.c3p0.DataSources;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-9 下午2:52
 */
public class Jdbc {
    private final static Logger LOGGER = LoggerFactory.getLogger(Jdbc.class);

    @Autowired
    private DataSource unpooled;
    private DataSource pooled;
    /*private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";

    @Value("db.url")
    private String dbUrl;
    @Value("db.user")
    private String user;
    @Value("db.password")
    private String passwd;*/

    public void initJdbc() {
        LOGGER.debug("init database connection!");
        try {
            //Class.forName(DATABASE_DRIVER);
            //unpooled = DataSources.unpooledDataSource(dbUrl, user, passwd);
            pooled = DataSources.pooledDataSource(unpooled);
        } /*catch (ClassNotFoundException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }*/ catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        init();
    }

    public void init() {
        try {
            InputStream input = Jdbc.class.getResourceAsStream("/sql/game_init.sql");
            importSQL(input);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    /**
     * 更新一个目录下所有的sql脚本
     *
     * @param dir
     */
    public void updateSQLs(String dir) {
        LOGGER.debug("exec sql file, dir is: " + dir);
        File sqlDir = new File(dir);
        if (sqlDir.exists() && sqlDir.isDirectory()) {
            Iterator it = FileUtils.iterateFiles(sqlDir, new String[]{"sql"}, false);
            while (it.hasNext()) {
                File sqlFile = (File) it.next();
                updateSQL(sqlFile);
            }
        }
    }

    /**
     * 更新单个sql脚本
     * @param file
     */
    public void updateSQL(File file) {
        try {
            if(file.exists()) {
                InputStream in = new FileInputStream(file);
                importSQL(in);
            }
        } catch (FileNotFoundException | SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    private void importSQL(InputStream in) throws SQLException {
        Scanner s = new Scanner(in, Constants.UTF8);
        s.useDelimiter("(;(\r)?\n)|(--\n)");
        Statement st = null;
        Connection conn = getConnection();
        try {
            st = conn.createStatement();
            while (s.hasNext()) {
                String line = s.next();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                if ((line.startsWith("/*!") || line.startsWith("/*")) && line.endsWith("*/")) {
                    int i = line.indexOf(' ');
                    line = line.substring(i + 1, line.length() - " */".length());
                } else if (line.startsWith("--")) {
                    continue;
                }
                LOGGER.debug("sql is " + line);
                if (line.trim().length() > 0) {
                    st.execute(line);
                }
            }
        } finally {
            DbUtils.closeQuietly(st);
            DbUtils.closeQuietly(conn);
        }
    }

    public Connection getConnection() {
        try {
            return pooled.getConnection();
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new DbException("get connection error!");
        }
    }

    public QueryRunner getQueryRunner() {
        return new QueryRunner(pooled);
    }

    public int count(String sql) {
        try {
            return (Integer)getQueryRunner().query(sql, SCALAR_HANDLER);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new DbException(String.format("excute %s error", sql), ex);
        }
    }

    public int count(String sql, Object... params) {
        try {
            return (Integer)getQueryRunner().query(sql, SCALAR_HANDLER, params);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new DbException(String.format("excute %s error", sql), ex);
        }
    }

    public Map<String, Object> query(String sql, Object... args) {
        try {
            return getQueryRunner().query(sql, new MapHandler(), args);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new DbException(String.format("excute %s error", sql), ex);
        }
    }

    public <E> E queryClass(Connection conn, String sql, Class<E> className, Object... args) {
        ResultSetHandler<E> rsh = new BeanHandler<E>(className);
        E result = null;
        try {
            if(null == conn)
                result = getQueryRunner().query(sql, rsh, args);
            else
                result = getQueryRunner().query(conn, sql, rsh, args);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return result;
    }

    public Map<String, Object> query(Connection conn, String sql, Object... args) {
        try {
            return getQueryRunner().query(conn, sql, new MapHandler(), args);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new DbException(String.format("excute %s error", sql), ex);
        }
    }

    public int update(String sql, Object... args) {
        try {
            return getQueryRunner().update(sql, args);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new DbException(String.format("excute %s error", sql), ex);
        }
    }

    public int update(Connection conn, String sql, Object... args) {
        try {
            return getQueryRunner().update(conn, sql, args);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new DbException(String.format("excute %s error", sql), ex);
        }
    }

    public List list(String sql, Object... args) {
        try {
            return getQueryRunner().query(sql, new MapListHandler(), args);
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new DbException(String.format("query %s error", sql), ex);
        }
    }

    public ScalarHandler SCALAR_HANDLER = new ScalarHandler() {
        @Override
        public Object handle(ResultSet rs) throws SQLException {
            Object obj = super.handle(rs);
            if (obj instanceof Number)
                return ((Number) obj).intValue();
            return obj;
        }
    };
}
