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

import java.util.ArrayList;
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
    public CostResponse calculateAndAddCost(
            List<String> endpoints, String userID, List<String> roleIDs, Collection<Map.Entry<String, String>> request) {
        // Calculate cheapest cost
        Collection<Role> roles = getRoles(roleIDs);
        final CostResponse cost = peekCost(endpoints, roleIDs, request);
        if (cost.action == CostResponse.ACTION.stop) { // Hard denial of request
            return cost;
        }

        // Check that cost is within user allowance
        final User user = users.get(userID);
        // The UserPool ensures that the same user will be returned for the same userID.
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        String userResponse = user.checkAndAdd(roles, cost.cost);
        return userResponse != null ?
                new CostResponse(CostResponse.ACTION.stop, cost.cost, userResponse) :  // cost exceeded allowance
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
     * Calculate the cost of the operation and return it. This ignores allowance and only returns
     * {@link CostResponse.ACTION#stop} if the rules explicitly state that the request cannot be served.
     * @param endpointIDs the collections or services to send the request to.
     * @param roleIDs     the roles for the user defines access privileges and allowance. Most lenient role wins.
     * @param request     the request itself.
     * @return the cost of the request.
     * @throws UnsupportedOperationException if a requested endpoint does not exist.
     */
    public CostResponse peekCost(
            List<String> endpointIDs, List<String> roleIDs, Collection<Map.Entry<String, String>> request) {
        return peekCost(endpointIDs, getRoles(roleIDs), request);
    }

    /**
     * Calculate the cost of the operation and return it. This ignores allowance and only returns
     * {@link CostResponse.ACTION#stop} if the rules explicitly state that the request cannot be served.
     * @param endpointIDs the collections or services to send the request to.
     * @param roles       the roles for the user defines access privileges and allowance. Most lenient role wins.
     * @param request     the request itself.
     * @return the cost of the request.
     * @throws UnsupportedOperationException if a requested endpoint does not exist.
     */
    public CostResponse peekCost(
            List<String> endpointIDs, Collection<Role> roles, Collection<Map.Entry<String, String>> request) {
        CostResponse cheapest = new CostResponse(
                CostResponse.ACTION.stop, Double.MAX_VALUE,
                "Internal error: Default cheapest cost should always be replaced");
        for (Role role: roles) {
            cheapest = CostResponse.getCheapestCost(cheapest, peekCost(endpointIDs, role, request));
        }
        return cheapest;
    }

    /**
     * Calculate the cost of the operation for a specific and return it. This ignores allowance and only returns
     * {@link CostResponse.ACTION#stop} if the rules explicitly state that the request cannot be served.
     * @param endpointIDs the collections or services to send the request to.
     * @param role        a role for the user defines access privileges and allowance. Most lenient role wins.
     * @param request     the request itself.
     * @return the cost of the request.
     * @throws UnsupportedOperationException if a requested endpoint does not exist.
     */
    public CostResponse peekCost(
            List<String> endpointIDs, Role role, Collection<Map.Entry<String, String>> request) {

        CostResponse response = new CostResponse();
        for (String endpointDesignation: endpointIDs) {
            Endpoint endpoint = endpoints.get(endpointDesignation);
            if (endpoint == null) {
                throw new IllegalArgumentException("The endpoint '" + endpointDesignation + "' does not exist");
            }
            response = response.add(endpoint.calculateCost(role, request));
        }
        return response;
    }
}
