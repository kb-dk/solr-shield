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
 * Defines a role for a user. The role holds the allowance for the user.
 * All rules holds a priority 
 */
public class Role {
    private final String roleID;

    double singleCallMaxCost = 0.0;
    double perSecondMaxCost = 0.0;
    int perSecondMaxCalls = 1;
    double perMinuteMaxCost = 0.0;
    int perMinuteMaxCalls = 0;

    /**
     * @param roleID human-readable role designation, e.g. "Admin", "Student at AAU", "Everyone"...
     */
    public Role(String roleID) {
        this.roleID = roleID;
    }

    public String getRoleID() {
        return roleID;
    }
    
}
