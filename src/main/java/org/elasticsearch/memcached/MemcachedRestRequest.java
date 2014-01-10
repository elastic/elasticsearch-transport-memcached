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

package org.elasticsearch.memcached;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.support.RestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class MemcachedRestRequest extends RestRequest {

    private final Method method;
    private final String uri;
    private final byte[] uriBytes;
    private final int dataSize;
    private boolean binary;
    private final Map<String, String> params;
    private final String rawPath;
    private BytesReference data;
    private int opaque;
    private boolean quiet;

    public MemcachedRestRequest(Method method, String uri, byte[] uriBytes, int dataSize, boolean binary) {
        this.method = method;
        this.uri = uri;
        this.uriBytes = uriBytes;
        this.dataSize = dataSize;
        this.binary = binary;
        this.params = new HashMap<String, String>();
        int pathEndPos = uri.indexOf('?');
        if (pathEndPos < 0) {
            this.rawPath = uri;
        } else {
            this.rawPath = uri.substring(0, pathEndPos);
            RestUtils.decodeQueryString(uri, pathEndPos + 1, params);
        }
    }

    @Override
    public Method method() {
        return this.method;
    }

    @Override
    public String uri() {
        return this.uri;
    }

    @Override
    public String rawPath() {
        return this.rawPath;
    }

    public byte[] getUriBytes() {
        return uriBytes;
    }

    public boolean isBinary() {
        return binary;
    }

    public int getOpaque() {
        return opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void setData(BytesReference data) {
        this.data = data;
    }

    @Override
    public boolean hasContent() {
        return data != null;
    }

    @Override
    public boolean contentUnsafe() {
        // we still slice on teh network buffer, but its always copied in (this version) of netty
        return false;
    }

    @Override
    public BytesReference content() {
        return data;
    }

    @Override
    public String header(String name) {
        return null;
    }

    @Override
    public Iterable<Map.Entry<String, String>> headers() {
        return null;
    }

    @Override
    public boolean hasParam(String key) {
        return params.containsKey(key);
    }

    @Override
    public String param(String key) {
        return params.get(key);
    }

    @Override
    public Map<String, String> params() {
        return params;
    }

    @Override
    public String param(String key, String defaultValue) {
        String value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
