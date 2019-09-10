package prequel;

import java.awt.Graphics2D;
import java.util.ArrayList;

public class RoadMap extends GameObject
{
    private ArrayList<Road> roads;
    private ArrayList<Intersection> intersections;
    private ArrayList<Vehicle> vehicles;
    
    // how long the roads are on average
    private double averageRoadLength;
    
    // how close together intersections can be
    private double minIntersectionDistance;
    
    // how close should we lock onto another intersection from
    private double intersectionLockOnDistance;
    
    // this keeps track of when to build a new road
    private int ticksSinceLastRoadCreation;
    
    // the probability that a road is perpendicular to an axis
    private double perpendicularity;
    
    private int maxNumRoads;
    private double carsPerRoad;
    
    // how many ticks in between each road construction attempt
    private int ticksPerRoad;
    
	public RoadMap(int x, int y, ID id, double averageRoadLength, double minIntersectionDistance,
			double perpendicularity, int maxNumRoads, double carsPerRoad, 
			double intersectionLockOnDistance, int ticksPerRoad) 
	{
		super(x, y, id);
		roads = new ArrayList<Road>();
		intersections = new ArrayList<Intersection>();
		vehicles = new ArrayList<Vehicle>();
		ticksSinceLastRoadCreation = 0;
		this.averageRoadLength = averageRoadLength;
		this.minIntersectionDistance = minIntersectionDistance;
		this.perpendicularity = perpendicularity;
		this.maxNumRoads = maxNumRoads;
		this.carsPerRoad = carsPerRoad;
		this.intersectionLockOnDistance = intersectionLockOnDistance;
		this.ticksPerRoad = ticksPerRoad;
	}

	@Override
	public void tick() 
	{
		for(int i = 0; i < roads.size(); i++)
		{
			roads.get(i).tick();
		}
		for(int i = 0; i < vehicles.size(); i++)
		{
			vehicles.get(i).tick();
		}
		
		ticksSinceLastRoadCreation++;
		
		// if it's time to create a road
		if(ticksSinceLastRoadCreation == this.ticksPerRoad)
		{
			// debug
			//this.testIntersectionDistances();
			//this.testRoadDistances();
			
			// as long as we don't have too many roads, make a new one
			if(roads.size() < this.maxNumRoads)
			{
			    ticksSinceLastRoadCreation = 0;
			    this.createNewRoad(5);
			}
			
			// if we don't have too many vehicles, make a new one
			if(vehicles.size() < roads.size()*this.carsPerRoad)// && vehicles.size() < 1)
			{
				this.createNewVehicle();
			}
			
		}
	}

	@Override
	public void render(Graphics2D g) 
	{
		for(int i = 0; i < roads.size(); i++)
		{
			roads.get(i).render(g);
		}		
		for(Intersection intsec : intersections)
		{
			intsec.render(g);
		}
		for(int i = 0; i < vehicles.size(); i++)
		{
			vehicles.get(i).render(g);
		}
	}

	public void addRoad(Road r)
	{
		// add the new road to the road list
		roads.add(r);
		r.getStartInt().addRoadStart(r);
		r.getEndInt().addRoadEnd(r);
	}
	
	// getter
	public ArrayList<Road> getRoads()
	{
		return this.roads;
	}
	
	public ArrayList<Vehicle> getVehicles()
	{
		return this.vehicles;
	}
	
	public Road getRandomRoad()
	{
		int r = (int) Math.floor(Math.random() * roads.size());
		return roads.get(r);
	}
	
	// If this Road Map already has an intersection with the given coordinates,
	// this returns the intersection. Otherwise it returns null.
	public Intersection hasIntersection(int x, int y)
	{
		for(Intersection intsec : intersections)
		{
			if(intsec.sameCoordinates(x, y))
			{
				return intsec;
			}
		}
		return null;
	}
	
