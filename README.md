Currently got it to work basically
## TODOs
- does not yet account for firespread and therefore different woodlight material
- did not yet calculate the correct thresholds and increments yet (need correct constants)
- I didnt bother saving portal positions, so wouldn't light if reload the world (chunk's not a problem)
- potentially errors with unloaded chunks to solve
- didnt figure out dimensions yet

## How It Works
- we maintain a set of "points of interests", here `PotentialPortal`s
  - Their creation does not care if the portal is empty inside, because i wanted to restrict complex searching actions to only creation of obsidian
  - When Obsidian is places, run complicated searching algorithm; empty portals are marked "clear"
  - when block removed, check if "inside" any PotentialPortal, if so check if clear
  - When block placed, check if "inside" any PotentialPortal, if set clear to false
  - When Obsidian removed, check if "crutial to" any PotentialPortal, delete
- On server tick, run through every clear PotentialPortal, enumerate the air
  - if there is burnable block adjacent, then check the amount of lava that may light the portal, increment the PotentialPortal's counter correspondingly
  - if the counter is above a certain threshold, then light the portal

## A little bit of Lag Test
- My CPU: 14700HX(laptop)
- pouring water on lava pool, <2ms total search time (for creating portal)
- replacing an obsidian block one layer underground of a thick obsidian superflat world
  - 18908 PotentialPortals created
  - search time: < 1500ms