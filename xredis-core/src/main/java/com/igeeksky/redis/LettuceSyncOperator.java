
package com.igeeksky.redis;

import com.igeeksky.redis.api.RedisSyncOperator;
import io.lettuce.core.*;
import io.lettuce.core.XReadArgs.StreamOffset;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.json.JsonParser;
import io.lettuce.core.json.JsonPath;
import io.lettuce.core.json.JsonType;
import io.lettuce.core.json.JsonValue;
import io.lettuce.core.json.arguments.JsonGetArgs;
import io.lettuce.core.json.arguments.JsonMsetArgs;
import io.lettuce.core.json.arguments.JsonRangeArgs;
import io.lettuce.core.json.arguments.JsonSetArgs;
import io.lettuce.core.models.stream.ClaimedMessages;
import io.lettuce.core.models.stream.PendingMessage;
import io.lettuce.core.models.stream.PendingMessages;
import io.lettuce.core.output.*;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.protocol.RedisCommand;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lettuce 同步方法
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceSyncOperator<K, V> implements RedisSyncOperator<K, V>, RedisCommands<K, V> {

    private final RedisCommands<K, V> redisCommands;
    private final StatefulRedisConnection<K, V> connection;

    public LettuceSyncOperator(StatefulRedisConnection<K, V> connection) {
        this.connection = connection;
        this.redisCommands = connection.sync();
    }

    @Override
    public Long publish(K channel, V message) {
        return redisCommands.publish(channel, message);
    }

    @Override
    public List<K> pubsubChannels() {
        return redisCommands.pubsubChannels();
    }

    @Override
    public List<K> pubsubChannels(K channel) {
        return redisCommands.pubsubChannels(channel);
    }

    @Override
    @SafeVarargs
    public final Map<K, Long> pubsubNumsub(K... channels) {
        return redisCommands.pubsubNumsub(channels);
    }

    @Override
    public List<K> pubsubShardChannels() {
        return redisCommands.pubsubShardChannels();
    }

    @Override
    public List<K> pubsubShardChannels(K pattern) {
        return redisCommands.pubsubShardChannels(pattern);
    }

    @Override
    @SafeVarargs
    public final Map<K, Long> pubsubShardNumsub(K... shardChannels) {
        return redisCommands.pubsubShardNumsub(shardChannels);
    }

    @Override
    public Long pubsubNumpat() {
        return redisCommands.pubsubNumpat();
    }

    @Override
    public Long spublish(K shardChannel, V message) {
        return redisCommands.spublish(shardChannel, message);
    }

    @Override
    public V echo(V msg) {
        return redisCommands.echo(msg);
    }

    @Override
    public List<Object> role() {
        return redisCommands.role();
    }

    @Override
    public String ping() {
        return redisCommands.ping();
    }

    @Override
    public String readOnly() {
        return redisCommands.readOnly();
    }

    @Override
    public String readWrite() {
        return redisCommands.readWrite();
    }

    @Override
    public String quit() {
        return redisCommands.quit();
    }

    @Override
    public Long waitForReplication(int replicas, long timeout) {
        return redisCommands.waitForReplication(replicas, timeout);
    }

    @Override
    public <T> T dispatch(ProtocolKeyword type, CommandOutput<K, V, T> output) {
        return redisCommands.dispatch(type, output);
    }

    @Override
    public <T> T dispatch(ProtocolKeyword type, CommandOutput<K, V, T> output, CommandArgs<K, V> args) {
        return redisCommands.dispatch(type, output, args);
    }

    @Override
    public boolean isOpen() {
        return redisCommands.isOpen();
    }

    @Override
    public void reset() {
        redisCommands.reset();
    }

    @Override
    public Set<AclCategory> aclCat() {
        return redisCommands.aclCat();
    }

    @Override
    public Set<CommandType> aclCat(AclCategory category) {
        return redisCommands.aclCat(category);
    }

    @Override
    public Long aclDeluser(String... usernames) {
        return redisCommands.aclDeluser(usernames);
    }

    @Override
    public String aclDryRun(String username, String command, String... args) {
        return redisCommands.aclDryRun(username, command, args);
    }

    @Override
    public String aclDryRun(String username, RedisCommand<K, V, ?> command) {
        return redisCommands.aclDryRun(username, command);
    }

    @Override
    public String aclGenpass() {
        return redisCommands.aclGenpass();
    }

    @Override
    public String aclGenpass(int bits) {
        return redisCommands.aclGenpass(bits);
    }

    @Override
    public List<Object> aclGetuser(String username) {
        return redisCommands.aclGetuser(username);
    }

    @Override
    public List<String> aclList() {
        return redisCommands.aclList();
    }

    @Override
    public String aclLoad() {
        return redisCommands.aclLoad();
    }

    @Override
    public List<Map<String, Object>> aclLog() {
        return redisCommands.aclLog();
    }

    @Override
    public List<Map<String, Object>> aclLog(int count) {
        return redisCommands.aclLog(count);
    }

    @Override
    public String aclLogReset() {
        return redisCommands.aclLogReset();
    }

    @Override
    public String aclSave() {
        return redisCommands.aclSave();
    }

    @Override
    public String aclSetuser(String username, AclSetuserArgs setuserArgs) {
        return redisCommands.aclSetuser(username, setuserArgs);
    }

    @Override
    public List<String> aclUsers() {
        return redisCommands.aclUsers();
    }

    @Override
    public String aclWhoami() {
        return redisCommands.aclWhoami();
    }

    @Override
    @SafeVarargs
    public final <T> T fcall(String function, ScriptOutputType type, K... keys) {
        return redisCommands.fcall(function, type, keys);
    }

    @Override
    @SafeVarargs
    public final <T> T fcall(String function, ScriptOutputType type, K[] keys, V... values) {
        return redisCommands.fcall(function, type, keys, values);
    }

    @Override
    @SafeVarargs
    public final <T> T fcallReadOnly(String function, ScriptOutputType type, K... keys) {
        return redisCommands.fcallReadOnly(function, type, keys);
    }

    @Override
    @SafeVarargs
    public final <T> T fcallReadOnly(String function, ScriptOutputType type, K[] keys, V... values) {
        return redisCommands.fcallReadOnly(function, type, keys, values);
    }

    @Override
    public String functionLoad(String functionCode) {
        return redisCommands.functionLoad(functionCode);
    }

    @Override
    public String functionLoad(String functionCode, boolean replace) {
        return redisCommands.functionLoad(functionCode, replace);
    }

    @Override
    public byte[] functionDump() {
        return redisCommands.functionDump();
    }

    @Override
    public String functionRestore(byte[] dump) {
        return redisCommands.functionRestore(dump);
    }

    @Override
    public String functionRestore(byte[] dump, FunctionRestoreMode mode) {
        return redisCommands.functionRestore(dump, mode);
    }

    @Override
    public String functionFlush(FlushMode flushMode) {
        return redisCommands.functionFlush(flushMode);
    }

    @Override
    public String functionKill() {
        return redisCommands.functionKill();
    }

    @Override
    public List<Map<String, Object>> functionList() {
        return redisCommands.functionList();
    }

    @Override
    public List<Map<String, Object>> functionList(String libraryName) {
        return redisCommands.functionList(libraryName);
    }

    @Override
    public Long geoadd(K key, double longitude, double latitude, V member) {
        return redisCommands.geoadd(key, longitude, latitude, member);
    }

    @Override
    public Long geoadd(K key, double longitude, double latitude, V member, GeoAddArgs args) {
        return redisCommands.geoadd(key, longitude, latitude, member, args);
    }

    @Override
    public Long geoadd(K key, Object... lngLatMember) {
        return redisCommands.geoadd(key, lngLatMember);
    }

    @Override
    @SafeVarargs
    public final Long geoadd(K key, GeoValue<V>... values) {
        return redisCommands.geoadd(key, values);
    }

    @Override
    public Long geoadd(K key, GeoAddArgs args, Object... lngLatMember) {
        return redisCommands.geoadd(key, args, lngLatMember);
    }

    @Override
    @SafeVarargs
    public final Long geoadd(K key, GeoAddArgs args, GeoValue<V>... values) {
        return redisCommands.geoadd(key, args, values);
    }

    @Override
    public Double geodist(K key, V from, V to, GeoArgs.Unit unit) {
        return redisCommands.geodist(key, from, to, unit);
    }

    @Override
    @SafeVarargs
    public final List<Value<String>> geohash(K key, V... members) {
        return redisCommands.geohash(key, members);
    }

    @Override
    @SafeVarargs
    public final List<GeoCoordinates> geopos(K key, V... members) {
        return redisCommands.geopos(key, members);
    }

    @Override
    public Set<V> georadius(K key, double longitude, double latitude, double distance, GeoArgs.Unit unit) {
        return redisCommands.georadius(key, longitude, latitude, distance, unit);
    }

    @Override
    public List<GeoWithin<V>> georadius(K key, double longitude, double latitude, double distance, GeoArgs.Unit unit, GeoArgs geoArgs) {
        return redisCommands.georadius(key, longitude, latitude, distance, unit, geoArgs);
    }

    @Override
    public Long georadius(K key, double longitude, double latitude, double distance, GeoArgs.Unit unit, GeoRadiusStoreArgs<K> geoRadiusStoreArgs) {
        return redisCommands.georadius(key, longitude, latitude, distance, unit, geoRadiusStoreArgs);
    }

    @Override
    public Set<V> georadiusbymember(K key, V member, double distance, GeoArgs.Unit unit) {
        return redisCommands.georadiusbymember(key, member, distance, unit);
    }

    @Override
    public List<GeoWithin<V>> georadiusbymember(K key, V member, double distance, GeoArgs.Unit unit, GeoArgs geoArgs) {
        return redisCommands.georadiusbymember(key, member, distance, unit, geoArgs);
    }

    @Override
    public Long georadiusbymember(K key, V member, double distance, GeoArgs.Unit unit, GeoRadiusStoreArgs<K> geoRadiusStoreArgs) {
        return redisCommands.georadiusbymember(key, member, distance, unit, geoRadiusStoreArgs);
    }

    @Override
    public Set<V> geosearch(K key, GeoSearch.GeoRef<K> reference, GeoSearch.GeoPredicate predicate) {
        return redisCommands.geosearch(key, reference, predicate);
    }

    @Override
    public List<GeoWithin<V>> geosearch(K key, GeoSearch.GeoRef<K> reference, GeoSearch.GeoPredicate predicate, GeoArgs geoArgs) {
        return redisCommands.geosearch(key, reference, predicate, geoArgs);
    }

    @Override
    public Long geosearchstore(K destination, K key, GeoSearch.GeoRef<K> reference, GeoSearch.GeoPredicate predicate, GeoArgs geoArgs, boolean storeDist) {
        return redisCommands.geosearchstore(destination, key, reference, predicate, geoArgs, storeDist);
    }

    @Override
    @SafeVarargs
    public final Long hdel(K key, K... fields) {
        return redisCommands.hdel(key, fields);
    }

    @Override
    public Boolean hexists(K key, K field) {
        return redisCommands.hexists(key, field);
    }

    @Override
    public V hget(K key, K field) {
        return redisCommands.hget(key, field);
    }

    @Override
    public Long hincrby(K key, K field, long amount) {
        return redisCommands.hincrby(key, field, amount);
    }

    @Override
    public Double hincrbyfloat(K key, K field, double amount) {
        return redisCommands.hincrbyfloat(key, field, amount);
    }

    @Override
    public Map<K, V> hgetall(K key) {
        return redisCommands.hgetall(key);
    }

    @Override
    public Long hgetall(KeyValueStreamingChannel<K, V> channel, K key) {
        return redisCommands.hgetall(channel, key);
    }

    @Override
    public List<K> hkeys(K key) {
        return redisCommands.hkeys(key);
    }

    @Override
    public Long hkeys(KeyStreamingChannel<K> channel, K key) {
        return redisCommands.hkeys(channel, key);
    }

    @Override
    public Long hlen(K key) {
        return redisCommands.hlen(key);
    }

    @Override
    @SafeVarargs
    public final List<KeyValue<K, V>> hmget(K key, K... fields) {
        return redisCommands.hmget(key, fields);
    }

    @Override
    @SafeVarargs
    public final Long hmget(KeyValueStreamingChannel<K, V> channel, K key, K... fields) {
        return redisCommands.hmget(channel, key, fields);
    }

    @Override
    public String hmset(K key, Map<K, V> map) {
        return redisCommands.hmset(key, map);
    }

    @Override
    public K hrandfield(K key) {
        return redisCommands.hrandfield(key);
    }

    @Override
    public List<K> hrandfield(K key, long count) {
        return redisCommands.hrandfield(key, count);
    }

    @Override
    public KeyValue<K, V> hrandfieldWithvalues(K key) {
        return redisCommands.hrandfieldWithvalues(key);
    }

    @Override
    public List<KeyValue<K, V>> hrandfieldWithvalues(K key, long count) {
        return redisCommands.hrandfieldWithvalues(key, count);
    }

    @Override
    public MapScanCursor<K, V> hscan(K key) {
        return redisCommands.hscan(key);
    }

    @Override
    public KeyScanCursor<K> hscanNovalues(K key) {
        return redisCommands.hscanNovalues(key);
    }

    @Override
    public MapScanCursor<K, V> hscan(K key, ScanArgs scanArgs) {
        return redisCommands.hscan(key, scanArgs);
    }

    @Override
    public KeyScanCursor<K> hscanNovalues(K key, ScanArgs scanArgs) {
        return redisCommands.hscanNovalues(key, scanArgs);
    }

    @Override
    public MapScanCursor<K, V> hscan(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.hscan(key, scanCursor, scanArgs);
    }

    @Override
    public KeyScanCursor<K> hscanNovalues(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.hscanNovalues(key, scanCursor, scanArgs);
    }

    @Override
    public MapScanCursor<K, V> hscan(K key, ScanCursor scanCursor) {
        return redisCommands.hscan(key, scanCursor);
    }

    @Override
    public KeyScanCursor<K> hscanNovalues(K key, ScanCursor scanCursor) {
        return redisCommands.hscanNovalues(key, scanCursor);
    }

    @Override
    public StreamScanCursor hscan(KeyValueStreamingChannel<K, V> channel, K key) {
        return redisCommands.hscan(channel, key);
    }

    @Override
    public StreamScanCursor hscanNovalues(KeyStreamingChannel<K> channel, K key) {
        return redisCommands.hscanNovalues(channel, key);
    }

    @Override
    public StreamScanCursor hscan(KeyValueStreamingChannel<K, V> channel, K key, ScanArgs scanArgs) {
        return redisCommands.hscan(channel, key, scanArgs);
    }

    @Override
    public StreamScanCursor hscanNovalues(KeyStreamingChannel<K> channel, K key, ScanArgs scanArgs) {
        return redisCommands.hscanNovalues(channel, key, scanArgs);
    }

    @Override
    public StreamScanCursor hscan(KeyValueStreamingChannel<K, V> channel, K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.hscan(channel, key, scanCursor, scanArgs);
    }

    @Override
    public StreamScanCursor hscanNovalues(KeyStreamingChannel<K> channel, K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.hscanNovalues(channel, key, scanCursor, scanArgs);
    }

    @Override
    public StreamScanCursor hscan(KeyValueStreamingChannel<K, V> channel, K key, ScanCursor scanCursor) {
        return redisCommands.hscan(channel, key, scanCursor);
    }

    @Override
    public StreamScanCursor hscanNovalues(KeyStreamingChannel<K> channel, K key, ScanCursor scanCursor) {
        return redisCommands.hscanNovalues(channel, key, scanCursor);
    }

    @Override
    public Boolean hset(K key, K field, V value) {
        return redisCommands.hset(key, field, value);
    }

    @Override
    public Long hset(K key, Map<K, V> map) {
        return redisCommands.hset(key, map);
    }

    @Override
    public Boolean hsetnx(K key, K field, V value) {
        return redisCommands.hsetnx(key, field, value);
    }

    @Override
    public Long hstrlen(K key, K field) {
        return redisCommands.hstrlen(key, field);
    }

    @Override
    public List<V> hvals(K key) {
        return redisCommands.hvals(key);
    }

    @Override
    public Long hvals(ValueStreamingChannel<V> channel, K key) {
        return redisCommands.hvals(channel, key);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpire(K key, long seconds, K... fields) {
        return redisCommands.hexpire(key, seconds, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpire(K key, long seconds, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hexpire(key, seconds, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpire(K key, Duration seconds, K... fields) {
        return redisCommands.hexpire(key, seconds, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpire(K key, Duration seconds, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hexpire(key, seconds, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpireat(K key, long timestamp, K... fields) {
        return redisCommands.hexpireat(key, timestamp, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpireat(K key, long timestamp, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hexpireat(key, timestamp, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpireat(K key, Date timestamp, K... fields) {
        return redisCommands.hexpireat(key, timestamp, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpireat(K key, Date timestamp, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hexpireat(key, timestamp, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpireat(K key, Instant timestamp, K... fields) {
        return redisCommands.hexpireat(key, timestamp, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpireat(K key, Instant timestamp, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hexpireat(key, timestamp, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hexpiretime(K key, K... fields) {
        return redisCommands.hexpiretime(key, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpersist(K key, K... fields) {
        return redisCommands.hpersist(key, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpire(K key, long milliseconds, K... fields) {
        return redisCommands.hpexpire(key, milliseconds, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpire(K key, long milliseconds, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hpexpire(key, milliseconds, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpire(K key, Duration milliseconds, K... fields) {
        return redisCommands.hpexpire(key, milliseconds, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpire(K key, Duration milliseconds, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hpexpire(key, milliseconds, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpireat(K key, long timestamp, K... fields) {
        return redisCommands.hpexpireat(key, timestamp, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpireat(K key, long timestamp, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hpexpireat(key, timestamp, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpireat(K key, Date timestamp, K... fields) {
        return redisCommands.hpexpireat(key, timestamp, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpireat(K key, Date timestamp, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hpexpireat(key, timestamp, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpireat(K key, Instant timestamp, K... fields) {
        return redisCommands.hpexpireat(key, timestamp, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpireat(K key, Instant timestamp, ExpireArgs expireArgs, K... fields) {
        return redisCommands.hpexpireat(key, timestamp, expireArgs, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpexpiretime(K key, K... fields) {
        return redisCommands.hpexpiretime(key, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> httl(K key, K... fields) {
        return redisCommands.httl(key, fields);
    }

    @Override
    @SafeVarargs
    public final List<Long> hpttl(K key, K... fields) {
        return redisCommands.hpttl(key, fields);
    }

    @Override
    @SafeVarargs
    public final Long pfadd(K key, V... values) {
        return redisCommands.pfadd(key, values);
    }

    @Override
    @SafeVarargs
    public final String pfmerge(K destkey, K... sourcekeys) {
        return redisCommands.pfmerge(destkey, sourcekeys);
    }

    @Override
    @SafeVarargs
    public final Long pfcount(K... keys) {
        return redisCommands.pfcount(keys);
    }

    @Override
    public List<Long> jsonArrappend(K key, JsonPath jsonPath, JsonValue... values) {
        return redisCommands.jsonArrappend(key, jsonPath, values);
    }

    @Override
    public List<Long> jsonArrappend(K key, JsonValue... values) {
        return redisCommands.jsonArrappend(key, values);
    }

    @Override
    public List<Long> jsonArrindex(K key, JsonPath jsonPath, JsonValue value, JsonRangeArgs range) {
        return redisCommands.jsonArrindex(key, jsonPath, value, range);
    }

    @Override
    public List<Long> jsonArrindex(K key, JsonPath jsonPath, JsonValue value) {
        return redisCommands.jsonArrindex(key, jsonPath, value);
    }

    @Override
    public List<Long> jsonArrinsert(K key, JsonPath jsonPath, int index, JsonValue... values) {
        return redisCommands.jsonArrinsert(key, jsonPath, index, values);
    }

    @Override
    public List<Long> jsonArrlen(K key, JsonPath jsonPath) {
        return redisCommands.jsonArrlen(key, jsonPath);
    }

    @Override
    public List<Long> jsonArrlen(K key) {
        return redisCommands.jsonArrlen(key);
    }

    @Override
    public List<JsonValue> jsonArrpop(K key, JsonPath jsonPath, int index) {
        return redisCommands.jsonArrpop(key, jsonPath, index);
    }

    @Override
    public List<JsonValue> jsonArrpop(K key, JsonPath jsonPath) {
        return redisCommands.jsonArrpop(key, jsonPath);
    }

    @Override
    public List<JsonValue> jsonArrpop(K key) {
        return redisCommands.jsonArrpop(key);
    }

    @Override
    public List<Long> jsonArrtrim(K key, JsonPath jsonPath, JsonRangeArgs range) {
        return redisCommands.jsonArrtrim(key, jsonPath, range);
    }

    @Override
    public Long jsonClear(K key, JsonPath jsonPath) {
        return redisCommands.jsonClear(key, jsonPath);
    }

    @Override
    public Long jsonClear(K key) {
        return redisCommands.jsonClear(key);
    }

    @Override
    public Long jsonDel(K key, JsonPath jsonPath) {
        return redisCommands.jsonDel(key, jsonPath);
    }

    @Override
    public Long jsonDel(K key) {
        return redisCommands.jsonDel(key);
    }

    @Override
    public List<JsonValue> jsonGet(K key, JsonGetArgs options, JsonPath... jsonPaths) {
        return redisCommands.jsonGet(key, options, jsonPaths);
    }

    @Override
    public List<JsonValue> jsonGet(K key, JsonPath... jsonPaths) {
        return redisCommands.jsonGet(key, jsonPaths);
    }

    @Override
    public String jsonMerge(K key, JsonPath jsonPath, JsonValue value) {
        return redisCommands.jsonMerge(key, jsonPath, value);
    }

    @Override
    @SafeVarargs
    public final List<JsonValue> jsonMGet(JsonPath jsonPath, K... keys) {
        return redisCommands.jsonMGet(jsonPath, keys);
    }

    @Override
    public String jsonMSet(List<JsonMsetArgs<K, V>> arguments) {
        return redisCommands.jsonMSet(arguments);
    }

    @Override
    public List<Number> jsonNumincrby(K key, JsonPath jsonPath, Number number) {
        return redisCommands.jsonNumincrby(key, jsonPath, number);
    }

    @Override
    public List<V> jsonObjkeys(K key, JsonPath jsonPath) {
        return redisCommands.jsonObjkeys(key, jsonPath);
    }

    @Override
    public List<V> jsonObjkeys(K key) {
        return redisCommands.jsonObjkeys(key);
    }

    @Override
    public List<Long> jsonObjlen(K key, JsonPath jsonPath) {
        return redisCommands.jsonObjlen(key, jsonPath);
    }

    @Override
    public List<Long> jsonObjlen(K key) {
        return redisCommands.jsonObjlen(key);
    }

    @Override
    public String jsonSet(K key, JsonPath jsonPath, JsonValue value, JsonSetArgs options) {
        return redisCommands.jsonSet(key, jsonPath, value, options);
    }

    @Override
    public String jsonSet(K key, JsonPath jsonPath, JsonValue value) {
        return redisCommands.jsonSet(key, jsonPath, value);
    }

    @Override
    public List<Long> jsonStrappend(K key, JsonPath jsonPath, JsonValue value) {
        return redisCommands.jsonStrappend(key, jsonPath, value);
    }

    @Override
    public List<Long> jsonStrappend(K key, JsonValue value) {
        return redisCommands.jsonStrappend(key, value);
    }

    @Override
    public List<Long> jsonStrlen(K key, JsonPath jsonPath) {
        return redisCommands.jsonStrlen(key, jsonPath);
    }

    @Override
    public List<Long> jsonStrlen(K key) {
        return redisCommands.jsonStrlen(key);
    }

    @Override
    public List<Long> jsonToggle(K key, JsonPath jsonPath) {
        return redisCommands.jsonToggle(key, jsonPath);
    }

    @Override
    public List<JsonType> jsonType(K key, JsonPath jsonPath) {
        return redisCommands.jsonType(key, jsonPath);
    }

    @Override
    public List<JsonType> jsonType(K key) {
        return redisCommands.jsonType(key);
    }

    @Override
    public Boolean copy(K source, K destination) {
        return redisCommands.copy(source, destination);
    }

    @Override
    public Boolean copy(K source, K destination, CopyArgs copyArgs) {
        return redisCommands.copy(source, destination, copyArgs);
    }

    @Override
    @SafeVarargs
    public final Long del(K... keys) {
        return redisCommands.del(keys);
    }

    @Override
    @SafeVarargs
    public final Long unlink(K... keys) {
        return redisCommands.unlink(keys);
    }

    @Override
    public byte[] dump(K key) {
        return redisCommands.dump(key);
    }

    @Override
    @SafeVarargs
    public final Long exists(K... keys) {
        return redisCommands.exists(keys);
    }

    @Override
    public Boolean expire(K key, long seconds) {
        return redisCommands.expire(key, seconds);
    }

    @Override
    public Boolean expire(K key, long seconds, ExpireArgs expireArgs) {
        return redisCommands.expire(key, seconds, expireArgs);
    }

    @Override
    public Boolean expire(K key, Duration seconds) {
        return redisCommands.expire(key, seconds);
    }

    @Override
    public Boolean expire(K key, Duration seconds, ExpireArgs expireArgs) {
        return redisCommands.expire(key, seconds, expireArgs);
    }

    @Override
    public Boolean expireat(K key, long timestamp) {
        return redisCommands.expireat(key, timestamp);
    }

    @Override
    public Boolean expireat(K key, long timestamp, ExpireArgs expireArgs) {
        return redisCommands.expireat(key, timestamp, expireArgs);
    }

    @Override
    public Boolean expireat(K key, Date timestamp) {
        return redisCommands.expireat(key, timestamp);
    }

    @Override
    public Boolean expireat(K key, Date timestamp, ExpireArgs expireArgs) {
        return redisCommands.expireat(key, timestamp, expireArgs);
    }

    @Override
    public Boolean expireat(K key, Instant timestamp) {
        return redisCommands.expireat(key, timestamp);
    }

    @Override
    public Boolean expireat(K key, Instant timestamp, ExpireArgs expireArgs) {
        return redisCommands.expireat(key, timestamp, expireArgs);
    }

    @Override
    public Long expiretime(K key) {
        return redisCommands.expiretime(key);
    }

    @Override
    public List<K> keys(K pattern) {
        return redisCommands.keys(pattern);
    }

    @Override
    public Long keys(KeyStreamingChannel<K> channel, K pattern) {
        return redisCommands.keys(channel, pattern);
    }

    @Override
    public String migrate(String host, int port, K key, int db, long timeout) {
        return redisCommands.migrate(host, port, key, db, timeout);
    }

    @Override
    public String migrate(String host, int port, int db, long timeout, MigrateArgs<K> migrateArgs) {
        return redisCommands.migrate(host, port, db, timeout, migrateArgs);
    }

    @Override
    public Boolean move(K key, int db) {
        return redisCommands.move(key, db);
    }

    @Override
    public String objectEncoding(K key) {
        return redisCommands.objectEncoding(key);
    }

    @Override
    public Long objectFreq(K key) {
        return redisCommands.objectFreq(key);
    }

    @Override
    public Long objectIdletime(K key) {
        return redisCommands.objectIdletime(key);
    }

    @Override
    public Long objectRefcount(K key) {
        return redisCommands.objectRefcount(key);
    }

    @Override
    public Boolean persist(K key) {
        return redisCommands.persist(key);
    }

    @Override
    public Boolean pexpire(K key, long milliseconds) {
        return redisCommands.pexpire(key, milliseconds);
    }

    @Override
    public Boolean pexpire(K key, long milliseconds, ExpireArgs expireArgs) {
        return redisCommands.pexpire(key, milliseconds, expireArgs);
    }

    @Override
    public Boolean pexpire(K key, Duration milliseconds) {
        return redisCommands.pexpire(key, milliseconds);
    }

    @Override
    public Boolean pexpire(K key, Duration milliseconds, ExpireArgs expireArgs) {
        return redisCommands.pexpire(key, milliseconds, expireArgs);
    }

    @Override
    public Boolean pexpireat(K key, long timestamp) {
        return redisCommands.pexpireat(key, timestamp);
    }

    @Override
    public Boolean pexpireat(K key, long timestamp, ExpireArgs expireArgs) {
        return redisCommands.pexpireat(key, timestamp, expireArgs);
    }

    @Override
    public Boolean pexpireat(K key, Date timestamp) {
        return redisCommands.pexpireat(key, timestamp);
    }

    @Override
    public Boolean pexpireat(K key, Date timestamp, ExpireArgs expireArgs) {
        return redisCommands.pexpireat(key, timestamp, expireArgs);
    }

    @Override
    public Boolean pexpireat(K key, Instant timestamp) {
        return redisCommands.pexpireat(key, timestamp);
    }

    @Override
    public Boolean pexpireat(K key, Instant timestamp, ExpireArgs expireArgs) {
        return redisCommands.pexpireat(key, timestamp, expireArgs);
    }

    @Override
    public Long pexpiretime(K key) {
        return redisCommands.pexpiretime(key);
    }

    @Override
    public Long pttl(K key) {
        return redisCommands.pttl(key);
    }

    @Override
    public K randomkey() {
        return redisCommands.randomkey();
    }

    @Override
    public String rename(K key, K newKey) {
        return redisCommands.rename(key, newKey);
    }

    @Override
    public Boolean renamenx(K key, K newKey) {
        return redisCommands.renamenx(key, newKey);
    }

    @Override
    public String restore(K key, long ttl, byte[] value) {
        return redisCommands.restore(key, ttl, value);
    }

    @Override
    public String restore(K key, byte[] value, RestoreArgs args) {
        return redisCommands.restore(key, value, args);
    }

    @Override
    public List<V> sort(K key) {
        return redisCommands.sort(key);
    }

    @Override
    public Long sort(ValueStreamingChannel<V> channel, K key) {
        return redisCommands.sort(channel, key);
    }

    @Override
    public List<V> sort(K key, SortArgs sortArgs) {
        return redisCommands.sort(key, sortArgs);
    }

    @Override
    public Long sort(ValueStreamingChannel<V> channel, K key, SortArgs sortArgs) {
        return redisCommands.sort(channel, key, sortArgs);
    }

    @Override
    public List<V> sortReadOnly(K key) {
        return redisCommands.sortReadOnly(key);
    }

    @Override
    public Long sortReadOnly(ValueStreamingChannel<V> channel, K key) {
        return redisCommands.sortReadOnly(channel, key);
    }

    @Override
    public List<V> sortReadOnly(K key, SortArgs sortArgs) {
        return redisCommands.sortReadOnly(key, sortArgs);
    }

    @Override
    public Long sortReadOnly(ValueStreamingChannel<V> channel, K key, SortArgs sortArgs) {
        return redisCommands.sortReadOnly(channel, key, sortArgs);
    }

    @Override
    public Long sortStore(K key, SortArgs sortArgs, K destination) {
        return redisCommands.sortStore(key, sortArgs, destination);
    }

    @Override
    @SafeVarargs
    public final Long touch(K... keys) {
        return redisCommands.touch(keys);
    }

    @Override
    public Long ttl(K key) {
        return redisCommands.ttl(key);
    }

    @Override
    public String type(K key) {
        return redisCommands.type(key);
    }

    @Override
    public KeyScanCursor<K> scan() {
        return redisCommands.scan();
    }

    @Override
    public KeyScanCursor<K> scan(ScanArgs scanArgs) {
        return redisCommands.scan(scanArgs);
    }

    @Override
    public KeyScanCursor<K> scan(ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.scan(scanCursor, scanArgs);
    }

    @Override
    public KeyScanCursor<K> scan(ScanCursor scanCursor) {
        return redisCommands.scan(scanCursor);
    }

    @Override
    public StreamScanCursor scan(KeyStreamingChannel<K> channel) {
        return redisCommands.scan(channel);
    }

    @Override
    public StreamScanCursor scan(KeyStreamingChannel<K> channel, ScanArgs scanArgs) {
        return redisCommands.scan(channel, scanArgs);
    }

    @Override
    public StreamScanCursor scan(KeyStreamingChannel<K> channel, ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.scan(channel, scanCursor, scanArgs);
    }

    @Override
    public StreamScanCursor scan(KeyStreamingChannel<K> channel, ScanCursor scanCursor) {
        return redisCommands.scan(channel, scanCursor);
    }

    @Override
    public V blmove(K source, K destination, LMoveArgs args, long timeout) {
        return redisCommands.blmove(source, destination, args, timeout);
    }

    @Override
    public V blmove(K source, K destination, LMoveArgs args, double timeout) {
        return redisCommands.blmove(source, destination, args, timeout);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, List<V>> blmpop(long timeout, LMPopArgs args, K... keys) {
        return redisCommands.blmpop(timeout, args, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, List<V>> blmpop(double timeout, LMPopArgs args, K... keys) {
        return redisCommands.blmpop(timeout, args, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, V> blpop(long timeout, K... keys) {
        return redisCommands.blpop(timeout, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, V> blpop(double timeout, K... keys) {
        return redisCommands.blpop(timeout, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, V> brpop(long timeout, K... keys) {
        return redisCommands.brpop(timeout, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, V> brpop(double timeout, K... keys) {
        return redisCommands.brpop(timeout, keys);
    }

    @Override
    public V brpoplpush(long timeout, K source, K destination) {
        return redisCommands.brpoplpush(timeout, source, destination);
    }

    @Override
    public V brpoplpush(double timeout, K source, K destination) {
        return redisCommands.brpoplpush(timeout, source, destination);
    }

    @Override
    public V lindex(K key, long index) {
        return redisCommands.lindex(key, index);
    }

    @Override
    public Long linsert(K key, boolean before, V pivot, V value) {
        return redisCommands.linsert(key, before, pivot, value);
    }

    @Override
    public Long llen(K key) {
        return redisCommands.llen(key);
    }

    @Override
    public V lmove(K source, K destination, LMoveArgs args) {
        return redisCommands.lmove(source, destination, args);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, List<V>> lmpop(LMPopArgs args, K... keys) {
        return redisCommands.lmpop(args, keys);
    }

    @Override
    public V lpop(K key) {
        return redisCommands.lpop(key);
    }

    @Override
    public List<V> lpop(K key, long count) {
        return redisCommands.lpop(key, count);
    }

    @Override
    public Long lpos(K key, V value) {
        return redisCommands.lpos(key, value);
    }

    @Override
    public Long lpos(K key, V value, LPosArgs args) {
        return redisCommands.lpos(key, value, args);
    }

    @Override
    public List<Long> lpos(K key, V value, int count) {
        return redisCommands.lpos(key, value, count);
    }

    @Override
    public List<Long> lpos(K key, V value, int count, LPosArgs args) {
        return redisCommands.lpos(key, value, count, args);
    }

    @Override
    @SafeVarargs
    public final Long lpush(K key, V... values) {
        return redisCommands.lpush(key, values);
    }

    @Override
    @SafeVarargs
    public final Long lpushx(K key, V... values) {
        return redisCommands.lpushx(key, values);
    }

    @Override
    public List<V> lrange(K key, long start, long stop) {
        return redisCommands.lrange(key, start, stop);
    }

    @Override
    public Long lrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return redisCommands.lrange(channel, key, start, stop);
    }

    @Override
    public Long lrem(K key, long count, V value) {
        return redisCommands.lrem(key, count, value);
    }

    @Override
    public String lset(K key, long index, V value) {
        return redisCommands.lset(key, index, value);
    }

    @Override
    public String ltrim(K key, long start, long stop) {
        return redisCommands.ltrim(key, start, stop);
    }

    @Override
    public V rpop(K key) {
        return redisCommands.rpop(key);
    }

    @Override
    public List<V> rpop(K key, long count) {
        return redisCommands.rpop(key, count);
    }

    @Override
    public V rpoplpush(K source, K destination) {
        return redisCommands.rpoplpush(source, destination);
    }

    @Override
    @SafeVarargs
    public final Long rpush(K key, V... values) {
        return redisCommands.rpush(key, values);
    }

    @Override
    @SafeVarargs
    public final Long rpushx(K key, V... values) {
        return redisCommands.rpushx(key, values);
    }

    @Override
    @SafeVarargs
    public final <T> T eval(String script, ScriptOutputType type, K... keys) {
        return redisCommands.eval(script, type, keys);
    }

    @Override
    @SafeVarargs
    public final <T> T eval(byte[] script, ScriptOutputType type, K... keys) {
        return redisCommands.eval(script, type, keys);
    }

    @Override
    @SafeVarargs
    public final <T> T eval(String script, ScriptOutputType type, K[] keys, V... values) {
        return redisCommands.eval(script, type, keys, values);
    }

    @Override
    @SafeVarargs
    public final <T> T eval(byte[] script, ScriptOutputType type, K[] keys, V... values) {
        return redisCommands.eval(script, type, keys, values);
    }

    @Override
    @SafeVarargs
    public final <T> T evalReadOnly(String script, ScriptOutputType type, K[] keys, V... values) {
        return redisCommands.evalReadOnly(script, type, keys, values);
    }

    @Override
    @SafeVarargs
    public final <T> T evalReadOnly(byte[] script, ScriptOutputType type, K[] keys, V... values) {
        return redisCommands.evalReadOnly(script, type, keys, values);
    }

    @Override
    @SafeVarargs
    public final <T> T evalsha(String digest, ScriptOutputType type, K... keys) {
        return redisCommands.evalsha(digest, type, keys);
    }

    @Override
    @SafeVarargs
    public final <T> T evalsha(String digest, ScriptOutputType type, K[] keys, V... values) {
        return redisCommands.evalsha(digest, type, keys, values);
    }

    @Override
    @SafeVarargs
    public final <T> T evalshaReadOnly(String digest, ScriptOutputType type, K[] keys, V... values) {
        return redisCommands.evalshaReadOnly(digest, type, keys, values);
    }

    @Override
    public List<Boolean> scriptExists(String... digests) {
        return redisCommands.scriptExists(digests);
    }

    @Override
    public String scriptFlush() {
        return redisCommands.scriptFlush();
    }

    @Override
    public String scriptFlush(FlushMode flushMode) {
        return redisCommands.scriptFlush(flushMode);
    }

    @Override
    public String scriptKill() {
        return redisCommands.scriptKill();
    }

    @Override
    public String scriptLoad(String script) {
        return redisCommands.scriptLoad(script);
    }

    @Override
    public String scriptLoad(byte[] script) {
        return redisCommands.scriptLoad(script);
    }

    @Override
    public String digest(String script) {
        return redisCommands.digest(script);
    }

    @Override
    public String digest(byte[] script) {
        return redisCommands.digest(script);
    }

    @Override
    public String bgrewriteaof() {
        return redisCommands.bgrewriteaof();
    }

    @Override
    public String bgsave() {
        return redisCommands.bgsave();
    }

    @Override
    public String clientCaching(boolean enabled) {
        return redisCommands.clientCaching(enabled);
    }

    @Override
    public K clientGetname() {
        return redisCommands.clientGetname();
    }

    @Override
    public Long clientGetredir() {
        return redisCommands.clientGetredir();
    }

    @Override
    public Long clientId() {
        return redisCommands.clientId();
    }

    @Override
    public String clientKill(String addr) {
        return redisCommands.clientKill(addr);
    }

    @Override
    public Long clientKill(KillArgs killArgs) {
        return redisCommands.clientKill(killArgs);
    }

    @Override
    public String clientList() {
        return redisCommands.clientList();
    }

    @Override
    public String clientList(ClientListArgs clientListArgs) {
        return redisCommands.clientList(clientListArgs);
    }

    @Override
    public String clientInfo() {
        return redisCommands.clientInfo();
    }

    @Override
    public String clientNoEvict(boolean on) {
        return redisCommands.clientNoEvict(on);
    }

    @Override
    public String clientPause(long timeout) {
        return redisCommands.clientPause(timeout);
    }

    @Override
    public String clientSetname(K name) {
        return redisCommands.clientSetname(name);
    }

    @Override
    public String clientSetinfo(String key, String value) {
        return redisCommands.clientSetinfo(key, value);
    }

    @Override
    public String clientTracking(TrackingArgs args) {
        return redisCommands.clientTracking(args);
    }

    @Override
    public TrackingInfo clientTrackinginfo() {
        return redisCommands.clientTrackinginfo();
    }

    @Override
    public Long clientUnblock(long id, UnblockType type) {
        return redisCommands.clientUnblock(id, type);
    }

    @Override
    public List<Object> command() {
        return redisCommands.command();
    }

    @Override
    public Long commandCount() {
        return redisCommands.commandCount();
    }

    @Override
    public List<Object> commandInfo(String... commands) {
        return redisCommands.commandInfo(commands);
    }

    @Override
    public List<Object> commandInfo(CommandType... commands) {
        return redisCommands.commandInfo(commands);
    }

    @Override
    public Map<String, String> configGet(String parameter) {
        return redisCommands.configGet(parameter);
    }

    @Override
    public Map<String, String> configGet(String... parameters) {
        return redisCommands.configGet(parameters);
    }

    @Override
    public String configResetstat() {
        return redisCommands.configResetstat();
    }

    @Override
    public String configRewrite() {
        return redisCommands.configRewrite();
    }

    @Override
    public String configSet(String parameter, String value) {
        return redisCommands.configSet(parameter, value);
    }

    @Override
    public String configSet(Map<String, String> kvs) {
        return redisCommands.configSet(kvs);
    }

    @Override
    public Long dbsize() {
        return redisCommands.dbsize();
    }

    @Override
    public String debugCrashAndRecover(Long delay) {
        return redisCommands.debugCrashAndRecover(delay);
    }

    @Override
    public String debugHtstats(int db) {
        return redisCommands.debugHtstats(db);
    }

    @Override
    public String debugObject(K key) {
        return redisCommands.debugObject(key);
    }

    @Override
    public void debugOom() {
        redisCommands.debugOom();
    }

    @Override
    public String debugReload() {
        return redisCommands.debugReload();
    }

    @Override
    public String debugRestart(Long delay) {
        return redisCommands.debugRestart(delay);
    }

    @Override
    public String debugSdslen(K key) {
        return redisCommands.debugSdslen(key);
    }

    @Override
    public void debugSegfault() {
        redisCommands.debugSegfault();
    }

    @Override
    public String flushall() {
        return redisCommands.flushall();
    }

    @Override
    public String flushall(FlushMode flushMode) {
        return redisCommands.flushall(flushMode);
    }

    @Override
    public String flushallAsync() {
        return redisCommands.flushallAsync();
    }

    @Override
    public String flushdb() {
        return redisCommands.flushdb();
    }

    @Override
    public String flushdb(FlushMode flushMode) {
        return redisCommands.flushdb(flushMode);
    }

    @Override
    public String flushdbAsync() {
        return redisCommands.flushdbAsync();
    }

    @Override
    public String info() {
        return redisCommands.info();
    }

    @Override
    public String info(String section) {
        return redisCommands.info(section);
    }

    @Override
    public Date lastsave() {
        return redisCommands.lastsave();
    }

    @Override
    public Long memoryUsage(K key) {
        return redisCommands.memoryUsage(key);
    }

    @Override
    public String replicaof(String host, int port) {
        return redisCommands.replicaof(host, port);
    }

    @Override
    public String replicaofNoOne() {
        return redisCommands.replicaofNoOne();
    }

    @Override
    public String save() {
        return redisCommands.save();
    }

    @Override
    public void shutdown(boolean save) {
        redisCommands.shutdown(save);
    }

    @Override
    public void shutdown(ShutdownArgs args) {
        redisCommands.shutdown(args);
    }

    @Override
    public String slaveof(String host, int port) {
        return redisCommands.slaveof(host, port);
    }

    @Override
    public String slaveofNoOne() {
        return redisCommands.slaveofNoOne();
    }

    @Override
    public List<Object> slowlogGet() {
        return redisCommands.slowlogGet();
    }

    @Override
    public List<Object> slowlogGet(int count) {
        return redisCommands.slowlogGet(count);
    }

    @Override
    public Long slowlogLen() {
        return redisCommands.slowlogLen();
    }

    @Override
    public String slowlogReset() {
        return redisCommands.slowlogReset();
    }

    @Override
    public List<V> time() {
        return redisCommands.time();
    }

    @Override
    @SafeVarargs
    public final Long sadd(K key, V... members) {
        return redisCommands.sadd(key, members);
    }

    @Override
    public Long scard(K key) {
        return redisCommands.scard(key);
    }

    @Override
    @SafeVarargs
    public final Set<V> sdiff(K... keys) {
        return redisCommands.sdiff(keys);
    }

    @Override
    @SafeVarargs
    public final Long sdiff(ValueStreamingChannel<V> channel, K... keys) {
        return redisCommands.sdiff(channel, keys);
    }

    @Override
    @SafeVarargs
    public final Long sdiffstore(K destination, K... keys) {
        return redisCommands.sdiffstore(destination, keys);
    }

    @Override
    @SafeVarargs
    public final Set<V> sinter(K... keys) {
        return redisCommands.sinter(keys);
    }

    @Override
    @SafeVarargs
    public final Long sinter(ValueStreamingChannel<V> channel, K... keys) {
        return redisCommands.sinter(channel, keys);
    }

    @Override
    @SafeVarargs
    public final Long sintercard(K... keys) {
        return redisCommands.sintercard(keys);
    }

    @Override
    @SafeVarargs
    public final Long sintercard(long limit, K... keys) {
        return redisCommands.sintercard(limit, keys);
    }

    @Override
    @SafeVarargs
    public final Long sinterstore(K destination, K... keys) {
        return redisCommands.sinterstore(destination, keys);
    }

    @Override
    public Boolean sismember(K key, V member) {
        return redisCommands.sismember(key, member);
    }

    @Override
    public Set<V> smembers(K key) {
        return redisCommands.smembers(key);
    }

    @Override
    public Long smembers(ValueStreamingChannel<V> channel, K key) {
        return redisCommands.smembers(channel, key);
    }

    @Override
    @SafeVarargs
    public final List<Boolean> smismember(K key, V... members) {
        return redisCommands.smismember(key, members);
    }

    @Override
    public Boolean smove(K source, K destination, V member) {
        return redisCommands.smove(source, destination, member);
    }

    @Override
    public V spop(K key) {
        return redisCommands.spop(key);
    }

    @Override
    public Set<V> spop(K key, long count) {
        return redisCommands.spop(key, count);
    }

    @Override
    public V srandmember(K key) {
        return redisCommands.srandmember(key);
    }

    @Override
    public List<V> srandmember(K key, long count) {
        return redisCommands.srandmember(key, count);
    }

    @Override
    public Long srandmember(ValueStreamingChannel<V> channel, K key, long count) {
        return redisCommands.srandmember(channel, key, count);
    }

    @Override
    @SafeVarargs
    public final Long srem(K key, V... members) {
        return redisCommands.srem(key, members);
    }

    @Override
    @SafeVarargs
    public final Set<V> sunion(K... keys) {
        return redisCommands.sunion(keys);
    }

    @Override
    @SafeVarargs
    public final Long sunion(ValueStreamingChannel<V> channel, K... keys) {
        return redisCommands.sunion(channel, keys);
    }

    @Override
    @SafeVarargs
    public final Long sunionstore(K destination, K... keys) {
        return redisCommands.sunionstore(destination, keys);
    }

    @Override
    public ValueScanCursor<V> sscan(K key) {
        return redisCommands.sscan(key);
    }

    @Override
    public ValueScanCursor<V> sscan(K key, ScanArgs scanArgs) {
        return redisCommands.sscan(key, scanArgs);
    }

    @Override
    public ValueScanCursor<V> sscan(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.sscan(key, scanCursor, scanArgs);
    }

    @Override
    public ValueScanCursor<V> sscan(K key, ScanCursor scanCursor) {
        return redisCommands.sscan(key, scanCursor);
    }

    @Override
    public StreamScanCursor sscan(ValueStreamingChannel<V> channel, K key) {
        return redisCommands.sscan(channel, key);
    }

    @Override
    public StreamScanCursor sscan(ValueStreamingChannel<V> channel, K key, ScanArgs scanArgs) {
        return redisCommands.sscan(channel, key, scanArgs);
    }

    @Override
    public StreamScanCursor sscan(ValueStreamingChannel<V> channel, K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.sscan(channel, key, scanCursor, scanArgs);
    }

    @Override
    public StreamScanCursor sscan(ValueStreamingChannel<V> channel, K key, ScanCursor scanCursor) {
        return redisCommands.sscan(channel, key, scanCursor);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, ScoredValue<V>> bzmpop(long timeout, ZPopArgs args, K... keys) {
        return redisCommands.bzmpop(timeout, args, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, List<ScoredValue<V>>> bzmpop(long timeout, long count, ZPopArgs args, K... keys) {
        return redisCommands.bzmpop(timeout, count, args, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, ScoredValue<V>> bzmpop(double timeout, ZPopArgs args, K... keys) {
        return redisCommands.bzmpop(timeout, args, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, List<ScoredValue<V>>> bzmpop(double timeout, int count, ZPopArgs args, K... keys) {
        return redisCommands.bzmpop(timeout, count, args, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, ScoredValue<V>> bzpopmin(long timeout, K... keys) {
        return redisCommands.bzpopmin(timeout, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, ScoredValue<V>> bzpopmin(double timeout, K... keys) {
        return redisCommands.bzpopmin(timeout, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, ScoredValue<V>> bzpopmax(long timeout, K... keys) {
        return redisCommands.bzpopmax(timeout, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, ScoredValue<V>> bzpopmax(double timeout, K... keys) {
        return redisCommands.bzpopmax(timeout, keys);
    }

    @Override
    public Long zadd(K key, double score, V member) {
        return redisCommands.zadd(key, score, member);
    }

    @Override
    public Long zadd(K key, Object... scoresAndValues) {
        return redisCommands.zadd(key, scoresAndValues);
    }

    @Override
    @SafeVarargs
    public final Long zadd(K key, ScoredValue<V>... scoredValues) {
        return redisCommands.zadd(key, scoredValues);
    }

    @Override
    public Long zadd(K key, ZAddArgs zAddArgs, double score, V member) {
        return redisCommands.zadd(key, zAddArgs, score, member);
    }

    @Override
    public Long zadd(K key, ZAddArgs zAddArgs, Object... scoresAndValues) {
        return redisCommands.zadd(key, zAddArgs, scoresAndValues);
    }

    @Override
    @SafeVarargs
    public final Long zadd(K key, ZAddArgs zAddArgs, ScoredValue<V>... scoredValues) {
        return redisCommands.zadd(key, zAddArgs, scoredValues);
    }

    @Override
    public Double zaddincr(K key, double score, V member) {
        return redisCommands.zaddincr(key, score, member);
    }

    @Override
    public Double zaddincr(K key, ZAddArgs zAddArgs, double score, V member) {
        return redisCommands.zaddincr(key, zAddArgs, score, member);
    }

    @Override
    public Long zcard(K key) {
        return redisCommands.zcard(key);
    }

    @Override
    public Long zcount(K key, double min, double max) {
        return redisCommands.zcount(key, min, max);
    }

    @Override
    public Long zcount(K key, String min, String max) {
        return redisCommands.zcount(key, min, max);
    }

    @Override
    public Long zcount(K key, Range<? extends Number> range) {
        return redisCommands.zcount(key, range);
    }

    @Override
    @SafeVarargs
    public final List<V> zdiff(K... keys) {
        return redisCommands.zdiff(keys);
    }

    @Override
    @SafeVarargs
    public final Long zdiffstore(K destKey, K... srcKeys) {
        return redisCommands.zdiffstore(destKey, srcKeys);
    }

    @Override
    @SafeVarargs
    public final List<ScoredValue<V>> zdiffWithScores(K... keys) {
        return redisCommands.zdiffWithScores(keys);
    }

    @Override
    public Double zincrby(K key, double amount, V member) {
        return redisCommands.zincrby(key, amount, member);
    }

    @Override
    @SafeVarargs
    public final List<V> zinter(K... keys) {
        return redisCommands.zinter(keys);
    }

    @Override
    @SafeVarargs
    public final List<V> zinter(ZAggregateArgs aggregateArgs, K... keys) {
        return redisCommands.zinter(aggregateArgs, keys);
    }

    @Override
    @SafeVarargs
    public final Long zintercard(K... keys) {
        return redisCommands.zintercard(keys);
    }

    @Override
    @SafeVarargs
    public final Long zintercard(long limit, K... keys) {
        return redisCommands.zintercard(limit, keys);
    }

    @Override
    @SafeVarargs
    public final List<ScoredValue<V>> zinterWithScores(ZAggregateArgs aggregateArgs, K... keys) {
        return redisCommands.zinterWithScores(aggregateArgs, keys);
    }

    @Override
    @SafeVarargs
    public final List<ScoredValue<V>> zinterWithScores(K... keys) {
        return redisCommands.zinterWithScores(keys);
    }

    @Override
    @SafeVarargs
    public final Long zinterstore(K destination, K... keys) {
        return redisCommands.zinterstore(destination, keys);
    }

    @Override
    @SafeVarargs
    public final Long zinterstore(K destination, ZStoreArgs storeArgs, K... keys) {
        return redisCommands.zinterstore(destination, storeArgs, keys);
    }

    @Override
    public Long zlexcount(K key, String min, String max) {
        return redisCommands.zlexcount(key, min, max);
    }

    @Override
    public Long zlexcount(K key, Range<? extends V> range) {
        return redisCommands.zlexcount(key, range);
    }

    @Override
    @SafeVarargs
    public final List<Double> zmscore(K key, V... members) {
        return redisCommands.zmscore(key, members);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, ScoredValue<V>> zmpop(ZPopArgs args, K... keys) {
        return redisCommands.zmpop(args, keys);
    }

    @Override
    @SafeVarargs
    public final KeyValue<K, List<ScoredValue<V>>> zmpop(int count, ZPopArgs args, K... keys) {
        return redisCommands.zmpop(count, args, keys);
    }

    @Override
    public ScoredValue<V> zpopmin(K key) {
        return redisCommands.zpopmin(key);
    }

    @Override
    public List<ScoredValue<V>> zpopmin(K key, long count) {
        return redisCommands.zpopmin(key, count);
    }

    @Override
    public ScoredValue<V> zpopmax(K key) {
        return redisCommands.zpopmax(key);
    }

    @Override
    public List<ScoredValue<V>> zpopmax(K key, long count) {
        return redisCommands.zpopmax(key, count);
    }

    @Override
    public V zrandmember(K key) {
        return redisCommands.zrandmember(key);
    }

    @Override
    public List<V> zrandmember(K key, long count) {
        return redisCommands.zrandmember(key, count);
    }

    @Override
    public ScoredValue<V> zrandmemberWithScores(K key) {
        return redisCommands.zrandmemberWithScores(key);
    }

    @Override
    public List<ScoredValue<V>> zrandmemberWithScores(K key, long count) {
        return redisCommands.zrandmemberWithScores(key, count);
    }

    @Override
    public List<V> zrange(K key, long start, long stop) {
        return redisCommands.zrange(key, start, stop);
    }

    @Override
    public Long zrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return redisCommands.zrange(channel, key, start, stop);
    }

    @Override
    public List<ScoredValue<V>> zrangeWithScores(K key, long start, long stop) {
        return redisCommands.zrangeWithScores(key, start, stop);
    }

    @Override
    public Long zrangeWithScores(ScoredValueStreamingChannel<V> channel, K key, long start, long stop) {
        return redisCommands.zrangeWithScores(channel, key, start, stop);
    }

    @Override
    public List<V> zrangebylex(K key, String min, String max) {
        return redisCommands.zrangebylex(key, min, max);
    }

    @Override
    public List<V> zrangebylex(K key, Range<? extends V> range) {
        return redisCommands.zrangebylex(key, range);
    }

    @Override
    public List<V> zrangebylex(K key, String min, String max, long offset, long count) {
        return redisCommands.zrangebylex(key, min, max, offset, count);
    }

    @Override
    public List<V> zrangebylex(K key, Range<? extends V> range, Limit limit) {
        return redisCommands.zrangebylex(key, range, limit);
    }

    @Override
    public List<V> zrangebyscore(K key, double min, double max) {
        return redisCommands.zrangebyscore(key, min, max);
    }

    @Override
    public List<V> zrangebyscore(K key, String min, String max) {
        return redisCommands.zrangebyscore(key, min, max);
    }

    @Override
    public List<V> zrangebyscore(K key, Range<? extends Number> range) {
        return redisCommands.zrangebyscore(key, range);
    }

    @Override
    public List<V> zrangebyscore(K key, double min, double max, long offset, long count) {
        return redisCommands.zrangebyscore(key, min, max, offset, count);
    }

    @Override
    public List<V> zrangebyscore(K key, String min, String max, long offset, long count) {
        return redisCommands.zrangebyscore(key, min, max, offset, count);
    }

    @Override
    public List<V> zrangebyscore(K key, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrangebyscore(key, range, limit);
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, double min, double max) {
        return redisCommands.zrangebyscore(channel, key, min, max);
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, String min, String max) {
        return redisCommands.zrangebyscore(channel, key, min, max);
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, Range<? extends Number> range) {
        return redisCommands.zrangebyscore(channel, key, range);
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, double min, double max, long offset, long count) {
        return redisCommands.zrangebyscore(channel, key, min, max, offset, count);
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, String min, String max, long offset, long count) {
        return redisCommands.zrangebyscore(channel, key, min, max, offset, count);
    }

    @Override
    public Long zrangebyscore(ValueStreamingChannel<V> channel, K key, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrangebyscore(channel, key, range, limit);
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, double min, double max) {
        return redisCommands.zrangebyscoreWithScores(key, min, max);
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, String min, String max) {
        return redisCommands.zrangebyscoreWithScores(key, min, max);
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, Range<? extends Number> range) {
        return redisCommands.zrangebyscoreWithScores(key, range);
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, double min, double max, long offset, long count) {
        return redisCommands.zrangebyscoreWithScores(key, min, max, offset, count);
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, String min, String max, long offset, long count) {
        return redisCommands.zrangebyscoreWithScores(key, min, max, offset, count);
    }

    @Override
    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrangebyscoreWithScores(key, range, limit);
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double min, double max) {
        return redisCommands.zrangebyscoreWithScores(channel, key, min, max);
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String min, String max) {
        return redisCommands.zrangebyscoreWithScores(channel, key, min, max);
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, Range<? extends Number> range) {
        return redisCommands.zrangebyscoreWithScores(channel, key, range);
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double min, double max, long offset, long count) {
        return redisCommands.zrangebyscoreWithScores(channel, key, min, max, offset, count);
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String min, String max, long offset, long count) {
        return redisCommands.zrangebyscoreWithScores(channel, key, min, max, offset, count);
    }

    @Override
    public Long zrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrangebyscoreWithScores(channel, key, range, limit);
    }

    @Override
    public Long zrangestore(K dstKey, K srcKey, Range<Long> range) {
        return redisCommands.zrangestore(dstKey, srcKey, range);
    }

    @Override
    public Long zrangestorebylex(K dstKey, K srcKey, Range<? extends V> range, Limit limit) {
        return redisCommands.zrangestorebylex(dstKey, srcKey, range, limit);
    }

    @Override
    public Long zrangestorebyscore(K dstKey, K srcKey, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrangestorebyscore(dstKey, srcKey, range, limit);
    }

    @Override
    public Long zrank(K key, V member) {
        return redisCommands.zrank(key, member);
    }

    @Override
    public ScoredValue<Long> zrankWithScore(K key, V member) {
        return redisCommands.zrankWithScore(key, member);
    }

    @Override
    @SafeVarargs
    public final Long zrem(K key, V... members) {
        return redisCommands.zrem(key, members);
    }

    @Override
    public Long zremrangebylex(K key, String min, String max) {
        return redisCommands.zremrangebylex(key, min, max);
    }

    @Override
    public Long zremrangebylex(K key, Range<? extends V> range) {
        return redisCommands.zremrangebylex(key, range);
    }

    @Override
    public Long zremrangebyrank(K key, long start, long stop) {
        return redisCommands.zremrangebyrank(key, start, stop);
    }

    @Override
    public Long zremrangebyscore(K key, double min, double max) {
        return redisCommands.zremrangebyscore(key, min, max);
    }

    @Override
    public Long zremrangebyscore(K key, String min, String max) {
        return redisCommands.zremrangebyscore(key, min, max);
    }

    @Override
    public Long zremrangebyscore(K key, Range<? extends Number> range) {
        return redisCommands.zremrangebyscore(key, range);
    }

    @Override
    public List<V> zrevrange(K key, long start, long stop) {
        return redisCommands.zrevrange(key, start, stop);
    }

    @Override
    public Long zrevrange(ValueStreamingChannel<V> channel, K key, long start, long stop) {
        return redisCommands.zrevrange(channel, key, start, stop);
    }

    @Override
    public List<ScoredValue<V>> zrevrangeWithScores(K key, long start, long stop) {
        return redisCommands.zrevrangeWithScores(key, start, stop);
    }

    @Override
    public Long zrevrangeWithScores(ScoredValueStreamingChannel<V> channel, K key, long start, long stop) {
        return redisCommands.zrevrangeWithScores(channel, key, start, stop);
    }

    @Override
    public List<V> zrevrangebylex(K key, Range<? extends V> range) {
        return redisCommands.zrevrangebylex(key, range);
    }

    @Override
    public List<V> zrevrangebylex(K key, Range<? extends V> range, Limit limit) {
        return redisCommands.zrevrangebylex(key, range, limit);
    }

    @Override
    public List<V> zrevrangebyscore(K key, double max, double min) {
        return redisCommands.zrevrangebyscore(key, max, min);
    }

    @Override
    public List<V> zrevrangebyscore(K key, String max, String min) {
        return redisCommands.zrevrangebyscore(key, max, min);
    }

    @Override
    public List<V> zrevrangebyscore(K key, Range<? extends Number> range) {
        return redisCommands.zrevrangebyscore(key, range);
    }

    @Override
    public List<V> zrevrangebyscore(K key, double max, double min, long offset, long count) {
        return redisCommands.zrevrangebyscore(key, max, min, offset, count);
    }

    @Override
    public List<V> zrevrangebyscore(K key, String max, String min, long offset, long count) {
        return redisCommands.zrevrangebyscore(key, max, min, offset, count);
    }

    @Override
    public List<V> zrevrangebyscore(K key, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrevrangebyscore(key, range, limit);
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, double max, double min) {
        return redisCommands.zrevrangebyscore(channel, key, max, min);
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, String max, String min) {
        return redisCommands.zrevrangebyscore(channel, key, max, min);
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, Range<? extends Number> range) {
        return redisCommands.zrevrangebyscore(channel, key, range);
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, double max, double min, long offset, long count) {
        return redisCommands.zrevrangebyscore(channel, key, max, min, offset, count);
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, String max, String min, long offset, long count) {
        return redisCommands.zrevrangebyscore(channel, key, max, min, offset, count);
    }

    @Override
    public Long zrevrangebyscore(ValueStreamingChannel<V> channel, K key, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrevrangebyscore(channel, key, range, limit);
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, double max, double min) {
        return redisCommands.zrevrangebyscoreWithScores(key, max, min);
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, String max, String min) {
        return redisCommands.zrevrangebyscoreWithScores(key, max, min);
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, Range<? extends Number> range) {
        return redisCommands.zrevrangebyscoreWithScores(key, range);
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, double max, double min, long offset, long count) {
        return redisCommands.zrevrangebyscoreWithScores(key, max, min, offset, count);
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, String max, String min, long offset, long count) {
        return redisCommands.zrevrangebyscoreWithScores(key, max, min, offset, count);
    }

    @Override
    public List<ScoredValue<V>> zrevrangebyscoreWithScores(K key, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrevrangebyscoreWithScores(key, range, limit);
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double max, double min) {
        return redisCommands.zrevrangebyscoreWithScores(channel, key, max, min);
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String max, String min) {
        return redisCommands.zrevrangebyscoreWithScores(channel, key, max, min);
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, Range<? extends Number> range) {
        return redisCommands.zrevrangebyscoreWithScores(channel, key, range);
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, double max, double min, long offset, long count) {
        return redisCommands.zrevrangebyscoreWithScores(channel, key, max, min, offset, count);
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, String max, String min, long offset, long count) {
        return redisCommands.zrevrangebyscoreWithScores(channel, key, max, min, offset, count);
    }

    @Override
    public Long zrevrangebyscoreWithScores(ScoredValueStreamingChannel<V> channel, K key, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrevrangebyscoreWithScores(channel, key, range, limit);
    }

    @Override
    public Long zrevrangestore(K dstKey, K srcKey, Range<Long> range) {
        return redisCommands.zrevrangestore(dstKey, srcKey, range);
    }

    @Override
    public Long zrevrangestorebylex(K dstKey, K srcKey, Range<? extends V> range, Limit limit) {
        return redisCommands.zrevrangestorebylex(dstKey, srcKey, range, limit);
    }

    @Override
    public Long zrevrangestorebyscore(K dstKey, K srcKey, Range<? extends Number> range, Limit limit) {
        return redisCommands.zrevrangestorebyscore(dstKey, srcKey, range, limit);
    }

    @Override
    public Long zrevrank(K key, V member) {
        return redisCommands.zrevrank(key, member);
    }

    @Override
    public ScoredValue<Long> zrevrankWithScore(K key, V member) {
        return redisCommands.zrevrankWithScore(key, member);
    }

    @Override
    public ScoredValueScanCursor<V> zscan(K key) {
        return redisCommands.zscan(key);
    }

    @Override
    public ScoredValueScanCursor<V> zscan(K key, ScanArgs scanArgs) {
        return redisCommands.zscan(key, scanArgs);
    }

    @Override
    public ScoredValueScanCursor<V> zscan(K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.zscan(key, scanCursor, scanArgs);
    }

    @Override
    public ScoredValueScanCursor<V> zscan(K key, ScanCursor scanCursor) {
        return redisCommands.zscan(key, scanCursor);
    }

    @Override
    public StreamScanCursor zscan(ScoredValueStreamingChannel<V> channel, K key) {
        return redisCommands.zscan(channel, key);
    }

    @Override
    public StreamScanCursor zscan(ScoredValueStreamingChannel<V> channel, K key, ScanArgs scanArgs) {
        return redisCommands.zscan(channel, key, scanArgs);
    }

    @Override
    public StreamScanCursor zscan(ScoredValueStreamingChannel<V> channel, K key, ScanCursor scanCursor, ScanArgs scanArgs) {
        return redisCommands.zscan(channel, key, scanCursor, scanArgs);
    }

    @Override
    public StreamScanCursor zscan(ScoredValueStreamingChannel<V> channel, K key, ScanCursor scanCursor) {
        return redisCommands.zscan(channel, key, scanCursor);
    }

    @Override
    public Double zscore(K key, V member) {
        return redisCommands.zscore(key, member);
    }

    @Override
    @SafeVarargs
    public final List<V> zunion(K... keys) {
        return redisCommands.zunion(keys);
    }

    @Override
    @SafeVarargs
    public final List<V> zunion(ZAggregateArgs aggregateArgs, K... keys) {
        return redisCommands.zunion(aggregateArgs, keys);
    }

    @Override
    @SafeVarargs
    public final List<ScoredValue<V>> zunionWithScores(ZAggregateArgs aggregateArgs, K... keys) {
        return redisCommands.zunionWithScores(aggregateArgs, keys);
    }

    @Override
    @SafeVarargs
    public final List<ScoredValue<V>> zunionWithScores(K... keys) {
        return redisCommands.zunionWithScores(keys);
    }

    @Override
    @SafeVarargs
    public final Long zunionstore(K destination, K... keys) {
        return redisCommands.zunionstore(destination, keys);
    }

    @Override
    @SafeVarargs
    public final Long zunionstore(K destination, ZStoreArgs storeArgs, K... keys) {
        return redisCommands.zunionstore(destination, storeArgs, keys);
    }

    @Override
    public Long xack(K key, K group, String... messageIds) {
        return redisCommands.xack(key, group, messageIds);
    }

    @Override
    public String xadd(K key, Map<K, V> body) {
        return redisCommands.xadd(key, body);
    }

    @Override
    public String xadd(K key, XAddArgs args, Map<K, V> body) {
        return redisCommands.xadd(key, args, body);
    }

    @Override
    public String xadd(K key, Object... keysAndValues) {
        return redisCommands.xadd(key, keysAndValues);
    }

    @Override
    public String xadd(K key, XAddArgs args, Object... keysAndValues) {
        return redisCommands.xadd(key, args, keysAndValues);
    }

    @Override
    public ClaimedMessages<K, V> xautoclaim(K key, XAutoClaimArgs<K> args) {
        return redisCommands.xautoclaim(key, args);
    }

    @Override
    public List<StreamMessage<K, V>> xclaim(K key, Consumer<K> consumer, long minIdleTime, String... messageIds) {
        return redisCommands.xclaim(key, consumer, minIdleTime, messageIds);
    }

    @Override
    public List<StreamMessage<K, V>> xclaim(K key, Consumer<K> consumer, XClaimArgs args, String... messageIds) {
        return redisCommands.xclaim(key, consumer, args, messageIds);
    }

    @Override
    public Long xdel(K key, String... messageIds) {
        return redisCommands.xdel(key, messageIds);
    }

    @Override
    public String xgroupCreate(StreamOffset<K> streamOffset, K group) {
        return redisCommands.xgroupCreate(streamOffset, group);
    }

    @Override
    public String xgroupCreate(StreamOffset<K> streamOffset, K group, XGroupCreateArgs args) {
        return redisCommands.xgroupCreate(streamOffset, group, args);
    }

    @Override
    public Boolean xgroupCreateconsumer(K key, Consumer<K> consumer) {
        return redisCommands.xgroupCreateconsumer(key, consumer);
    }

    @Override
    public Long xgroupDelconsumer(K key, Consumer<K> consumer) {
        return redisCommands.xgroupDelconsumer(key, consumer);
    }

    @Override
    public Boolean xgroupDestroy(K key, K group) {
        return redisCommands.xgroupDestroy(key, group);
    }

    @Override
    public String xgroupSetid(StreamOffset<K> streamOffset, K group) {
        return redisCommands.xgroupSetid(streamOffset, group);
    }

    @Override
    public List<Object> xinfoStream(K key) {
        return redisCommands.xinfoStream(key);
    }

    @Override
    public List<Object> xinfoGroups(K key) {
        return redisCommands.xinfoGroups(key);
    }

    @Override
    public List<Object> xinfoConsumers(K key, K group) {
        return redisCommands.xinfoConsumers(key, group);
    }

    @Override
    public Long xlen(K key) {
        return redisCommands.xlen(key);
    }

    @Override
    public PendingMessages xpending(K key, K group) {
        return redisCommands.xpending(key, group);
    }

    @Override
    public List<PendingMessage> xpending(K key, K group, Range<String> range, Limit limit) {
        return redisCommands.xpending(key, group, range, limit);
    }

    @Override
    public List<PendingMessage> xpending(K key, Consumer<K> consumer, Range<String> range, Limit limit) {
        return redisCommands.xpending(key, consumer, range, limit);
    }

    @Override
    public List<PendingMessage> xpending(K key, XPendingArgs<K> args) {
        return redisCommands.xpending(key, args);
    }

    @Override
    public List<StreamMessage<K, V>> xrange(K key, Range<String> range) {
        return redisCommands.xrange(key, range);
    }

    @Override
    public List<StreamMessage<K, V>> xrange(K key, Range<String> range, Limit limit) {
        return redisCommands.xrange(key, range, limit);
    }

    @Override
    @SafeVarargs
    public final List<StreamMessage<K, V>> xread(StreamOffset<K>... streams) {
        return redisCommands.xread(streams);
    }

    @Override
    @SafeVarargs
    public final List<StreamMessage<K, V>> xread(XReadArgs args, StreamOffset<K>... streams) {
        return redisCommands.xread(args, streams);
    }

    @Override
    @SafeVarargs
    public final List<StreamMessage<K, V>> xreadgroup(Consumer<K> consumer, StreamOffset<K>... streams) {
        return redisCommands.xreadgroup(consumer, streams);
    }

    @Override
    @SafeVarargs
    public final List<StreamMessage<K, V>> xreadgroup(Consumer<K> consumer, XReadArgs args, StreamOffset<K>... streams) {
        return redisCommands.xreadgroup(consumer, args, streams);
    }

    @Override
    public List<StreamMessage<K, V>> xrevrange(K key, Range<String> range) {
        return redisCommands.xrevrange(key, range);
    }

    @Override
    public List<StreamMessage<K, V>> xrevrange(K key, Range<String> range, Limit limit) {
        return redisCommands.xrevrange(key, range, limit);
    }

    @Override
    public Long xtrim(K key, long count) {
        return redisCommands.xtrim(key, count);
    }

    @Override
    public Long xtrim(K key, boolean approximateTrimming, long count) {
        return redisCommands.xtrim(key, approximateTrimming, count);
    }

    @Override
    public Long xtrim(K key, XTrimArgs args) {
        return redisCommands.xtrim(key, args);
    }

    @Override
    public Long append(K key, V value) {
        return redisCommands.append(key, value);
    }

    @Override
    public Long bitcount(K key) {
        return redisCommands.bitcount(key);
    }

    @Override
    public Long bitcount(K key, long start, long end) {
        return redisCommands.bitcount(key, start, end);
    }

    @Override
    public List<Long> bitfield(K key, BitFieldArgs bitFieldArgs) {
        return redisCommands.bitfield(key, bitFieldArgs);
    }

    @Override
    public Long bitpos(K key, boolean state) {
        return redisCommands.bitpos(key, state);
    }

    @Override
    public Long bitpos(K key, boolean state, long start) {
        return redisCommands.bitpos(key, state, start);
    }

    @Override
    public Long bitpos(K key, boolean state, long start, long end) {
        return redisCommands.bitpos(key, state, start, end);
    }

    @Override
    @SafeVarargs
    public final Long bitopAnd(K destination, K... keys) {
        return redisCommands.bitopAnd(destination, keys);
    }

    @Override
    public Long bitopNot(K destination, K source) {
        return redisCommands.bitopNot(destination, source);
    }

    @Override
    @SafeVarargs
    public final Long bitopOr(K destination, K... keys) {
        return redisCommands.bitopOr(destination, keys);
    }

    @Override
    @SafeVarargs
    public final Long bitopXor(K destination, K... keys) {
        return redisCommands.bitopXor(destination, keys);
    }

    @Override
    public Long decr(K key) {
        return redisCommands.decr(key);
    }

    @Override
    public Long decrby(K key, long amount) {
        return redisCommands.decrby(key, amount);
    }

    @Override
    public V get(K key) {
        return redisCommands.get(key);
    }

    @Override
    public Long getbit(K key, long offset) {
        return redisCommands.getbit(key, offset);
    }

    @Override
    public V getdel(K key) {
        return redisCommands.getdel(key);
    }

    @Override
    public V getex(K key, GetExArgs args) {
        return redisCommands.getex(key, args);
    }

    @Override
    public V getrange(K key, long start, long end) {
        return redisCommands.getrange(key, start, end);
    }

    @Override
    public V getset(K key, V value) {
        return redisCommands.getset(key, value);
    }

    @Override
    public Long incr(K key) {
        return redisCommands.incr(key);
    }

    @Override
    public Long incrby(K key, long amount) {
        return redisCommands.incrby(key, amount);
    }

    @Override
    public Double incrbyfloat(K key, double amount) {
        return redisCommands.incrbyfloat(key, amount);
    }

    @Override
    @SafeVarargs
    public final List<KeyValue<K, V>> mget(K... keys) {
        return redisCommands.mget(keys);
    }

    @Override
    @SafeVarargs
    public final Long mget(KeyValueStreamingChannel<K, V> channel, K... keys) {
        return redisCommands.mget(channel, keys);
    }

    @Override
    public String mset(Map<K, V> map) {
        return redisCommands.mset(map);
    }

    @Override
    public Boolean msetnx(Map<K, V> map) {
        return redisCommands.msetnx(map);
    }

    @Override
    public String set(K key, V value) {
        return redisCommands.set(key, value);
    }

    @Override
    public String set(K key, V value, SetArgs setArgs) {
        return redisCommands.set(key, value, setArgs);
    }

    @Override
    public V setGet(K key, V value) {
        return redisCommands.setGet(key, value);
    }

    @Override
    public V setGet(K key, V value, SetArgs setArgs) {
        return redisCommands.setGet(key, value, setArgs);
    }

    @Override
    public Long setbit(K key, long offset, int value) {
        return redisCommands.setbit(key, offset, value);
    }

    @Override
    public String setex(K key, long seconds, V value) {
        return redisCommands.setex(key, seconds, value);
    }

    @Override
    public String psetex(K key, long milliseconds, V value) {
        return redisCommands.psetex(key, milliseconds, value);
    }

    @Override
    public Boolean setnx(K key, V value) {
        return redisCommands.setnx(key, value);
    }

    @Override
    public Long setrange(K key, long offset, V value) {
        return redisCommands.setrange(key, offset, value);
    }

    @Override
    public StringMatchResult stralgoLcs(StrAlgoArgs strAlgoArgs) {
        return redisCommands.stralgoLcs(strAlgoArgs);
    }

    @Override
    public Long strlen(K key) {
        return redisCommands.strlen(key);
    }

    @Override
    public String auth(CharSequence password) {
        return redisCommands.auth(password);
    }

    @Override
    public String auth(String username, CharSequence password) {
        return redisCommands.auth(username, password);
    }

    @Override
    public String select(int db) {
        return redisCommands.select(db);
    }

    @Override
    public String swapdb(int db1, int db2) {
        return redisCommands.swapdb(db1, db2);
    }

    @Override
    public StatefulRedisConnection<K, V> getStatefulConnection() {
        return redisCommands.getStatefulConnection();
    }

    @Override
    public JsonParser getJsonParser() {
        return redisCommands.getJsonParser();
    }

    @Override
    public String discard() {
        return redisCommands.discard();
    }

    @Override
    public TransactionResult exec() {
        return redisCommands.exec();
    }

    @Override
    public String multi() {
        return redisCommands.multi();
    }

    @Override
    @SafeVarargs
    public final String watch(K... keys) {
        return redisCommands.watch(keys);
    }

    @Override
    public String unwatch() {
        return redisCommands.unwatch();
    }

    @Override
    public void setTimeout(Duration timeout) {
        redisCommands.setTimeout(timeout);
    }

    @Override
    public String asking() {
        return redisCommands.asking();
    }

    @Override
    public String clusterAddSlots(int... slots) {
        return redisCommands.clusterAddSlots(slots);
    }

    @Override
    public String clusterBumpepoch() {
        return redisCommands.clusterBumpepoch();
    }

    @Override
    public Long clusterCountFailureReports(String nodeId) {
        return redisCommands.clusterCountFailureReports(nodeId);
    }

    @Override
    public Long clusterCountKeysInSlot(int slot) {
        return redisCommands.clusterCountKeysInSlot(slot);
    }

    @Override
    @SafeVarargs
    public final String clusterAddSlotsRange(Range<Integer>... ranges) {
        return redisCommands.clusterAddSlotsRange(ranges);
    }

    @Override
    public String clusterDelSlots(int... slots) {
        return redisCommands.clusterDelSlots(slots);
    }

    @Override
    @SafeVarargs
    public final String clusterDelSlotsRange(Range<Integer>... ranges) {
        return redisCommands.clusterDelSlotsRange(ranges);
    }

    @Override
    public String clusterFailover(boolean force) {
        return redisCommands.clusterFailover(force);
    }

    @Override
    public String clusterFailover(boolean force, boolean takeOver) {
        return redisCommands.clusterFailover(force, takeOver);
    }

    @Override
    public String clusterFlushslots() {
        return redisCommands.clusterFlushslots();
    }

    @Override
    public String clusterForget(String nodeId) {
        return redisCommands.clusterForget(nodeId);
    }

    @Override
    public List<K> clusterGetKeysInSlot(int slot, int count) {
        return redisCommands.clusterGetKeysInSlot(slot, count);
    }

    @Override
    public String clusterInfo() {
        return redisCommands.clusterInfo();
    }

    @Override
    public Long clusterKeyslot(K key) {
        return redisCommands.clusterKeyslot(key);
    }

    @Override
    public String clusterMeet(String ip, int port) {
        return redisCommands.clusterMeet(ip, port);
    }

    @Override
    public String clusterMyId() {
        return redisCommands.clusterMyId();
    }

    @Override
    public String clusterMyShardId() {
        return redisCommands.clusterMyShardId();
    }

    @Override
    public String clusterNodes() {
        return redisCommands.clusterNodes();
    }

    @Override
    public String clusterReplicate(String nodeId) {
        return redisCommands.clusterReplicate(nodeId);
    }

    @Override
    public List<String> clusterReplicas(String nodeId) {
        return redisCommands.clusterReplicas(nodeId);
    }

    @Override
    public String clusterReset(boolean hard) {
        return redisCommands.clusterReset(hard);
    }

    @Override
    public String clusterSaveconfig() {
        return redisCommands.clusterSaveconfig();
    }

    @Override
    public String clusterSetConfigEpoch(long configEpoch) {
        return redisCommands.clusterSetConfigEpoch(configEpoch);
    }

    @Override
    public String clusterSetSlotImporting(int slot, String nodeId) {
        return redisCommands.clusterSetSlotImporting(slot, nodeId);
    }

    @Override
    public String clusterSetSlotMigrating(int slot, String nodeId) {
        return redisCommands.clusterSetSlotMigrating(slot, nodeId);
    }

    @Override
    public String clusterSetSlotNode(int slot, String nodeId) {
        return redisCommands.clusterSetSlotNode(slot, nodeId);
    }

    @Override
    public String clusterSetSlotStable(int slot) {
        return redisCommands.clusterSetSlotStable(slot);
    }

    @Override
    public List<Object> clusterShards() {
        return redisCommands.clusterShards();
    }

    @Override
    public List<String> clusterSlaves(String nodeId) {
        return redisCommands.clusterSlaves(nodeId);
    }

    @Override
    public List<Object> clusterSlots() {
        return redisCommands.clusterSlots();
    }

    @Override
    public List<Map<String, Object>> clusterLinks() {
        return redisCommands.clusterLinks();
    }

    public StatefulConnection<K, V> getConnection() {
        return connection;
    }

}