	// when we are adding a new road, do we need to make new intersections? If so, we do.
	// In either case, both intersections are returned in an array
	public Intersection[] addIntersectionsIfNeeded(int x1, int y1, int x2, int y2)
	{
		Intersection startInt = this.hasIntersection(x1, y1);
		if(startInt == null)
		{
			startInt = new Intersection(x1, y1, ID.Intersection, this);
			this.intersections.add(startInt);
		}
		Intersection endInt = this.hasIntersection(x2, y2);
		if(endInt == null)
		{
			endInt = new Intersection(x2, y2, ID.Intersection, this);
			this.intersections.add(endInt);
		}
		return new Intersection[]{startInt, endInt};
	}
	
	public ArrayList<Intersection> getIntersections()
	{
		return this.intersections;
	}
	public double getMinIntersectionDistance()
	{
		return this.minIntersectionDistance;
	}
	
	public double getRandomAngle()
	{
		if(Math.random() < this.perpendicularity)
		{
			return Math.round(Math.random()*4)*Math.PI/2;
		}
		else
		{
			return Math.round(Math.random()*4)*Math.PI/2 + Math.PI/4;
		}
	}
	
	
	// This attempts to create a new random road. It will look at each intersection
	// and make the given number of attempts to construct a road before moving on
	// to the next intersection. Once it creates a road, the method terminates.
	public void createNewRoad(int attemptsPerIntersection)
	{
		int randx, randy;
		double roadLength;
		double roadAngle;
		Intersection connectTo;
		
		// iterate through all of the intersections
		for(Intersection intsec : intersections)
		{
			// keep track of whether we can build a proposed road
			boolean roadApproved;
			
			// if the intersections already has at least 4 roads, move on
			if(intsec.numRoads() < 4)
			{
				for(int i = 0; i < attemptsPerIntersection; i++)
				{
					connectTo = null;
					roadApproved = true;  // assume innocent until proven guilty USA USA
					
					// get random nearby coordinates to see if we can add a road there
					roadLength = (Math.random()*.4 + .8) * this.averageRoadLength;
					roadAngle = this.getRandomAngle();
					randx = (int)Math.round(intsec.getX() + roadLength*Math.cos(roadAngle));
					randy = (int)Math.round(intsec.getY() + roadLength*Math.sin(roadAngle));
					
					// if the new point is very close to an existing intersection, lock onto it
					for(Intersection intsec2 : intersections)
					{
						if(intsec2.distanceToPoint(randx, randy) < intersectionLockOnDistance)
						{
							// if there is not already a road from this intersection to that one
							// proceed
							if(!intsec.getNeighbors().contains(intsec2))
							{
								connectTo = intsec2;
								randx = (int)intsec2.getX();
								randy = (int)intsec2.getY();
								break;
							}
							else  // if there is already a road there, we don't approve
							{
								roadApproved = false;
								break;
							}
						}
					}
									
					// if the proposed road would cross an existing road or end too close to one,
					// cancel
					for(Road curRoad : roads)
					{
						if(curRoad.hitsRoad((int)intsec.getX(), (int)intsec.getY(), randx, randy) ||
								curRoad.directedDistance(randx, randy) < this.minIntersectionDistance)
						{
							// also make sure the road we are too close to isn't just coming out
							// from our target intersection we are connecting to.
							if(connectTo == null)
							{
								roadApproved = false;
								break;
							}
							if(connectTo != null && !connectTo.getRoads().contains(curRoad))
							{
								roadApproved = false;
								break;
							}
						}
					}
				    // if the road passes too close to an existing intersection, cancel
					for(Intersection intsec2 : intersections)
					{
						// if we are close to an intersection that isn't this one
					    if(intsec2.directedDistance((int)intsec.getX(), (int)intsec.getY(), 
					    		randx, randy) < this.minIntersectionDistance 
					    		&& !intsec.equals(intsec2))
					    {
					    	if(connectTo == null)
					    	{
					    		roadApproved = false;
					    		break;
					    	}
					    	// and the intersection isn't the one we are locking to
					    	if(connectTo != null && !connectTo.equals(intsec2))
					    	{
					    	    roadApproved = false;
					    	    break;
					    	}
					    }
					}
					// if nothing went wrong, build the road
					if(roadApproved)
					{
						// Put the coordinates in order
						int x1, y1, x2, y2;
						if((int)intsec.getX() < randx)
						{
							x1 = (int)intsec.getX();
							y1 = (int)intsec.getY();
							x2 = randx;
							y2 = randy;
						}
						else if((int)intsec.getX() > randx)
						{
							x2 = (int)intsec.getX();
							y2 = (int)intsec.getY();
							x1 = randx;
							y1 = randy;
						}
						else
						{
							if((int)intsec.getY() > randy)
							{
								x1 = (int)intsec.getX();
								y1 = (int)intsec.getY();
								x2 = randx;
								y2 = randy;
							}
							else
							{
								x2 = (int)intsec.getX();
								y2 = (int)intsec.getY();
								x1 = randx;
								y1 = randy;
							}
						}
						Intersection[] roadInts = this.addIntersectionsIfNeeded(x1, y1, x2, y2);
						this.addRoad(new Road(0,0, ID.Road, this,  roadInts[0], roadInts[1],
								x1, y1, x2, y2));
						return;
					}
				}
			}
		}
		return;
	}
	
	
	public void createNewVehicle()
	{
		this.vehicles.add(new Vehicle(0, 0, ID.Vehicle, this, .5, this.getRandomRoad(), true));
	}
	
