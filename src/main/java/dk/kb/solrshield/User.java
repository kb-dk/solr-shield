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

import java.util.ArrayDeque;

/**
 * Memory-persistent representation of a user, holding allowance and accumulated cost.
 * This class is Thread safe.
 */
public class User {
    final String userID;
    double totalCost = 0.0;
    long totalCalls = 0;
    long lastUpdated = System.currentTimeMillis();
    CostQueue secondQueue = new CostQueue("per second", 1000);
    CostQueue minuteQueue = new CostQueue("per minute", 60*1000);

    public User(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public synchronized double getTotalCost() {
        return totalCost;
    }
    public synchronized long getTotalCalls() {
        return totalCalls;
    }

    /**
     * Check that the user's accumulated cost and the user's allowance, as defined by the role, allows for the
     * given new cost. If so, the cost is added and null is returned, else the cost is not added and a description of
     * why the cost was too high is returned.
     * @param role the Role under which the request is to be made.
     * @param cost the cost of the operation that the user want to perform.
     * @return null if the user is allowed to perform the request, else an explanation as to why not.
     */
    public synchronized String checkAndAdd(Role role, double cost) {
        if (cost > role.singleCallMaxCost) {
            return String.format("The cost of the single query %.0f exceeds max cost allowance %.0f from role %s",
                                 cost, role.singleCallMaxCost, role.getRoleID());
        }
        String answer;
        if ((answer = checkQueue(secondQueue, role.perSecondMaxCost, role.perSecondMaxCalls, cost)) != null) {
            return answer;
        }
        if ((answer = checkQueue(minuteQueue, role.perMinuteMaxCost, role.perMinuteMaxCalls, cost)) != null) {
            return answer;
        }

        lastUpdated = System.currentTimeMillis();
        totalCost += cost;
        totalCalls++;
        TimeCostEntry timeCost = new TimeCostEntry(cost);
        secondQueue.add(timeCost);
        minuteQueue.add(timeCost);
        return null;
    }

    /**
     * @return the timestamp (epoch = milliseconds since 1970-01-01) for when the User's stats were last updated.
     */
    public long lastUpdated() {
        return lastUpdated;
    }

    private String checkQueue(CostQueue queue, double maxCost, int maxCalls, double cost) {
        queue.removeOld();
        if (queue.currentCost+cost > maxCalls) {
            return String.format("Accumulated cost %.0f plus new cost %.0f exceeds %s allowance of %.0f",
                                 queue.getCurrentCost(), cost, queue.designation, maxCost);
        }
        if (queue.getCurrentCalls()+1 > maxCalls) {
            return String.format("Accumulated #calls %d plus 1 new call exceeds %s allowance of %d calls",
                                 queue.getCurrentCalls(), queue.designation, maxCalls);
        }
        return null;
    }

    // Note: This class is private as it only overrides the methods used by {@link User}.
    // Note 2: this class it not thread safe: Synchronization must be handled by the caller.
    private static class CostQueue extends ArrayDeque<TimeCostEntry> {
        private final String designation;
        private final long maxAgeMS;
        private double currentCost = 0.0;

        public CostQueue(String designation, long maxAgeMS) {
            this.designation = designation;
            this.maxAgeMS = maxAgeMS;
        }

        /**
         * Returns the sum of the entry-costs for the elements in the queue in O(1).
         * Note: Call {@link #removeOld()} to ensure the queue is up to date.
         * @return the current sum of entry-costs in the queue.
         */
        public double getCurrentCost() {
            return currentCost;
        }

        /**
         * Returns the number of entries in the queue in O(1). This is an alias for {@link #size()}.
         * Note: Call {@link #removeOld()} to ensure the queue is up to date.
         * @return the current number of entries in the queue.
         */
        public int getCurrentCalls() {
            return size();
        }

        public String getDesignation() {
            return designation;
        }

        /**
         * Removed all entries that are older than now-maxAgeMS.
         * @return true if one or more elements were removed.
         */
        public boolean removeOld() {
            final long old = System.currentTimeMillis()-maxAgeMS;
            boolean removedSomething = false;
            while (!isEmpty() && peek().epoch < old) {
                removeFirst();
                removedSomething = true;
            }
            return removedSomething;
        }

        @Override
        public boolean add(TimeCostEntry entry) {
            currentCost += entry.cost;
            return super.add(entry);
        }

        @Override
        public TimeCostEntry remove() {
            if (!isEmpty()) {
                currentCost -= peekLast().cost;
            }
            return super.remove();
        }

        @Override
        public TimeCostEntry removeLast() {
            if (!isEmpty()) {
                currentCost -= peekLast().cost;
            }
            return super.removeLast();
        }

        @Override
        public TimeCostEntry removeFirst() {
            if (!isEmpty()) {
                currentCost -= peekFirst().cost;
            }
            return super.removeFirst();
        }

        @Override
        public void clear() {
            currentCost = 0.0;
            super.clear();
        }
    }

    private static class TimeCostEntry {
        final long epoch;
        final double cost;

        private TimeCostEntry(double cost) {
            this.epoch = System.currentTimeMillis();
            this.cost = cost;
        }
    }
}
