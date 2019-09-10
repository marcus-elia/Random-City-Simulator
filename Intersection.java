package prequel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class Intersection extends GameObject
{

	private int numRoads;
	//private ArrayList<Road> roads;
	
	// Roads are stored in order by the angle they leave this intersection at, from
	// least to greatest, with 0 being directly to the right, and going clockwise.
	private LinkedList<Road> roadsLL;
	
	private RoadMap rm;
	
	private ArrayList<Double> intersectionFillXPoints;
	private ArrayList<Double> intersectionFillYPoints;
	private Path2D intersectionFill;
	
	// a list of the intersections directly connected to this by a road
	private ArrayList<Intersection> neighbors;
	
	// for debugging
	private boolean isBad;
	
	public Intersection(double x, double y, ID id, RoadMap rm) 
	{
		super(x, y, id);
		this.rm = rm;
		//this.roads = new ArrayList<Road>();
		this.roadsLL = new LinkedList<Road>();
		this.numRoads = this.roadsLL.size();
		this.neighbors = new ArrayList<Intersection>();
		isBad = false;
		intersectionFill = new Path2D.Double();
		this.intersectionFillXPoints = new ArrayList<Double>();
		this.intersectionFillYPoints = new ArrayList<Double>();
		intersectionFillXPoints.add(x);
		intersectionFillYPoints.add(y);
		intersectionFill.moveTo(x, y);
		this.updateIntersectionFillPoints();
		this.updateIntersectionFill();
	}

	@Override
	public void tick() 
	{
				
	}
	
	/*public void highlightRoads(Graphics2D g)
	{
		//debug
				ListIterator<Road> iter = (ListIterator)roadsLL.iterator();
				Road curRoad;
				double[] points;
				int i = 1;
				Path2D highLightRoad = new Path2D.Double();
				
				while(iter.hasNext())
				{
					highLightRoad = new Path2D.Double();
					curRoad = iter.next();
					points = curRoad.getDrawCoordinates();
					highLightRoad.moveTo(points[0], points[1]);
					highLightRoad.lineTo(points[2], points[3]);
					highLightRoad.lineTo(points[6], points[7]);
					highLightRoad.lineTo(points[4], points[5]);
					highLightRoad.closePath();
					if(i == 1)
					{
						g.setColor(Color.red);
						System.out.println("red: " + curRoad.getAngleFromIntersection(this)*180/Math.PI);
					}
					else if(i == 2)
					{
						g.setColor(Color.orange);
						System.out.println("orange: " + curRoad.getAngleFromIntersection(this)*180/Math.PI);
					}
					else if(i == 3)
					{
						g.setColor(Color.green);
						System.out.println("green: " + curRoad.getAngleFromIntersection(this)*180/Math.PI);
					}
					else if(i == 4)
					{
						g.setColor(Color.blue);
						System.out.println("blue: " + curRoad.getAngleFromIntersection(this)*180/Math.PI);
					}
					g.fill(highLightRoad);
					i++;
					
				}
	}*/

	@Override
	public void render(Graphics2D g) 
	{
		g.setColor(Color.WHITE);
		if(this.numRoads > 3)
		{
			g.setColor(Color.RED);
		}
		/*if(this.isBad)
		{
			g.setColor(Color.CYAN);
			g.fillRect((int)this.x-3, (int)this.y-3, 6, 6);
			int radius = (int)rm.getMinIntersectionDistance();
			g.drawArc((int)this.x - radius, (int)this.y - radius, 2*radius, 2*radius, 0, 360);
		}*/
		//g.fillRect((int)this.x-2, (int)this.y-2, 4, 4);
		g.setColor(Color.gray);

		// fill in the gaps in the intersection based on the roads going into it
		if(numRoads > 1)
		{
			//g.setColor(Color.DARK_GRAY);
			g.fill(intersectionFill);
		}
	}
	
	// update the points that are used in the Path2D object that fills in the intersection
	public void updateIntersectionFillPoints()
	{
		// if we have more than one road, calculate things based upon that
		if(numRoads > 1)
		{
			ListIterator<Road> iter = (ListIterator)roadsLL.iterator();

			// for calculating intersection points between roads and the intersection fill
			double x1, y1, x2, y2, x3, y3, x4, y4;
			
			int arrSize;
			double[] point;
			
			this.intersectionFillXPoints = new ArrayList<Double>();
			this.intersectionFillYPoints = new ArrayList<Double>();
			
			// start at the last and first road in the linked list
			Road prevRoad = roadsLL.getLast();
			double[] prevRoadPoints = prevRoad.getDrawCoordinates();
			Road curRoad = roadsLL.getFirst();
			double[] curRoadPoints = curRoad.getDrawCoordinates();
			double angleDifference = curRoad.getAngleFromIntersection(this) - prevRoad.getAngleFromIntersection(this);
			
			// if the two roads form an angle that is less than 180, 
			// just draw to their intersection point
			if((angleDifference > 0 && angleDifference < Math.PI) || 
					(angleDifference > -2*Math.PI && angleDifference < -Math.PI))
			{
				double[] drawPoint = this.roadsIntersection(prevRoad, curRoad);
				intersectionFillXPoints.add(drawPoint[0]);
				intersectionFillYPoints.add(drawPoint[1]);
			}
			// otherwise, connect to both of the roads' endpoints
			else
			{
				// if the current road starts at this intersection
				if(curRoad.getStartInt().equals(this))
				{
					if(prevRoad.getStartInt().equals(this))
					{
						intersectionFillXPoints.add(prevRoadPoints[0]);
						intersectionFillYPoints.add(prevRoadPoints[1]);
					}
					else
					{
						intersectionFillXPoints.add(prevRoadPoints[6]);
						intersectionFillYPoints.add(prevRoadPoints[7]);
					}
					intersectionFillXPoints.add(curRoadPoints[4]);
					intersectionFillYPoints.add(curRoadPoints[5]);
				}
				// if the current road ends at this intersection
				else
				{
					if(prevRoad.getStartInt().equals(this))
					{
						intersectionFillXPoints.add(prevRoadPoints[0]);
						intersectionFillYPoints.add(prevRoadPoints[1]);
					}
					else
					{
						intersectionFillXPoints.add(prevRoadPoints[6]);
						intersectionFillYPoints.add(prevRoadPoints[7]);
					}
					intersectionFillXPoints.add(curRoadPoints[2]);
					intersectionFillYPoints.add(curRoadPoints[3]);
				}
			}
			
			// now iterate through all the roads
			iter.next();
			while(iter.hasNext())
			{
				prevRoad = curRoad;
				prevRoadPoints = curRoadPoints;
				curRoad = iter.next();
				curRoadPoints = curRoad.getDrawCoordinates();
				angleDifference = curRoad.getAngleFromIntersection(this) - prevRoad.getAngleFromIntersection(this);
				
				// if the two roads form an angle that is less than 180, just draw their intersection point
				if((angleDifference > 0 && angleDifference < Math.PI) || 
						(angleDifference > -2*Math.PI && angleDifference < -Math.PI))
				{
					double[] drawPoint = this.roadsIntersection(prevRoad, curRoad);
					intersectionFillXPoints.add(drawPoint[0]);
					intersectionFillYPoints.add(drawPoint[1]);
					//intersectionFill.lineTo(drawPoint[0], drawPoint[1]);
					arrSize = intersectionFillXPoints.size();
					
					if(prevRoad.getStartInt().equals(this))
					{
						x1 = prevRoad.getForwardStartCoordinates()[0];
						y1 = prevRoad.getForwardStartCoordinates()[1];
						x2 = prevRoad.getForwardEndCoordinates()[0];
						y2 = prevRoad.getForwardEndCoordinates()[1];
						x3 = intersectionFillXPoints.get(arrSize-2);
						y3 = intersectionFillYPoints.get(arrSize-2);
						x4 = intersectionFillXPoints.get(arrSize-1);
						y4 = intersectionFillYPoints.get(arrSize-1);
						
						point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
						prevRoad.setFS(point[0], point[1]);
						
						x1 = prevRoad.getBackwardStartCoordinates()[0];
						y1 = prevRoad.getBackwardStartCoordinates()[1];
						x2 = prevRoad.getBackwardEndCoordinates()[0];
						y2 = prevRoad.getBackwardEndCoordinates()[1];
						
						point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
						prevRoad.setBE(point[0], point[1]);
					}
					else
					{
						x1 = prevRoad.getBackwardStartCoordinates()[0];
						y1 = prevRoad.getBackwardStartCoordinates()[1];
						x2 = prevRoad.getBackwardEndCoordinates()[0];
						y2 = prevRoad.getBackwardEndCoordinates()[1];
						x3 = intersectionFillXPoints.get(arrSize-2);
						y3 = intersectionFillYPoints.get(arrSize-2);
						x4 = intersectionFillXPoints.get(arrSize-1);
						y4 = intersectionFillYPoints.get(arrSize-1);
						
						point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
						prevRoad.setBS(point[0], point[1]);
						
						x1 = prevRoad.getForwardStartCoordinates()[0];
						y1 = prevRoad.getForwardStartCoordinates()[1];
						x2 = prevRoad.getForwardEndCoordinates()[0];
						y2 = prevRoad.getForwardEndCoordinates()[1];
						
						point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
						prevRoad.setFE(point[0], point[1]);
					}
				}
				// otherwise, connect to both of the roads endpoints
				else
				{
					if(curRoad.getStartInt().equals(this))
					{
						if(prevRoad.getStartInt().equals(this))
						{
							intersectionFillXPoints.add(prevRoadPoints[0]);
							intersectionFillYPoints.add(prevRoadPoints[1]);
						}
						else
						{
							intersectionFillXPoints.add(prevRoadPoints[6]);
							intersectionFillYPoints.add(prevRoadPoints[7]);
						}
						intersectionFillXPoints.add(curRoadPoints[4]);
						intersectionFillYPoints.add(curRoadPoints[5]);
						
						
					}
					else
					{
						if(prevRoad.getStartInt().equals(this))
						{
							intersectionFillXPoints.add(prevRoadPoints[0]);
							intersectionFillYPoints.add(prevRoadPoints[1]);
						}
						else
						{
							intersectionFillXPoints.add(prevRoadPoints[6]);
							intersectionFillYPoints.add(prevRoadPoints[7]);
						}
						intersectionFillXPoints.add(curRoadPoints[2]);
						intersectionFillYPoints.add(curRoadPoints[3]);
					}
					arrSize = intersectionFillXPoints.size();
					
					if(prevRoad.getStartInt().equals(this))
					{
						x1 = prevRoad.getForwardStartCoordinates()[0];
						y1 = prevRoad.getForwardStartCoordinates()[1];
						x2 = prevRoad.getForwardEndCoordinates()[0];
						y2 = prevRoad.getForwardEndCoordinates()[1];
						x3 = intersectionFillXPoints.get(arrSize-2);
						y3 = intersectionFillYPoints.get(arrSize-2);
						x4 = intersectionFillXPoints.get(arrSize-3);
						y4 = intersectionFillYPoints.get(arrSize-3);
						
						point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
						prevRoad.setFS(point[0], point[1]);
						
						x1 = prevRoad.getBackwardStartCoordinates()[0];
						y1 = prevRoad.getBackwardStartCoordinates()[1];
						x2 = prevRoad.getBackwardEndCoordinates()[0];
						y2 = prevRoad.getBackwardEndCoordinates()[1];
						
						point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
						prevRoad.setBE(point[0], point[1]);
					}
					else
					{
						x1 = prevRoad.getBackwardStartCoordinates()[0];
						y1 = prevRoad.getBackwardStartCoordinates()[1];
						x2 = prevRoad.getBackwardEndCoordinates()[0];
						y2 = prevRoad.getBackwardEndCoordinates()[1];
						x3 = intersectionFillXPoints.get(arrSize-2);
						y3 = intersectionFillYPoints.get(arrSize-2);
						x4 = intersectionFillXPoints.get(arrSize-3);
						y4 = intersectionFillYPoints.get(arrSize-3);
						
						point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
						prevRoad.setBS(point[0], point[1]);
						
						x1 = prevRoad.getForwardStartCoordinates()[0];
						y1 = prevRoad.getForwardStartCoordinates()[1];
						x2 = prevRoad.getForwardEndCoordinates()[0];
						y2 = prevRoad.getForwardEndCoordinates()[1];
						
						point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
						prevRoad.setFE(point[0], point[1]);
					}
				}
			}
			arrSize = intersectionFillXPoints.size();
			prevRoad = roadsLL.getLast();
			if(prevRoad.getStartInt().equals(this))
			{
				x1 = prevRoad.getForwardStartCoordinates()[0];
				y1 = prevRoad.getForwardStartCoordinates()[1];
				x2 = prevRoad.getForwardEndCoordinates()[0];
				y2 = prevRoad.getForwardEndCoordinates()[1];
				x3 = intersectionFillXPoints.get(0);
				y3 = intersectionFillYPoints.get(0);
				x4 = intersectionFillXPoints.get(arrSize-1);
				y4 = intersectionFillYPoints.get(arrSize-1);
				
				point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
				prevRoad.setFS(point[0], point[1]);
				
				x1 = prevRoad.getBackwardStartCoordinates()[0];
				y1 = prevRoad.getBackwardStartCoordinates()[1];
				x2 = prevRoad.getBackwardEndCoordinates()[0];
				y2 = prevRoad.getBackwardEndCoordinates()[1];
				
				point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
				prevRoad.setBE(point[0], point[1]);
			}
			else
			{
				x1 = prevRoad.getBackwardStartCoordinates()[0];
				y1 = prevRoad.getBackwardStartCoordinates()[1];
				x2 = prevRoad.getBackwardEndCoordinates()[0];
				y2 = prevRoad.getBackwardEndCoordinates()[1];
				x3 = intersectionFillXPoints.get(0);
				y3 = intersectionFillYPoints.get(0);
				x4 = intersectionFillXPoints.get(arrSize-1);
				y4 = intersectionFillYPoints.get(arrSize-1);
				
				point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
				prevRoad.setBS(point[0], point[1]);
				
				x1 = prevRoad.getForwardStartCoordinates()[0];
				y1 = prevRoad.getForwardStartCoordinates()[1];
				x2 = prevRoad.getForwardEndCoordinates()[0];
				y2 = prevRoad.getForwardEndCoordinates()[1];
				
				point = this.intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4);
				prevRoad.setFE(point[0], point[1]);
			}
		}
		else if(numRoads > 0)
		{
			Road r = roadsLL.getFirst();
			double point[];
			if(r.getStartInt().equals(this))
			{
				point = r.getForwardStartCoordinates();
				r.setFS(point[0], point[1]);
				point = r.getBackwardEndCoordinates();
				r.setBE(point[0], point[1]);
			}
			else
			{
				point = r.getForwardEndCoordinates();
				r.setFE(point[0], point[1]);
				point = r.getBackwardStartCoordinates();
				r.setBS(point[0], point[1]);
			}
		}
	}
	
	// whenever we add a new road, or we move, we need to update the Path2D that
	// fills the center of the intersection
	public void updateIntersectionFill()
	{
		this.intersectionFill = new Path2D.Double();
		this.intersectionFill.moveTo(intersectionFillXPoints.get(0), 
				intersectionFillYPoints.get(0));
		
		for(int i = 1; i < intersectionFillXPoints.size(); i++)
		{
			intersectionFill.lineTo(intersectionFillXPoints.get(i),
					intersectionFillYPoints.get(i));
		}
		intersectionFill.closePath();
	}

	
	
	// add a road that starts at this intersection
	public void addRoadStart(Road r)
	{
		//this.roads.add(r);
		this.numRoads++;
		

		// add the end of the road to the neighbors here
		int[] coords = r.getEndCoordinates();
		this.neighbors.add(this.rm.hasIntersection(coords[0], coords[1]));
		
		// put the road in proper spot in the linked list
		double angleToAdd = r.getAngleFromIntersection(this);
		if(this.roadsLL.isEmpty() || angleToAdd < roadsLL.getFirst().getAngleFromIntersection(this))
		{
			roadsLL.addFirst(r);
		}
		else
		{
			double curAngle;
			Road curRoad;
			ListIterator<Road> iter = (ListIterator)roadsLL.iterator();
			
			while(iter.hasNext())
			{
				curRoad = iter.next();
				curAngle = curRoad.getAngleFromIntersection(this);
				if(angleToAdd < curAngle)
				{
					iter.previous();
					iter.add(r);
					this.updateIntersectionFillPoints();
					this.updateIntersectionFill();
					return;
				}
			}
			roadsLL.addLast(r);
		}
		this.updateIntersectionFillPoints();
		this.updateIntersectionFill();
	}
	// add a road that end at this intersection
	public void addRoadEnd(Road r)
	{
		//this.roads.add(r);
		this.numRoads++;
	
		// add the start of the road to the neighbors here
		int[] coords = r.getStartCoordinates();
		this.neighbors.add(this.rm.hasIntersection(coords[0], coords[1]));
		
		// put the road in proper spot in the linked list
		double angleToAdd = r.getAngleFromIntersection(this);
		if(this.roadsLL.isEmpty() || angleToAdd < roadsLL.getFirst().getAngleFromIntersection(this))
		{
			roadsLL.addFirst(r);
		}
		else
		{
			double curAngle;
			Road curRoad;
			ListIterator<Road> iter = (ListIterator)roadsLL.iterator();
			
			while(iter.hasNext())
			{
				curRoad = iter.next();
				curAngle = curRoad.getAngleFromIntersection(this);
				if(angleToAdd < curAngle)
				{
					iter.previous();
					iter.add(r);
					this.updateIntersectionFillPoints();
					this.updateIntersectionFill();
					return;
				}
			}
			roadsLL.addLast(r);
		}
		this.updateIntersectionFillPoints();
		this.updateIntersectionFill();
	}
	
	/*public ArrayList<Road> getRoads()
	{
		return this.roads;
	}*/
	public LinkedList<Road> getRoads()
	{
		return this.roadsLL;
	}
	
	// Returns true if this intersection is the same as the given coordinates
	public boolean sameCoordinates(int x, int y)
	{
		double tolerance = .4;
		return Math.sqrt((this.x - x)*(this.x - x) + (this.y - y)*(this.y - y)) < tolerance;
	}
	
	// Returns a random Road object from this intersection
	public Road getRandomRoad()
	{
		int rand = (int) Math.floor(Math.random() * roadsLL.size());
		return roadsLL.get(rand);
	}
	
	// Returns a random road that is not the given Road, unless it is the only one
	public Road getRandomRoadExcept(Road r)
	{
		if(this.numRoads() == 1)
		{
			return roadsLL.get(0);
		}
		int rand = (int) Math.floor(Math.random() * roadsLL.size());
		Road curRoad = roadsLL.get(rand);
		while(curRoad.equals(r))
		{
			rand = (int) Math.floor(Math.random() * roadsLL.size());
			curRoad = roadsLL.get(rand);
		}
		return curRoad;
	}
	
	public int[] getCoordinates()
	{
		return new int[]{(int)Math.round(this.x), (int)Math.round(this.y)};
	}
	
	// Get the number of roads
	public int numRoads()
	{
		return this.numRoads;
	}
	// Get the list of neighbors
    public ArrayList<Intersection> getNeighbors()
    {
    	return this.neighbors;
    }
    public Path2D getIntersectionFill()
    {
    	return this.intersectionFill;
    }
    
    // Given that two roads intersect at this Intersection, this finds the point where
    // their lines actually hit
    public double[] roadsIntersection(Road r1, Road r2)
    {
    	double[] r1Points = r1.getDrawCoordinates();
    	double[] r2Points = r2.getDrawCoordinates();
    	
    	// check if r1 and r2 start or end at this intersection
    	boolean r1Start = r1.getStartInt().equals(this);
    	boolean r2Start = r2.getStartInt().equals(this);
    	if(r1Start && r2Start)
    	{
    		return this.intersectionPoint(r1Points[0], r1Points[1], r1Points[2], r1Points[3],
    				r2Points[4], r2Points[5], r2Points[6], r2Points[7]);
    	}
    	else if(r1Start)
    	{
    		return this.intersectionPoint(r1Points[0], r1Points[1], r1Points[2], r1Points[3], 
    				r2Points[0], r2Points[1], r2Points[2], r2Points[3]);
    	}
    	else if(r2Start)
    	{
    		return this.intersectionPoint(r1Points[4], r1Points[5], r1Points[6], r1Points[7], 
    				r2Points[4], r2Points[5], r2Points[6], r2Points[7]);
    	}
    	else
    	{
    		return this.intersectionPoint(r1Points[4], r1Points[5], r1Points[6], r1Points[7], 
    				r2Points[0], r2Points[1], r2Points[2], r2Points[3]);
    	}
    	
    }
    
    // Calculate the intersection point between the lines determined by these
    // 4 points. Must assume they are not both vertical
    public double[] intersectionPoint(double x1, double y1, double x2, double y2, 
    		double x3, double y3, double x4, double y4)
    {
    	double x,y, slope1, slope2, b1, b2;
    	if(x1 == x2)
    	{
    		slope2 = (y4 - y3) / (x4 - x3);
    		b2 = y3 - slope2 * x3;
    		x = x1;
    		y = slope2*x + b2;
    		return new double[]{x,y};
    	}
    	if(x3 == x4)
    	{
    		slope1 = (y2 - y1) / (x2 - x1);
    		b1 = y1 - slope1 * x1;
    		x = x3;
    		y = slope1*x + b1;
    		return new double[]{x,y};
    	}
    	else
    	{
    		slope1 = (y2 - y1) / (x2 - x1);
    		b1 = y1 - slope1 * x1;
    		slope2 = (y4 - y3) / (x4 - x3);
    		b2 = y3 - slope2 * x3;
    		x = (b2 - b1) / (slope1 - slope2);
    		y = slope1*x + b1;
    		return new double[]{x,y};
    	}
    }
    
    // assuming that both r1 and r2 belong to this intersection, and the vehicle
    // is approaching the intersection along r1, this returns the point the vehicle
    // should target to transition from r1 to r2
    public double[] getVehicleIntersection(Road r1, Road r2)
    {
    	double[] r1points, startPoint, endPoint, r2points;
    	if(r1.getStartInt().equals(this))
    	{
    		startPoint = r1.getBackwardStartCoordinates();
    		endPoint = r1.getBackwardEndCoordinates();
    	}
    	else
    	{
    		startPoint = r1.getForwardStartCoordinates();
    		endPoint = r1.getForwardEndCoordinates();
    	}
		r1points = new double[]{startPoint[0], startPoint[1], endPoint[0], endPoint[1]};
		
		if(r2.getStartInt().equals(this))
		{
			startPoint = r2.getForwardStartCoordinates();
    		endPoint = r2.getForwardEndCoordinates();
		}
		else
    	{
    		startPoint = r2.getBackwardStartCoordinates();
    		endPoint = r2.getBackwardEndCoordinates();
    	}
		r2points = new double[]{startPoint[0], startPoint[1], endPoint[0], endPoint[1]};
		
		// intersect the two roads
		return this.intersectionPoint(r1points[0], r1points[1], r1points[2], r1points[3], 
				r2points[0], r2points[1], r2points[2], r2points[3]);
    }
    
    // the distance from this intersection to a given point
	public double distanceToPoint(int px, int py)
	{
		return Math.sqrt((this.x - px)*(this.x-px) + (this.y-py)*(this.y-py));
	}
	
	
	
	public double directedDistance(int x1, int y1, int  x2, int y2)
	{
		double distPA = this.distanceToPoint(x1, y1);
		double distPB = this.distanceToPoint(x2, y2);
		double distAB = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
		
		double sqdistPA = distPA * distPA;
        double sqdistPB = distPB * distPB;
        double sqdistAB = distAB * distAB;
        
        if(sqdistPA > sqdistPB + sqdistAB || sqdistPB > sqdistPA + sqdistAB)
        {
        	return Math.min(distPA, distPB);
        }
        
        double A = y2 - y1;
        double B = x1 - x2;
        double C = y1*x2 - y2*x1;
        
        return Math.abs(A*x + B*y + C) / Math.sqrt(A*A + B*B);
	}
	
	public void moveUp(double distance)
	{
		this.y -= distance;
		this.updateIntersectionFillPoints();
		this.updateIntersectionFill();
	}
	public void moveDown(double distance)
	{
		this.y += distance;
		this.updateIntersectionFillPoints();
		this.updateIntersectionFill();
	}
	public void moveLeft(double distance)
	{
		this.x -= distance;
		this.updateIntersectionFillPoints();
		this.updateIntersectionFill();
	}
	public void moveRight(double distance)
	{
		this.x += distance;
		this.updateIntersectionFillPoints();
		this.updateIntersectionFill();
	}
	
	// for debugging, checks if the intersection is too close to a road
	public boolean testIntersection()
	{
		int x1, y1, x2, y2;
		for(Road r : this.rm.getRoads())
		{
			x1 = r.getStartCoordinates()[0];
			y1 = r.getStartCoordinates()[1];
			x2 = r.getEndCoordinates()[0];
			y2 = r.getEndCoordinates()[1];
			if(!this.roadsLL.contains(r) && 
					this.directedDistance(x1, y1, x2, y2) < this.rm.getMinIntersectionDistance())
			{
				if(!this.isBad)
				{
					System.out.println(this.directedDistance(x1, y1, x2, y2));
				}
				this.isBad = true;
				return true;
			}
		}
		this.isBad = false;
		return false;
	}

}
