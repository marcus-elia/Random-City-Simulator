package prequel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

public class Vehicle extends GameObject
{
    // every vehicle gets the road map
	private RoadMap rm;
	
	// the vehicle's current speed
	private double speed;
	// normal speed for driving straight
	private double normalSpeed;
		
	private double acceleration;
		
	// the x and y coordinates this vehicle is heading toward next
	// these could be part of a turn
	private double targetX;
	private double targetY;
	
	// the road we are on
	private Road currentRoad;
	// the intersection we will come to next
	private Intersection nextInt;
	// the road we will go on after the next intersection
	private Road nextRoad;
	private boolean isGoingForward;  // is the vehicle going forward according
	                                  // to the road's orientation
	// if we are facing toward a dead end, we are stuck
	private boolean isStuck;
	// if this is true, then the vehicle is in an intersection
	private boolean isInIntersection;
	// if we are on a road and not in an intersection
	private boolean isOnRoad;
	// if we are stopped before going through an intersection
	private boolean isStopped;
	
	// how long we have been stopped for
	private int stopTime;
	
	// variables to track when going through an intersection
	
	// how far we have to the target
	private double distanceToTarget;
	
	private double endIntX;
	private double endIntY;
	
	private double angle;
	
	private Color paintColor;
	// The distance from the center to the front of the vehicle
	private double frontLength;
	
	// how long we have been stuck in a row
	private int stuckTime;
	
	public Vehicle(int x, int y, ID id, RoadMap rm, double normalSpeed, 
			Road currentRoad, boolean isGoingForward) 
	{
		super(x, y, id);
		this.rm = rm;
		
		// set the original speed, acceleration, and position
		this.normalSpeed = 1 + (Math.random()-.5);
		this.speed = 0;
		this.acceleration = .05;
		this.currentRoad = currentRoad;
		this.isGoingForward = isGoingForward;
		double[] startCoords = currentRoad.getForwardStartCoordinates();
		this.x = startCoords[0];
		this.y = startCoords[1];

		this.isInIntersection = false;
		this.isOnRoad = true;
		this.isStopped = false;
		this.stopTime = 0;
		this.setIntersection();
		this.isStuck = this.checkStuck();
		if(this.isStuck)
		{
			this.setTargetStuck();
		}
		else
		{
			this.setTargetRoad();
		}
		this.setAngle();
		this.setNextRoad();
		
		this.setColor();
		this.frontLength = 10;
	}

	@Override
	public void tick() 
	{
		// If we are at the target point
		if(this.x == this.targetX && this.y == this.targetY)
		{
			if(this.isStuck)
			{
				this.stuckTime++;
				// If we've been stuck too long, goodbye
				if(this.stuckTime == 500)
				{
					this.rm.getVehicles().remove(this);
					return;
				}
				if(!this.checkStuck())
				{
					this.isStuck = false;
					this.setTargetRoad();
					this.setNextRoad();
					this.stuckTime = 0;
				}
				return;
			}
			// If we are at the end of the road, go through the intersection
			else if(this.isOnRoad)
			{
				this.isOnRoad = false;
				this.isInIntersection = true;
				this.setTargetIntersection();
				this.setAngle();
				return;
			}
			// if we are at the end of the intersection, go on the new road
			else
			{
				this.isOnRoad = true;
				this.isInIntersection = false;
				this.currentRoad = this.nextRoad;
				this.setIntersection();
				this.isStuck = this.checkStuck();
				if(this.isStuck)
				{
					this.setTargetStuck();
				}
				else
				{
					this.setTargetRoad();
					this.setNextRoad();
				}
				this.setAngle();
				return;
				
			}
		}
		
		double curDistanceToGo = this.distanceFormula(this.x, this.y,
				this.targetX, this.targetY);
		
		
		// if we're close enough to the target point that we will overshoot it, just
		// go directly to it
		if(curDistanceToGo < this.speed)
		{
			this.x = this.targetX;
			this.y = this.targetY;
		}
		
		// if we are on a road and the front of the vehicle is close to the start of
		// the intersection, then stop
		else if(this.isOnRoad && !this.isStopped && !this.isStuck &&
				curDistanceToGo < (this.frontLength + this.speed) &&
				curDistanceToGo > this.frontLength)
		{
			this.isStopped = true;
			return;
		}
		
		// if we are not at or near the target, keep moving
		else if(this.x != this.targetX || this.y != this.targetY)
		{
			if(this.isStopped)
			{
				if(this.stopTime < 100)
				{
					this.stopTime++;
					return;
				}
				else
				{
					this.isStopped = false;
					this.stopTime = 0;
				}
			}
			this.x += this.dx;
			this.y += this.dy;
			
			// if we are not at normal speed and we are on a road, accelerate
			if(this.isOnRoad && this.speed < this.normalSpeed)
			{
				this.speed += this.acceleration;
				this.setDxDy();
			}
		}
			
		
	}
	
