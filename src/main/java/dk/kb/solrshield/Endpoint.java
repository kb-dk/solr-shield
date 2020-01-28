/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dk.kb.solrshield;

import dk.kb.util.YAML;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents a resource, such as a Solr collection, an image server or similar.
 * Each resource has a collection of rules, grouped under URL parameters, for validation of requests and
 * calculation of cost.
 */
public class Endpoint {
    private final String id;
    private Map<String, Argument> arguments;

    public Endpoint(String id, YAML config) {
        this.id = id;
        throw new UnsupportedOperationException("Not implemented yet. Parse from " + config);
    }

    public CostResponse calculateCost(Role role, Collection<Map.Entry<String, String>> request) {
        CostResponse cost = new CostResponse();
        for (Map.Entry<String, String> entry: request) {
            Argument argument = arguments.get(entry.getKey());
            if (argument == null) {
                return new CostResponse("Error: The request key '" + entry.getKey() + "' is not on the white list");
            }
            
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
