# Grass Simulation — Feature Spec

## World
- 50×50 tile grid
- All tiles start empty
- Empty tile = available sunlight = claimable by grass

## Tile Quadrant System
Each tile is divided into 4 quadrants:
- **Top-left** → Grass (reserved for this feature)
- Top-right → (reserved for future herbivores)
- Bottom-left → (reserved for future use)
- Bottom-right → (reserved for future use)

This allows multiple organism types to share a tile visibly.

## Player Configuration (Pre-Simulation Setup)
Before the simulation runs, the player sets:
- `ATTACK_POWER` — photosynthesis rate (how quickly grass gains energy)
- `MOVEMENT_POWER` — travel speed toward target tile

## God Spawn
- At simulation start, one grass organism is spawned by the player
- Spawns at the center of the grid
- Starts with 50% energy (10 out of 20 units)
- Immediately begins seeking the nearest unclaimed tile

## Grass Organism

### Energy
- Capacity: 20 units
- Starting energy: 10 units (50%)
- Energy is gained only when the grass has claimed and is occupying a tile
- Energy is NOT gained while in transit

### Movement
- Each grass that is not yet settled has a target: the nearest unclaimed tile
- It moves toward that tile at a speed determined by `MOVEMENT_POWER`
- Movement is continuous (not instant tile teleportation)
- Once it arrives at the target tile, it claims it and begins photosynthesising

### Photosynthesis
- Only occurs once a tile is claimed and the grass is settled
- `ATTACK_POWER` determines how frequently the grass gains 1 energy unit
- Higher `ATTACK_POWER` = shorter interval between energy gains

### Reproduction
- Triggers when energy reaches 20 (100%)
- Parent remains on its claimed tile
- Offspring spawns adjacent to the parent (just beside it, not on a new tile yet)
- Offspring immediately begins seeking the nearest unclaimed tile
- Both parent and offspring reset to 10 energy (50%) after reproduction

## Rendering
- Each grass is drawn in the **top-left quadrant** of its claimed tile
- While in transit, grass is drawn at its current position moving toward its target
- An **energy bar** is displayed above each grass organism showing current energy out of 20

## Implementation Stack
- Java
- `JPanel` with `paintComponent` for rendering
- Game loop via `Runnable` thread
- `SetupPanel` with sliders for `ATTACK_POWER` and `MOVEMENT_POWER` shown before simulation starts