	public void setAngle()
	{
		this.angle = Math.atan2(this.targetY - this.y, this.targetX - this.x);
		this.setDxDy();	
	}
	
	
	
	// if we are on a road, set targetX and targetY to be the end of the road
	public void setTargetRoad()
	{
		double[] target;
		if(this.isGoingForward)
		{
			target = this.currentRoad.getFECoordinates();
		}
		else
		{
			target = this.currentRoad.getBECoordinates();
		}
		this.targetX = target[0];
		this.targetY = target[1];
	}
	
	// if we are on a road, set targetX and targetY to be the start of the new road
	public void setTargetIntersection()
	{
		double[] target;
		if(this.nextRoad.getStartInt().equals(this.nextInt))
		{
			target = this.nextRoad.getFSCoordinates();
			this.isGoingForward = true;
		}
		else
		{
			target = this.nextRoad.getBSCoordinates();
			this.isGoingForward = false;
		}
		this.targetX = target[0];
		this.targetY = target[1];
	}
	
	public void setTargetStuck()
	{
		double[] roadStart;
		double[] roadEnd;
		if(this.isGoingForward)
		{
			roadStart = this.currentRoad.getForwardStartCoordinates();
			roadEnd = this.currentRoad.getForwardEndCoordinates();
		}
		else
		{
			roadStart = this.currentRoad.getBackwardStartCoordinates();
			roadEnd = this.currentRoad.getBackwardEndCoordinates();
		}
		this.targetX = (roadStart[0] + roadEnd[0]) / 2;
		this.targetY = (roadStart[1] + roadEnd[1]) / 2;
		return;
	}
	
	
    // Set dx and dy. This should be done every time the vehicle either rotates
	// or changes speed
	public void setDxDy()
	{
		this.dx = this.speed*Math.cos(this.angle);
		this.dy = this.speed*Math.sin(this.angle);
	}
	
	@Override
	public void render(Graphics2D g) 
	{
		g.setColor(this.paintColor);
			
		// these lists are each coordinate's displacement from this.x, this.y
		double xpoints[] = {-5, 0, 5, 5};
	    double ypoints[] = {5, -10, 5, 5};
	    double rotationAngle = this.angle + Math.PI/2; // idk why, but it has to be rotated by 90
	    for(int i = 0; i < xpoints.length; i++)
	    {
	    	double temp = x + xpoints[i]*Math.cos(rotationAngle) - ypoints[i]*Math.sin(rotationAngle);
	    	ypoints[i] =  y + xpoints[i]*Math.sin(rotationAngle) + ypoints[i]*Math.cos(rotationAngle);
	    	xpoints[i] = temp;
	    }
	    Path2D path = new Path2D.Double();
	    path.moveTo(xpoints[0], ypoints[0]);
	    for(int i = 1; i < xpoints.length; i++)
	    {
	    	path.lineTo(xpoints[i], ypoints[i]);
	    }
	    path.closePath();
	    g.fill(path);
	    //g.setColor(Color.BLUE);
	    //g.fillRect((int)this.targetX-3, (int)this.targetY-3, 6, 6);
	}
	
