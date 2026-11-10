```mermaid
classDiagram
    %% Core Architecture
    class GameObject {
        <<abstract>>
        #Position position
        +getPosition()
        +setPosition()
        +update(Game, double)
    }
    
    class Game {
        -Maze maze
        -Player player
        -List~Enemy~ enemies
        -List~Item~ items
        +update(double)
        +updateRealTime(double)
        +movePlayer(Direction)
    }

    %% Entities
    class Player {
        -double speed
        -double shieldSeconds
        +tickMovement(Game, double)
        +tickShield(double)
        +grantShield(double)
    }

    class Enemy {
        <<abstract>>
        #double detectionRadius
        +update(Game, double)*
        #react(Game, double)*
        #idle(Game, double)
    }
    
    class Chaser {
        -EnemyBehavior behavior
        +onPlayerContact(Player)
    }
    class Bomber {
        +onPlayerContact(Player)
    }
    class Leech {
        +onPlayerContact(Player)
        +followPlayer(Player)
    }

    class Item {
        <<abstract>>
        +onPickup(Player, Game)*
        +symbol() char
    }
    class Key
    class Shield
    class Taser {
        +use(Player, Game) boolean
    }
    
    class Usable {
        <<interface>>
        +use(Player, Game) boolean
    }

    class EnemyBehavior {
        <<interface>>
        +move(Enemy, Game, double)
    }
    class ChaseBehavior {
        -Pathfinder pathfinder
        +move(Enemy, Game, double)
    }

    %% Maze & Tiles
    class Tile {
        <<abstract>>
        +isBlocking() boolean
        +onEnter(Player)
        +completesLevel() boolean
    }
    class Wall
    class Floor
    class Door
    class Exit

    %% Relationships
    Game *-- Player
    Game *-- Enemy
    Game *-- Item
    Game *-- Maze

    GameObject <|-- Player
    GameObject <|-- Enemy
    GameObject <|-- Item

    Enemy <|-- Chaser
    Enemy <|-- Bomber
    Enemy <|-- Leech
    Enemy o-- EnemyBehavior

    Item <|-- Key
    Item <|-- Shield
    Item <|-- Taser
    Usable <|.. Taser

    EnemyBehavior <|.. ChaseBehavior

    Maze *-- Tile
    Tile <|-- Wall
    Tile <|-- Floor
    Tile <|-- Door
    Tile <|-- Exit

    %% Patterns Note
    note for Enemy "Template Method Pattern:\nupdate() is final and calls react() or idle()"
    note for EnemyBehavior "Strategy Pattern:\nChaser uses ChaseBehavior\nCan be swapped for PatrolBehavior"