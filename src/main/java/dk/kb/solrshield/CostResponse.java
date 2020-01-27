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
public class CostResponse {
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

    public String getMessage() {
        return message;
    }
}
