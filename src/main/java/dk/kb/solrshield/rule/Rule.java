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
package dk.kb.solrshield.rule;

import dk.kb.solrshield.Argument;
import dk.kb.solrshield.Cost;
import dk.kb.util.YAML;

import java.util.Locale;

/**
 * Base rule for an {@link Argument}. The type of a Rule is the type of a REST argument.
 */
public abstract class Rule<T> {

    private final Cost matchCost;

    public Rule(YAML config) {
        matchCost = new Cost(config);
    }

    /**
     * Calculate the cost for the given value. If the value is not matched by the rule, null is returned.
     * @param value a value of the type accepted by the rule.
     * @return the cost of the value or null if the value is not matched by the rule.
     */
    public Cost calculateCost(T value) {
        if (matches(value)) {
            return matchCost;
        }
        return null;
    }

    /**
     * If this method returns true, the {@link #matchCost} is returned from {@link #calculateCost(Object)};
     * @param value a value of the type accepted by the rule.
     * @return true if the value matches the rule.
     */
    protected abstract boolean matches(T value);

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Rule(matchCost=%s",
                             matchCost);
    }
}
