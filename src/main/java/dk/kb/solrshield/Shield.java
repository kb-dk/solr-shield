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
import java.util.stream.Collectors;

/**
 * The main entry for Solr Shield. Keeps track of cost calculators, user allowances and statistics.
 */
public class Shield {
    private Map<String, Role> roles;
    private Map<String, Endpoint> endpoints;
    private final UserPool users = new UserPool();

    /**
     * Calculate the cost of the operation and add it to the user's account before returning it.
     * @param endpoints the collections or services to send the request to.
     * @param userID  ID of a user. Can be anything that is stable between requests from the same user.
     * @param roleIDs the roles for the user defines access privileges and allowance. Most lenient role wins.
     * @param request the request itself.
     * @return the cost of the request.
     */
    public Cost calculateAndAddCost(
            List<String> endpoints, String userID, List<String> roleIDs, Collection<Map.Entry<String, String>> request) {
        // Calculate cheapest cost
        Collection<Role> roles = getRoles(roleIDs);
        final Cost cost = peekCost(endpoints, request);
        if (cost.action == Cost.ACTION.stop) { // Hard denial of request
            return cost;
        }

        // Check that cost is within user allowance
        final User user = users.get(userID);
        // The UserPool ensures that the same user will be returned for the same userID.
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        String userResponse = user.checkAndAdd(roles, cost.getCost());
        return userResponse != null ?
                cost.stop(userResponse) :  // cost exceeded allowance
                cost; // All OK
    }

    public Collection<Role> getRoles(List<String> roleIDs) {
        if (roleIDs.isEmpty()) {
            throw new IllegalArgumentException("Error: No roleIDs defined");
        }
        return roleIDs.stream().map(roleID -> {
            Role role = roles.get(roleID);
            if (role == null) {
                throw new IllegalArgumentException("Unknown role '" + roleID + "'");
            }
            return role;
        }).collect(Collectors.toList());
    }

    /**
     * Calculate the cost of the operation for a specific and return it. This ignores allowance and only returns
     * {@link Cost.ACTION#stop} if the rules explicitly state that the request cannot be served.
     * @param endpointIDs the collections or services to send the request to.
     * @param request     the request itself.
     * @return the cost of the request.
     * @throws UnsupportedOperationException if a requested endpoint does not exist.
     */
    public Cost peekCost(List<String> endpointIDs, Collection<Map.Entry<String, String>> request) {

        Cost response = Cost.NEUTRAL;
        for (String endpointDesignation: endpointIDs) {
            Endpoint endpoint = endpoints.get(endpointDesignation);
            if (endpoint == null) {
                throw new IllegalArgumentException("The endpoint '" + endpointDesignation + "' does not exist");
            }
            response = response.merge(endpoint.calculateCost(request));
        }
        return response;
    }
}
