// Owner: Rachid
package com.mazeescape.ui;

import static org.junit.jupiter.api.Assertions.*;

import com.mazeescape.core.Game;
import com.mazeescape.core.Position;
import com.mazeescape.entities.Player;
import com.mazeescape.entities.enemies.Leech;
import com.mazeescape.entities.items.Taser;
import org.junit.jupiter.api.Test;

class HudTextTest {

    private Game game() {
        Game game = new Game();
        game.setPlayer(new Player(new Position(0, 0), 4));
        return game;
    }

    @Test
    void calmHud() {
        assertEquals("Level 1/3 | Key: no | Taser [T]: 0 | Time Stop [F]: 0", HudText.of(game()));
    }

    @Test
    void hudShowsKeyItemsShieldAndFreeze() {
        Game game = game();
        game.setLevel(2); // <--- Added missing semicolon here
        Player p = game.getPlayer();
        p.addKey();
        p.store(new Taser(new Position(0, 0)));
        p.grantShield(8.0);
        game.freezeEnemies(5);
        String hud = HudText.of(game);
        
        assertTrue(hud.startsWith("Level 2/3 | Key: yes | Taser [T]: 1"));
        assertTrue(hud.contains("SHIELD"));
        assertTrue(hud.contains("ENEMIES FROZEN"));
    }

    @Test
    void hudWarnsWhenALeechIsAttached() {
        Game game = game();
        Leech leech = new Leech(new Position(1, 0), 5);
        game.getEnemies().add(leech);
        assertFalse(HudText.isSlowed(game));
        
        leech.onPlayerContact(game.getPlayer());
        
        assertTrue(HudText.isSlowed(game));
        assertTrue(HudText.of(game).contains("SLOWED BY LEECH"));
    }
}