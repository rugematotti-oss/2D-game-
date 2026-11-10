# Game Design Document: Maze Escape, The Forgotten Labyrinth

Authors: Totti (engine, player, enemies, rendering) and Rachid (maze, items, levels, save, HUD, docs).
Status: final for version 1. Any change to the rules is written here first.

## 1. Concept

You wake up in a labyrinth that rebuilds itself every time someone enters it.
Somewhere in its corridors lies the key that opens the door to the way out.
Three kinds of creatures roam the maze, and one blow from them is enough to kill you.
Find the key, open the door, reach the exit, three levels in a row.

- Genre: top-down maze exploration with stealth and survival.
- View: 2D, seen from above, the whole maze on screen.
- One player, keyboard only, real time.

## 2. Core loop

Main menu -> START GAME -> spawn -> explore -> pick up the key -> avoid or slow the enemies with items ->
open the door -> step on the exit -> next level (saved automatically).

## 3. Time and speed

The game runs in real time, about 60 updates per second. Holding or pressing a direction key
makes the player walk tile by tile at his own speed; enemies move on their own clock.

| Who | Speed |
|---|---|
| Player | 3 tiles/s |
| Player slowed by a Leech | 1.5 tiles/s |
| Player with a Speed Boost | 4.5 tiles/s |
| Chaser | 2 tiles/s: slower than the player, faster than a slowed player |

Every timer (Leech 10 s, Chaser escape 15 s, Speed Boost 5 s, Time Stop 5 s, Shield 8 s) counts
real seconds, so standing still does not pause them.

## 4. Win and lose

| | Condition |
|---|---|
| Win a level | Pick up the key, walk into the door (it opens), step on the exit. |
| Win the game | Finish level 3. The save is deleted, "Play again" starts a new game. |
| Lose | An enemy blow (Chaser catch, Bomber contact) while the player has no shield. |
| After a death | "Restart Level": same maze, same enemies, same items, the key moves elsewhere. |

There is no health bar: one blow kills.

## 5. Controls

| Key | Action |
|---|---|
| Z Q S D or arrow keys | Walk tile by tile |
| T | Use a Taser (only works while a Leech is attached; otherwise it is kept) |
| F | Use a Time Stop |

Using an item is instant.

## 6. The maze

- Built from a seed: the same seed always gives the same maze. This is what makes restart and save possible.
- Algorithm: recursive backtracker (a random walk that backs up when stuck), then about 10% of the walls
  between two corridors are knocked down to create loops, so a dead end is not a death trap.
- Size: 13x13, then 15x15, then 17x17 tiles.
- The exit is in the dead end farthest from the start. The door is the only tile leading to it,
  so the exit cannot be reached without the key.

| Tile | Looks like | Player | Enemies and sight |
|---|---|---|---|
| Wall | light blue line between tiles | blocked | blocked |
| Floor | dark blue tile | walks | pass |
| Door, closed | wooden door | opens it if he holds a key | blocked |
| Door, open | open doorway | walks | pass |
| Exit | green portal | completes the level | pass |

## 7. Placement

All distances are counted in steps along the corridors (breadth-first search from the start).

- Enemies: at least 8 steps from the start. Level 1: one Chaser, one Bomber, one Leech.
  Level 2 adds a Bomber, level 3 adds a Chaser.
- Items: at least 3 steps from the start, one of each per level (Speed Boost, Taser, Shield, Time Stop).
- No enemy is ever placed on the shortest route from the start to the exit, so a Bomber (which
  never moves) can never wall the player in.
- Key: on a free tile in the farthest half of the maze that can be reached without walking on an
  enemy. On a restart it goes to another such tile.
- Nothing is ever placed on top of something else.
- Every level is therefore winnable; a test checks it on 900 generated levels.

## 8. Enemies

Every enemy has a circular detection zone of 6 tiles, blocked by walls and closed doors (range plus line of sight).
Detection is written once in `Enemy`; each enemy only says how it reacts (Template Method).

