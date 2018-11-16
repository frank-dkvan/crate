#
# Licensed to Crate.io Inc. (Crate) under one or more contributor license
# agreements.  See the NOTICE file distributed with this work for additional
# information regarding copyright ownership.  Crate licenses this file to
# you under the Apache License, Version 2.0 (the "License");  you may not
# use this file except in compliance with the License.  You may obtain a
# copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations
# under the License.
#
# However, to use any modules in this file marked as "Enterprise Features",
# Crate must have given you permission to enable and use such Enterprise
# Features and you must have a valid Enterprise or Subscription Agreement
# with Crate.  If you enable or use the Enterprise Features, you represent
# and warrant that you have a valid Enterprise or Subscription Agreement
# with Crate.  Your use of the Enterprise Features if governed by the terms
# and conditions of your Enterprise or Subscription Agreement with Crate.

import os
import pathlib
from testutils.paths import crate_path, project_root
from dnslib.server import DNSServer
from dnslib.zoneresolver import ZoneResolver
from crate.testing.layer import CrateLayer
from crate.client import connect
from testutils.ports import GLOBAL_PORT_POOL

CRATE_HTTP_PORT = GLOBAL_PORT_POOL.get()
CRATE_TRANSPORT_PORT = 44300
DNS_PORT = 50053

zone_file = pathlib.Path(os.path.abspath(os.path.join(
    project_root, 'blackbox', 'dns_discovery', 'crate.internal.zone')))

dns_server = DNSServer(ZoneResolver(open(zone_file)), port=DNS_PORT)


def main():
    num_nodes = 3
    crate_layers = []

    dns_server.start_thread()

    for i in range(0, num_nodes):
        crate_layer = CrateLayer(
            'node-' + str(i),
            cluster_name='crate-dns-discovery',
            crate_home=crate_path(),
            port=CRATE_HTTP_PORT if i == 0 else GLOBAL_PORT_POOL.get(),
            transport_port=CRATE_TRANSPORT_PORT + i,
            settings={
                'psql.port': GLOBAL_PORT_POOL.get(),
                "discovery.zen.hosts_provider": "srv",
                "discovery.srv.query": "_test._srv.crate.internal.",
                "discovery.srv.resolver": "127.0.0.1:" + str(DNS_PORT)
            }
        )
        crate_layers.append(crate_layer)
        crate_layer.start()

    try:
        conn = connect('localhost:{}'.format(CRATE_HTTP_PORT))
        c = conn.cursor()
        c.execute('''select count() from sys.nodes''')
        result = c.fetchone()
        if result[0] != num_nodes:
            raise AssertionError("Nodes could not join, expected number of nodes: " + str(num_nodes) + ", found: " + str(result[0]))

    finally:
        for crate_layer in crate_layers:
            crate_layer.stop()
        dns_server.stop()


if __name__ == "__main__":
    main()