	// only for initializing the roadmap
	public void addIntersection(Intersection intsec)
	{
		this.intersections.add(intsec);
	}
	
	public void moveUp(double distance)
	{
		this.y -= distance;
		for(Road r : roads)
		{
			r.moveUp(distance);
		}
		for(Intersection intsec : intersections)
		{
			intsec.moveUp(distance);
		}
		for(Vehicle veh : vehicles)
		{
			veh.moveUp(distance);
		}
	}
	public void moveDown(double distance)
	{
		this.y += distance;
		for(Road r : roads)
		{
			r.moveDown(distance);
		}
		for(Intersection intsec : intersections)
		{
			intsec.moveDown(distance);
		}
		for(Vehicle veh : vehicles)
		{
			veh.moveDown(distance);
		}
	}
	public void moveLeft(double distance)
	{
		this.x -= distance;
		for(Road r : roads)
		{
			r.moveLeft(distance);
		}
		for(Intersection intsec : intersections)
		{
			intsec.moveLeft(distance);
		}
		for(Vehicle veh : vehicles)
		{
			veh.moveLeft(distance);
		}
	}
	public void moveRight(double distance)
	{
		this.x += distance;
		for(Road r : roads)
		{
			r.moveRight(distance);
		}
		for(Intersection intsec : intersections)
		{
			intsec.moveRight(distance);
		}
		for(Vehicle veh : vehicles)
		{
			veh.moveRight(distance);
		}
	}
	
	// debugging
	public boolean testIntersectionDistances()
	{
		boolean testPassed = true;
		for(Intersection intsec : intersections)
		{
			if(intsec.testIntersection())
			{
				testPassed = false;				
			}
		}
		return testPassed;
	}
	public boolean testRoadDistances()
	{
		boolean testPassed = true;
		for(Road r: roads)
		{
			if(r.testRoad())
			{
				testPassed = false;
			}
		}
		return testPassed;
	}
	
	public void printDebugStats()
	{
		System.out.println("Current Stats:");
		System.out.println("Number of roads: " + this.roads.size() + 
				" out of " + this.maxNumRoads);
		System.out.println("Number of intersections: " + this.intersections.size());
		System.out.println("Number of cars: " + this.vehicles.size());
		System.out.println();
	}
	
}