| Enemy | Sprite | Behaviour | Danger |
|---|---|---|---|
| Chaser | red | Once it sees the player, follows the shortest path (BFS) at 2 tiles/s. Gives up after 15 s out of sight. | Kills when it reaches the player |
| Bomber | orange | Waits on its tile. | Explodes when the player walks onto it |
| Leech | green | Waits on its tile. | Sticks to the player when he walks onto it: speed halved for 10 s, or until a Taser is used. No damage |

## 9. Items

Each item has its own icon: a gold key, a lightning bolt, a shield, a taser and a stopwatch.

| Item | Letter | When picked up | Use |
|---|---|---|---|
| Key | K | Goes into the inventory | Opens the door |
| Speed Boost | S | Speed x1.5 for 5 s | none |
| Shield | H | Invincible for 8 s (a blue aura shows it) | none |
| Taser | T | Stored | T: detaches the Leech. With no Leech, nothing happens and it is kept |
| Time Stop | Z | Stored | F: enemies neither move nor hurt for 5 s |

## 10. The player character

An original stickman: black stick body with a light outline, round white head with two dot eyes,
a red scarf and white sneakers.

- Standing still, he is alive: he breathes (the body rises and falls), his arms sway, he blinks every
  few seconds and his scarf flutters.
- Walking, his legs and arms swing in opposite directions, he bobs, faces his direction and the
  scarf flies behind him. Facing up, only the back of his head shows.
- The pose is computed by `StickmanPose` (pure maths, unit-tested) and drawn by `StickmanRenderer`.

## 11. HUD and screens

Always visible at the top left:
`Level 1/3 | Key: no | Taser [T]: 0 | Time Stop [F]: 0`, plus `SLOWED BY LEECH`, `SHIELD` and
`ENEMIES FROZEN` when active (red background when slowed, blue when shielded).

Screens: main menu (START GAME; SETTINGS and LEVELS are placeholders for now), Game Over with
"Restart Level", victory with "Play again".

## 12. Save

- Automatic when a level starts.
- Two numbers: the level reached and its maze seed, in `saves/save.properties` (ignored by Git).
- On launch, the game continues the saved level. A missing, edited or broken file never crashes the game:
  it is reported in the console and a new game starts.

## 13. Scope

In version 1: seeded random maze, key and door, 3 levels, 3 enemy kinds, 5 item kinds, HUD, save, restart.
Out: health, stamina, mini-map, several exits, fog of war, sound.

## 14. OOP principles in the code

| Principle | Example |
|---|---|
| Encapsulation | `Player` hides its inventory and shield behind `use`, `count`, `grantShield`; `Maze` hides its grid |
| Inheritance | `Wall`, `Floor`, `Door`, `Exit` extend `Tile`; the 5 items extend `Item`; the 3 enemies extend `Enemy`; all of them extend `GameObject` |
| Polymorphism | `Game.movePlayer` calls `tile.beforeEnter`, `tile.onEnter`, `tile.completesLevel` and `item.onPickup` without knowing the concrete class |
| Abstraction | `GameObject`, `Tile`, `Item`, `Enemy` are abstract; `Usable` and `EnemyBehavior` are interfaces |

## 15. Design patterns

| Pattern | Where |
|---|---|
| Template Method | `Enemy.update` (detect, then `react` or `idle`) |
| Strategy | `EnemyBehavior` with `ChaseBehavior` and `PatrolBehavior` |
| Polymorphic tiles and items | `Tile` and `Item` hierarchies: no `if (tile is a door)` in the game loop |
| Separation of logic and drawing | `StickmanPose` computes, `StickmanRenderer` draws; `HudText` builds the text, `JavaFxRenderer` shows it |

## 16. Technical

Java 17, Maven, JUnit 5, JavaFX, Docker for the tests. Game logic has no JavaFX code, so it runs headless
(console version and tests). 103 unit tests; coverage report with JaCoCo (`mvn test`, then
`target/site/jacoco/index.html`).
