package com.handwin.db;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.CqlResult;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-13 下午2:01
 */
public class Cassandra {
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

    public Cassandra() {
        context = new AstyanaxContext.Builder()
                .forCluster(clusterName)
                .forKeyspace(keyspaceName)
                .withAstyanaxConfiguration(new AstyanaxConfigurationImpl()
                        .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE)
                        .setCqlVersion("3.0.0")
                        .setTargetCassandraVersion("1.2")
                )
                .withConnectionPoolConfiguration(
                        new ConnectionPoolConfigurationImpl("MyConnectionPool")
                        .setPort(port)
                        .setMaxConnsPerHost(1)
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
                    .withCql("SELECT count(*) FROM Standard1 where KEY='A';")
                    .execute();

            return result.getResult().getNumber();
        } catch (ConnectionException e) {
            log.error(e.getMessage(), e);
            throw new DbException(String.format("excute %s error", cql), e);
        }
    }

    /**
     * 指定每个表的字段序列化方式
     */
    public ColumnFamily<String, String> GAME_ONLINE_COUNTER =
            new ColumnFamily<String, String>(
                    "game_online_counter",              // Column Family Name
                    StringSerializer.get(),   // Key Serializer
                    StringSerializer.get());  // Column Serializer
}
