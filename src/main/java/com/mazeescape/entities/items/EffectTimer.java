// Owner: Rachid
package com.mazeescape.entities.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Remembers effects that must end after some game time, like the end of a speed boost.
 * The game calls {@link #tick} every turn; when an effect runs out, its end action runs.
 */
public class EffectTimer {

    private final List<Pending> pending = new ArrayList<>();

    /** Creates an empty timer. */
    public EffectTimer() {
    }

    /**
     * Plans an action for later.
     *
     * @param seconds game seconds to wait
     * @param onEnd   what to do when the time is up
     */
    public void schedule(double seconds, Runnable onEnd) {
        pending.add(new Pending(seconds, onEnd));
    }

    /**
     * Lets time pass and runs the actions whose time is up.
     *
     * @param deltaSeconds game seconds that just passed
     */
    public void tick(double deltaSeconds) {
        List<Runnable> due = new ArrayList<>();
        Iterator<Pending> it = pending.iterator();
        while (it.hasNext()) {
            Pending p = it.next();
            p.remaining -= deltaSeconds;
            if (p.remaining <= 0) {
                due.add(p.onEnd);
                it.remove();
            }
        }
        due.forEach(Runnable::run);
    }

    /**
     * Counts the effects still running.
     *
     * @return how many actions are waiting
     */
    public int size() {
        return pending.size();
    }

    private static final class Pending {
        private double remaining;
        private final Runnable onEnd;

        private Pending(double remaining, Runnable onEnd) {
            this.remaining = remaining;
            this.onEnd = onEnd;
        }
    }
}
