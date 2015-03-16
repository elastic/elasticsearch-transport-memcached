Memcached Transport for Elasticsearch
==================================

The memcached transport plugin allows to use the REST interface over memcached (though with limitations).
The memcached protocol supports both the binary and the text protocol, automatically detecting the correct one to use.

In order to install the plugin, simply run: 

```sh
bin/plugin -install elasticsearch/elasticsearch-transport-memcached/2.4.1
```

You need to install a version matching your Elasticsearch version:

| elasticsearch |   memcached Plugin    |   Docs     |  
|---------------|-----------------------|------------|
| master        |  Build from source    | See below  |
| es-1.x        |  Build from source    | [2.6.0-SNAPSHOT](https://github.com/elasticsearch/elasticsearch-transport-memcached/tree/es-1.x/#version-260-snapshot-for-elasticsearch-1x)  |
| es-1.5        |  Build from source    | [2.5.0-SNAPSHOT](https://github.com/elasticsearch/elasticsearch-transport-memcached/tree/es-1.5/#version-250-snapshot-for-elasticsearch-15)  |
|    es-1.4              |     2.4.1         | [2.4.1](https://github.com/elasticsearch/elasticsearch-transport-memcached/tree/v2.4.1/#version-241-for-elasticsearch-14)                  |
| es-1.3        |  2.3.0                | [2.3.0](https://github.com/elasticsearch/elasticsearch-transport-memcached/tree/v2.3.0/#memcached-transport-for-elasticsearch)  |
| es-1.2        |  2.2.0                | [2.2.0](https://github.com/elasticsearch/elasticsearch-transport-memcached/tree/v2.2.0/#memcached-transport-for-elasticsearch)  |
| es-1.0        |  2.0.0                | [2.0.0](https://github.com/elasticsearch/elasticsearch-transport-memcached/tree/v2.0.0/#memcached-transport-for-elasticsearch)  |
| es-0.90       |  1.7.0                | [1.7.0](https://github.com/elasticsearch/elasticsearch-transport-memcached/tree/v1.7.0/#memcached-transport-for-elasticsearch)  |

To build a `SNAPSHOT` version, you need to build it with Maven:

```bash
mvn clean install
plugin --install transport-memcached \
       --url file:target/releases/elasticsearch-transport-memcached-X.X.X-SNAPSHOT.zip
```


## mapping rest to memcached protocol

Memcached commands are mapped to REST and handled by the same generic REST layer in elasticsearch. Here is a list of the 
memcached commands supported:

### get

The memcached `GET` command maps to a REST `GET`. The key used is the URI (with parameters). The main downside is the 
fact that the memcached `GET` does not allow body in the request (and `SET` does not allow to return a result...). 
For this reason, most REST APIs (like search) allow to accept the "source" as a URI parameter as well.

### set

The memcached `SET` command maps to a REST `POST`. The key used is the URI (with parameters), and the body maps to the REST body.

### delete

The memcached `DELETE` command maps to a REST `DELETE`. The key used is the URI (with parameters).

### quit

The memcached `QUIT` command is supported and disconnects the client.

## settings

The following are the settings the can be configured for memcached:


|       Setting      |                        Description                                 |
|--------------------|--------------------------------------------------------------------|
| memcached.port     | A bind port range. Defaults to 11211-11311.                        |

It also uses the common [network settings](http://www.elasticsearch.org/guide/en/elasticsearch/reference/master/modules-network.html).

## disable memcached

The memcached module can be completely disabled and not started using by setting `memcached.enabled` to `false`.
By default it is enabled once it is detected as a plugin.

## Known limitations

Memcached protocol only allow the key length to be under 250. It means that when you send a "big" query using `GET`,
the JSON document passed as a URI parameter it could ends up to be truncated.
So elasticsearch don't get the whole query and generate a `JsonParseException` error.
It's better to use `SET` in that case.

License
-------

    This software is licensed under the Apache 2 license, quoted below.

    Copyright 2009-2014 Elasticsearch <http://www.elasticsearch.org>

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