	public boolean checkStuck()
	{
		if(this.nextInt.numRoads() == 1)
		{
			return true;
		}
		else
		{
			//this.setNextRoad();
			//this.setTarget();
			return false;
		}
	}
	
	public boolean checkInIntersection()
	{
		return this.nextInt.getIntersectionFill().contains(this.x, this.y);
	}
	
	// Euclidean distance
	public double distanceFormula(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	
	public void chooseRandomRoad(Intersection intsec)
	{
		Road r = intsec.getRandomRoadExcept(this.currentRoad);
		this.currentRoad = r;
		if(isSameTuple(r.getStartCoordinates(),intsec.getCoordinates()))
		{
			this.isGoingForward = true;
		}
		else
		{
			this.isGoingForward = false;
		}
	}
	
	// This checks if two int arrays are the same
	public boolean isSameTuple(int[] tuple1, int[] tuple2)
	{
		for(int i = 0; i < tuple1.length; i++)
		{
			if(tuple1[i] != tuple2[i])
			{
				return false;
			}
		}
		return true;
	}
	
	/*public double[] getNextIntersectionPoint(double endX, double endY, double d, double alpha,
			double thetaPrev, double prevX, double prevY, int i, int n)
	{
		double theta = thetaPrev + alpha/(n+1);
		double b = 2*Math.cos(theta)*(prevX - endX) + 2*Math.sin(theta)*(prevY - endY);
		double c = (prevX - endX)*(prevX - endX) + (prevY - endY)*(prevY - endY) 
		         - d*d*(1 - (i+1)/(n+1))*(1 - (i+1)/(n+1));
		double l = (-b - Math.sqrt(b*b - 4*c))/2;
		System.out.println("Finding new point, given");
		System.out.println(this.nextInt.getX());
		System.out.println(this.nextInt.getY());
		System.out.println();
		System.out.println(endX);
		System.out.println(endY);
		System.out.println(d);
		System.out.println(alpha);
		System.out.println(thetaPrev);
		System.out.println(prevX);
		System.out.println(prevY);
		System.out.println(i);
		System.out.println(n);
		System.out.println(this.nextRoad.getAngleFromIntersection(this.nextInt));
		
		return new double[]{prevX + l*Math.cos(theta), prevY + l*Math.sin(theta)};
	}*/
	
	public void setIntersection()
	{
		if(this.isGoingForward)
		{
			this.nextInt = this.currentRoad.getEndInt();
		}
		else
		{
			this.nextInt = this.currentRoad.getStartInt();
		}
	}
	
	public void setNextRoad()
	{
		if(this.nextInt.numRoads() == 1)
		{
			this.isStuck = true;
			return;
		}
		Road r = this.nextInt.getRandomRoadExcept(this.currentRoad);
		this.nextRoad = r;
	}

	// movement for keypresses
	public void moveUp(double distance)
	{
		this.y -= distance;
		this.targetY -= distance;
	}
	public void moveDown(double distance)
	{
		this.y += distance;
		this.targetY += distance;
	}
	public void moveLeft(double distance)
	{
		this.x -= distance;
		this.targetX -= distance;
	}
	public void moveRight(double distance)
	{
		this.x += distance;
		this.targetX += distance;
	}
	
	public int exp(int base, int exponent)
	{
		if(exponent == 0)
		{
			return 1;
		}
		return base * exp(base, exponent - 1);
	}
	
	public void setColor()
	{
		int colorChoice = (int)(Math.random()*4);
		if(colorChoice == 0)
		{
			this.paintColor = new Color(180, 20, 30);
		}
		else if(colorChoice == 1)
		{
			this.paintColor = new Color(10, 150, 20);
		}
		else if(colorChoice == 2)
		{
			this.paintColor = new Color(30, 60, 150);
		}
		else if(colorChoice == 3)
		{
			this.paintColor = new Color(100, 40, 120);
		}
	}
}
