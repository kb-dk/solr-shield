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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The main entry for Solr Shield. Keeps track of cost calculators, user allowances and statistics.
 */
public class Shield {
    private Map<String, Role> roles;
    private Map<String, Endpoint> endpoints;

    /**
     * Calculate the cost of the operation and add it to the user's account before returning it.
     * @param endpoints the collections or services to send the request to.
     * @param userID  ID of a user. Can be anything that is stable between requests from the same user.
     * @param role    the role of the user defines access privileges and allowance.
     * @param request the request itself.
     * @return the cost of the request.
     */
    public CostResponse calculateAndAddCost(
            List<String> endpoints, String userID, String role, Collection<Map.Entry<String, String>> request) {
        final CostResponse cost = peekCost(endpoints, role, request);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Calculate the cost of the operation and return it. This ignores allowance and only returns
     * {@link CostResponse.ACTION#stop} if the rules explicitly state that the request cannot be served.
     * @param endpointID the collections or services to send the request to.
     * @param roleID     the role of the user defines access privileges and allowance.
     * @param request    the request itself.
     * @return the cost of the request.
     * @throws UnsupportedOperationException if a requested endpoint does not exist.
     */
    public CostResponse peekCost(
            List<String> endpointID, String roleID, Collection<Map.Entry<String, String>> request) {
        double cost = 0.0;
        for (String endpointDesignation: endpointID) {
            Endpoint endpoint = endpoints.get(endpointDesignation);
            if (endpoint == null) {
                throw new IllegalArgumentException("The endpoint '" + endpointDesignation + "' does not exist");
            }
            CostResponse response = endpoint.calculateCost(role, )
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Role getRole(String roleID) {
        throw new UnsupportedOperationException("Not implemented yet");
        // TODO: Support multiple roles by selecting the one with highest priority
    }

}
