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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * Holds {@link User}s and handles automatic freeing of unneeded data.
 * This class is Thread safe.
 */
public class UserPool extends HashMap<String, User> {
    /**
     * The amount of time before a User is considered stale.
     */
    public static final long STALE_MS = 60*1000; // 1 minute

    private final Thread cleaner;
    {
        cleaner = new Thread(() -> {
            while (true) {
                cleanUp();
                try {
                    Thread.sleep(STALE_MS);
                } catch (InterruptedException e) {
                    // Not a problem as the cleanUp can be called arbitrarily without ill effects (besides performance)
                }
            }
        }, "UserPoolCleaner");
        cleaner.setDaemon(true);
        cleaner.start();
    }

    /**
     * Always returns a User: If the User does not already exist, a new one is created, added to the pool and returned.
     * If the user is existing, its internal time based cost queue will be refreshed before returning the user.
     * @param userID the ID can be anything, except null, that is persistent for the user between calls.
     * @return an existing User with the given userID if possible else a new User.
     */
    @Override
    public synchronized User get(Object userID) {
        if (!(userID instanceof String)) {
            throw new IllegalArgumentException(String.format(
                    Locale.ROOT, "get requires a non-null String but got %s",
                    userID == null ? "null" : "a " + userID.getClass().getCanonicalName()));
        }
        final String userIDStr = userID.toString();
        User user = super.get(userIDStr);
        if (user == null) {
            user = new User(userIDStr);
            put(userIDStr, user);
        }
        user.cleanUp();
        return user;
    }

    /**
     * Removes stale users from the pool. A stale user us one that has not made any requests for the last
     * {@link #STALE_MS} milliseconds (1 minute).
     * @return the number of users that were removed.
     */
    public synchronized int cleanUp() {
        final long old = System.currentTimeMillis()-STALE_MS;
        int removed = 0;
        Iterator<Entry<String, User>> users = entrySet().iterator();
        while (users.hasNext()) {
            User user = users.next().getValue();
            if (user.lastUpdated < old) {
                removed++;
                users.remove();
            }
        }
        return removed;
    }
}
