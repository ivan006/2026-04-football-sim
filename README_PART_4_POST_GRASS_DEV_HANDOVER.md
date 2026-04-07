**IVANS_HANDOVER.md — Codebase Overview**

_Assumes you've read the README and GRASS_GROW_SPEC.md._

---

**Where we're at**

Grass is fully working. Herbivores and Carnivores exist as placeholders in the UI but have no behaviour yet. The system is built so adding them later follows the same pattern as Grass.

---

**FPSJFrame.java**

The front door of the application. Starts everything up and decides which screen to show at any given moment — the home screen, the create world screen, or the simulation itself.

---

**World.java**

Represents a single running simulation. Responsible for keeping the simulation alive, tracking time, and holding all the data that belongs to that simulation. Can be paused and resumed. Multiple worlds can exist at once.

---

**WorldManager.java**

Keeps a list of all worlds currently in the session. Handles creating new worlds and deleting old ones.

---

**Grass.java**

The Grass organism. Responsible for its own survival — finding tiles, growing, reproducing, and dying. Each blade of grass acts independently.

---

**GrassRenderer.java**

Responsible purely for drawing a grass blade on screen. Knows nothing about grass behaviour — it just handles the visuals.

---

**Tile.java**

Represents a single cell on the grid. Tracks who has claimed it.

---

**Hud.java**

Sits between the simulation and the graph display. Feeds live population data into the graph and controls whether the graph is visible.

---

**GraphModal.java**

The population graph overlay. Displays a live chart of organism populations over time. Supports pagination to scroll back through history.

---

**SimPanel.java**

The simulation viewer screen. Shows the grid, the organisms, and the top bar with controls. Does not run the simulation — it just displays whatever world it is given.

---

**WorldSelectScreen.java**

The home screen. Lists all active worlds with their current status, configuration, and population. Lets you enter, pause, or delete a world from one place.

---

**CreateWorldPanel.java**

The new world form. Lets you name a world and configure the strength of each organism type before the simulation starts.
