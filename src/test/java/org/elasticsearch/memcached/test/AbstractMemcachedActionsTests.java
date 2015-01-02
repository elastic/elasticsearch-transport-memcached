/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.memcached.test;

import net.spy.memcached.MemcachedClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.PluginsService;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.hamcrest.Matchers.equalTo;

/**
 */
@ElasticsearchIntegrationTest.ClusterScope(transportClientRatio = 0.0,
        scope = ElasticsearchIntegrationTest.Scope.SUITE)
public abstract class AbstractMemcachedActionsTests extends ElasticsearchIntegrationTest {

    private MemcachedClient memcachedClient;
    public static int getPort(int nodeOrdinal) {
        try {
            return PropertiesHelper.getAsInt("plugin.port")
                    + nodeOrdinal * 10;
        } catch (IOException e) {
        }

        return -1;
    }

    @Before
    public void startMemcache() throws IOException {
        memcachedClient = createMemcachedClient();
    }

    protected abstract MemcachedClient createMemcachedClient() throws IOException;

    @After
    public void stopMemcache() {
        if (memcachedClient != null) {
            memcachedClient.shutdown();
        }
    }

    @Override
    protected Settings nodeSettings(int nodeOrdinal) {
        return ImmutableSettings.builder()
                .put("memcached.port", getPort(nodeOrdinal))
                .put("plugins." + PluginsService.LOAD_PLUGIN_FROM_CLASSPATH, true)
                .build();
    }

    @Override
    public Settings indexSettings() {
        ImmutableSettings.Builder builder = ImmutableSettings.builder()
                .put("number_of_shards", 1)
                .put("number_of_replicas", 0);
        return builder.build();
    }

    @Test
    public void testSimpleOperations() throws Exception {
        // TODO seems to use SetQ, which is not really supported yet
//        List<Future<Boolean>> setResults = Lists.newArrayList();
//
//        for (int i = 0; i < 10; i++) {
//            setResults.add(memcachedClient.set("/test/person/" + i, 0, jsonBuilder().startObject().field("test", "value").endObject().copiedBytes()));
//        }
//
//        for (Future<Boolean> setResult : setResults) {
//            assertThat(setResult.get(10, TimeUnit.SECONDS), equalTo(true));
//        }

        createIndex("test");

        Future<Boolean> setResult = memcachedClient.set("/test/person/1", 0, jsonBuilder().startObject().field("test", "value").endObject().bytes().copyBytesArray().array());
        assertThat(setResult.get(10, TimeUnit.SECONDS), equalTo(true));

        ensureYellow();

        String getResult = (String) memcachedClient.get("/_refresh");
        logger.info(" --> REFRESH " + getResult);
        assertThat(getResult, Matchers.containsString("\"total\":1"));
        assertThat(getResult, Matchers.containsString("\"successful\":1"));
        assertThat(getResult, Matchers.containsString("\"failed\":0"));

        getResult = (String) memcachedClient.get("/test/person/1");
        logger.info(" --> GET " + getResult);
        assertThat(getResult, Matchers.containsString("\"_index\":\"test\""));
        assertThat(getResult, Matchers.containsString("\"_type\":\"person\""));
        assertThat(getResult, Matchers.containsString("\"_id\":\"1\""));

        Future<Boolean> deleteResult = memcachedClient.delete("/test/person/1");
        assertThat(deleteResult.get(10, TimeUnit.SECONDS), equalTo(true));

        getResult = (String) memcachedClient.get("/_refresh");
        logger.info(" --> REFRESH " + getResult);
        assertThat(getResult, Matchers.containsString("\"total\":1"));
        assertThat(getResult, Matchers.containsString("\"successful\":1"));
        assertThat(getResult, Matchers.containsString("\"failed\":0"));

        getResult = (String) memcachedClient.get("/test/person/1");
        logger.info(" --> GET " + getResult);
    }
}
