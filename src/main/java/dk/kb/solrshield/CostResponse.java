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

/**
 * A response to a Solr Shield request. States whether the call can be issued, the current state of the user
 * and potential extra data for debug etc.
 */
public class CostResponse implements Comparable<CostResponse> {
    public enum ACTION {go, stop}

    final ACTION action;
    final double cost;
    final String message;

    public CostResponse(ACTION action, double cost, String message) {
        this.action = action;
        this.cost = cost;
        this.message = message;
    }

    public ACTION getAction() {
        return action;
    }

    /**
     * Only defined if {@link #action} is {@code ACTION.stop}.
     * @return the reason for {@code ACTION.stop}.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Ass the given cost to this and return a new CostResponse with the result.
     * If any {@link ACTION} is {@code ACTION.stop} then the resulting ACTION is {@code ACTION.stop}.
     * Messages are concatenated with newline as delimiter.
     * @param other another cost to add with this.
     * @return a new CostResponse created by addition of the cost contents.
     */
    public CostResponse add(CostResponse other) {
        return new CostResponse(
                action == ACTION.stop || other.action == ACTION.stop ? ACTION.stop : ACTION.go,
                cost + other.cost,
                message == null && other.message == null ? null :
                        message != null && other.message == null ? message :
                                message == null ? other.message :
                                        message + "\n" + other.message
        );

    }

    /**
     * Compares the 2 costResponses and returns the "cheapest" one, determined primarily by {@link #action} (go is
     * preferred over stop) and secondarily by cost (lower is better). This uses {@link #compareTo(CostResponse)}
     * @param cost1 a cost.
     * @param cost2 a cost.
     * @return the cheapest CostResponse or cost1 if the costs are equal.
     */
    public static CostResponse getCheapestCost(CostResponse cost1, CostResponse cost2) {
        return cost1.compareTo(cost2) <= 0 ? cost1 : cost2;
    }

    @Override
    public int compareTo(CostResponse other) {
        if (action == ACTION.go && other.action == ACTION.stop) {
            return -1;
        }
        if (action == ACTION.stop && other.action == ACTION.go) {
            return 1;
        }
        return Double.compare(cost, other.cost);
    }
}
