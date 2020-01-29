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

/**
 * Base rule for an {@link Argument}. The type of a Rule is the type of a REST argument.
 */
public class IntegerRule extends Rule<Integer> {

    public IntegerRule(YAML config) {
        super(config);
    }

    @Override
    protected boolean matches(Integer value) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
