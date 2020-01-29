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
import org.noggit.JSONUtil;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;

/**
 * The cost of a request or a subset of a request is calculated on a unit-less internal scale.
 * The final cost is made up of base * multiplier, with the cost object keeping track of base and multiplier,
 * so that multiple Costs can be merged.
 * Besides the cost, the Cost object can be marked with {@code stop}, meaning that the request should not be allowed to
 * proceed.
 *
 * This class is immutable: All methods operating on cost state returns a new Cost object.
 */
public class Cost implements Comparable<Cost> {
    public enum ACTION {go, stop}

    // configuration keys
    public static final String ACTION_KEY   = "action";
    public static final String ADD_KEY      = "add";
    public static final String MULTIPLY_KEY = "multiply";
    public static final String MESSAGE_KEY  = "message";

    /**
     * Neutral cost with {@link ACTION#go}, base 0.0, multiplier 1.0 and null as message.
     * Merging with another cost will result in a new cost identical to the other cost.
     */
    public static final Cost NEUTRAL = new Cost(ACTION.go, 0.0, 1.0, null);

    final ACTION action;
    final double base;
    final double multiplier;
    final String message;

    /**
     * YAML-defined cost, using the keys {@code action}, {@code add}, {@code multiply}, {@code message}.
     */
    // TODO: Replace getInteger with getDouble when sb-util has been bumped
    public Cost(YAML config) {
        this(config.containsKey(ACTION_KEY) ? ACTION.valueOf(config.getString(ACTION_KEY)) : ACTION.go,
             config.getInteger(ADD_KEY, 0), config.getInteger(MULTIPLY_KEY, 1),
             config.getString(MESSAGE_KEY));
    }
    /**
     * Plain cost-response with {@link ACTION#go}, multiplier 1.0 and null as message.
     * @param baseCost the base cost for this response.
     */
    public Cost(double baseCost) {
        this(ACTION.go, baseCost, 1.0, null);
    }
    /**
     * Plain cost-response with {@link ACTION#go}, multiplier 1.0 and null as message.
     * @param baseCost the base cost for this response.
     * @param costMultiplier the multiplier for the cost.
     */
    public Cost(double baseCost, double costMultiplier) {
        this(ACTION.go, baseCost, costMultiplier, null);
    }
    /**
     * Response with {@link ACTION#stop} and a message;
     */
    public Cost(String message) {
        this (ACTION.stop, 0.0, 1.0, message);
    }

    /**
     * Full custom response.
     * @param action  the action to take. Can be either go or stop.
     * @param baseCost the base cost for this response.
     * @param costMultiplier the multiplier for the cost.
     * @param message if the action is stop, there should also be a message.
     */
    public Cost(ACTION action, double baseCost, double costMultiplier, String message) {
        this.action = action;
        base = baseCost;
        multiplier = costMultiplier;
        this.message = message;
    }

    public ACTION getAction() {
        return action;
    }

    /**
     * @return the calculates cost {@code base*multiplier}.
     */
    public double getCost() {
        return base*multiplier;
    }

    /**
     * Only defined if {@link #action} is {@code ACTION.stop}.
     * @return the reason for {@code ACTION.stop}.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Merges the given cost to this and return a new CostResponse with the result.
     * If any {@link ACTION} is {@code ACTION.stop} then the resulting ACTION is {@code ACTION.stop}.
     * Messages are concatenated with newline as delimiter.
     * @param other another cost to add with this.
     * @return a new CostResponse created by addition of the cost contents.
     */
    public Cost merge(Cost other) {
        return new Cost(
                action == ACTION.stop || other.action == ACTION.stop ? ACTION.stop : ACTION.go,
                base + other.base,
                multiplier * other.multiplier,
                message == null && other.message == null ? null :
                        message != null && other.message == null ? message :
                                message == null ? other.message :
                                        message + "\n" + other.message
        );

    }

    /**
     * Create a new Cost with action set to stop and the given message. base and multiplier are transferred.
     * @param message the reason for the stop.
     * @return a new cost with the given message.
     */
    public Cost stop(String message) {
        return new Cost(ACTION.stop, base, multiplier, message);
    }

    /**
     * Compares the 2 costResponses and returns the "cheapest" one, determined primarily by {@link #action} (go is
     * preferred over stop) and secondarily by cost (lower is better). This uses {@link #compareTo(Cost)}
     * @param cost1 a cost.
     * @param cost2 a cost.
     * @return the cheapest CostResponse or cost1 if the costs are equal.
     */
    public static Cost getCheapestCost(Cost cost1, Cost cost2) {
        return cost1.compareTo(cost2) <= 0 ? cost1 : cost2;
    }

    @Override
    public int compareTo(Cost other) {
        if (action == ACTION.go && other.action == ACTION.stop) {
            return -1;
        }
        if (action == ACTION.stop && other.action == ACTION.go) {
            return 1;
        }
        return Double.compare(getCost(), other.getCost());
    }

    /**
     * Stream supporting collector that merges incoming Costs.
     */
    public static Collector<Cost, Cost[], Cost> collect() {
        return Collector.of(
                () -> new Cost[1],
                (cummulative, cost) -> cummulative[0] = cummulative[0].merge(cost),
                (cummulative1, cummulative2) -> new Cost[]{cummulative1[0].merge(cummulative2[0])},
                cost -> cost[0]);
    }
}
