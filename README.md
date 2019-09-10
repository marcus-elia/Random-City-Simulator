# Random-City-Simulator
The generates a random map of roads to form a city.  Cars randomly spawn in and move around the city.  The user can move around the
city by holding the arrow keys.

Roads
===========================================================================================================================================
The city starts out with a single road. Every road is a straight line with an intersection at both sides. Every few seconds, the game
creates a new random road from an existing intersection.  This new road will end at existing intersection if there is one nearby, or it
will dead end at a new intersection.  A road cannot split an existing road.

Vehicles
===========================================================================================================================================
The vehicles (which are currently drawn as triangles) will spawn in whenever there are fewer than the ratio requires. A vehicle drives on
the right side of the road until it reaches an intersection. It will briefly stop at the start of the intersection, and then proceed to
drive through the intersection onto a random road exiting the intersection.  If a vehicle chooses a road which is dead end, it will stop
halfway down the road. If a new road is generated from the ending intersection, the vehicle will proceed. Otherwise, after a determined
amount of ticks pass, it will despawn.  Vehicle collision is not currently implemented.
