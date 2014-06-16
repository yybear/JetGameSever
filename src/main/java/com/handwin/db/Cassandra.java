package com.handwin.db;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.ColumnMutation;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ConsistencyLevel;
import com.netflix.astyanax.model.CqlResult;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-13 下午2:01
 */
public class Cassandra implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(Cassandra.class);

    private AstyanaxContext<Keyspace> context;

    private Keyspace keyspace;

    @Value("${cassandra.cluster.name}")
    private String clusterName;

    @Value("${cassandra.keyspace.name}")
    private String keyspaceName;

    @Value("${cassandra.seeds}")
    private String seeds;

    @Value("${cassandra.port}")
    private int port;

    @Value("${cassandra.max.conn}")
    private int maxConn;

    @Override
    public void afterPropertiesSet() throws Exception {
        context = new AstyanaxContext.Builder()
                .forCluster(clusterName)
                .forKeyspace(keyspaceName)
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                        .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
                        .setCqlVersion("3.0.0")
                        .setTargetCassandraVersion("1.2")
                        .setDefaultReadConsistencyLevel(ConsistencyLevel.CL_LOCAL_QUORUM)
                        .setDefaultWriteConsistencyLevel(ConsistencyLevel.CL_LOCAL_QUORUM)
                )
                .withConnectionPoolConfiguration(
                        new ConnectionPoolConfigurationImpl("MyConnectionPool")
                                .setPort(port)
                                .setMaxConnsPerHost(maxConn)
                                .setSeeds(seeds)
                )
                .withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
                .buildKeyspace(ThriftFamilyFactory.getInstance());

        context.start();

        keyspace = context.getClient();
    }

    public int count(String cql, ColumnFamily<String, String> columnFamily) {
        try {
            OperationResult<CqlResult<String, String>> result
                    = keyspace.prepareQuery(columnFamily)
                    .withCql(cql)
                    .execute();

            return result.getResult().getNumber();
        } catch (ConnectionException e) {
            log.error(e.getMessage(), e);
            throw new DbException(String.format("excute %s error", cql), e);
        }
    }


    public Keyspace getKeyspace() {
        return keyspace;
    }

    public void updateGameOnlineNum(Integer gameId, boolean incr) {
        ColumnMutation m = keyspace.prepareColumnMutation(GAME_ONLINE_COUNTER, gameId, "counter");
        try {
            if(incr)
                m.incrementCounterColumn(1).execute();
            else
                m.incrementCounterColumn(-1).execute();
        } catch (ConnectionException e) {
            log.error(e.getMessage(), e);
            throw new DbException(String.format("excute updateGameOnlineNum error"), e);
        }
    }

    /**
     * 指定每个表的字段序列化方式
     */
    public final static ColumnFamily<Integer, String> GAME_ONLINE_COUNTER =
            new ColumnFamily<Integer, String>(
                    "game_online_counter",              // Column Family Name
                    IntegerSerializer.get(),             // Key Serializer
                    StringSerializer.get());            // Column Serializer

}
