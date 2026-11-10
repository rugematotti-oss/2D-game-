// Owner: Totti
package com.mazeescape.entities.behaviors;

import com.mazeescape.core.Game;
import com.mazeescape.entities.enemies.Enemy;

/** Strategy: how an enemy moves. Swappable at runtime. */
public interface EnemyBehavior {

    void move(Enemy enemy, Game game, double deltaSeconds);
}
