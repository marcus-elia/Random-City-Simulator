package prequel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class Road extends GameObject
{
    private int x1, y1, x2, y2;
    private double slope;    // the slope of the segment
    private double yint;     // the y-intercept of the segment
    private double angle;
   
    
    // the coordinates that determine where the road is drawn in 2D
    private double startRightX, startRightY, endRightX, endRightY,
                     startLeftX, startLeftY, endLeftX, endLeftY;
    
    // the coordinates vehicles will actually use
    private double forwardStartX, forwardStartY, forwardEndX, forwardEndY,
                   backwardStartX, backwardStartY, backwardEndX, backwardEndY;
    
    // the forward start/end and backward start/end coordinates the vehicles will use
    private double fsX, fsY, feX, feY, bsX, bsY, beX, beY;
    
    private RoadMap rm;
    
    private Intersection startInt;
    private Intersection endInt;
    
    private double roadWidth;
    
    // for debugging
    private boolean isBad;
    
    // Note: I am always defining roads so that x1 <= x2, and if x1 = x2, then
    // y1 > y2 (which guarantees that roads are oriented from left to right, and
    // vertical roads are oriented from down to up)
    
	public Road(int x, int y, ID id, RoadMap rm, Intersection startInt,
			Intersection endInt, int x1, int y1, int x2, int y2) 
	{
		super(x, y, id);
		isBad = false;
		dx = 0;
		dy = 0;
		this.rm = rm;

		this.startInt = startInt;
		this.endInt = endInt;
		
		// sort the coordinates to match up with my convention on directions described above
		if(x1 < x2)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		else if (x1 > x2)
		{
			this.x1 = x2;
			this.y1 = y2;
			this.x2 = x1;
			this.y2 = y1;
		}
		else
		{
			if(y1 > y2)
			{
				this.x1 = x1;
				this.y1 = y1;
				this.x2 = x2;
				this.y2 = y2;
			}
			else
			{
				this.x1 = x2;
				this.y1 = y2;
				this.x2 = x1;
				this.y2 = y1;
			}
			
		}
		
		// compute the slope
		if(x1 == x2) // undefined slope
		{
			this.slope = 1000.5;
			this.yint = x1;
		}
		else
		{
			this.slope = (y2 - y1) / (double)(x2 - x1);
			this.yint = y1 - slope*x1;
		}
		
						
		this.roadWidth = 10;
		this.setAngle();
		
		
		// set up the points where we draw the road
		
		// vertical line case
		if(x1 == x2)
		{
			this.startRightX = x1 + roadWidth;
			this.startRightY = y1;
			this.endRightX = x1 + roadWidth;
			this.endRightY = y2;
			this.startLeftX = x1 - roadWidth;
			this.startLeftY = y1;
			this.endLeftX = x1 - roadWidth;
			this.endLeftY = y2;
		}
		else if(slope < 0)
		{
			double angleC = Math.PI/2 - Math.abs(Math.atan(this.slope)); // complement angle
			this.startRightX = this.x1 + Math.cos(angleC)*this.roadWidth;
			this.startRightY = this.y1 + Math.sin(angleC)*this.roadWidth;
			this.startLeftX = this.x1 - Math.cos(angleC)*this.roadWidth;
			this.startLeftY = this.y1 - Math.sin(angleC)*this.roadWidth;
			this.endRightX = this.x2 + Math.cos(angleC)*this.roadWidth;
			this.endRightY = this.y2 + Math.sin(angleC)*this.roadWidth;
			this.endLeftX = this.x2 - Math.cos(angleC)*this.roadWidth;
			this.endLeftY = this.y2 - Math.sin(angleC)*this.roadWidth;
		}
		else
		{
			double angleC = Math.PI/2 - Math.abs(Math.atan(this.slope)); // complement angle
			this.startRightX = this.x1 - Math.cos(angleC)*this.roadWidth;
			this.startRightY = this.y1 + Math.sin(angleC)*this.roadWidth;
			this.startLeftX = this.x1 + Math.cos(angleC)*this.roadWidth;
			this.startLeftY = this.y1 - Math.sin(angleC)*this.roadWidth;
			this.endRightX = this.x2 - Math.cos(angleC)*this.roadWidth;
			this.endRightY = this.y2 + Math.sin(angleC)*this.roadWidth;
			this.endLeftX = this.x2 + Math.cos(angleC)*this.roadWidth;
			this.endLeftY = this.y2 - Math.sin(angleC)*this.roadWidth;
		}
		
		forwardStartX = (startRightX + x1) / 2;
		forwardStartY = (startRightY + y1) / 2;
		forwardEndX = (endRightX + x2) / 2;
		forwardEndY = (endRightY + y2) / 2;
		backwardStartX = (endLeftX + x2) / 2;
	    backwardStartY = (endLeftY + y2) / 2;
	    backwardEndX = (startLeftX + x1) / 2;
	    backwardEndY = (startLeftY + y1) / 2;
	    
	    /*fsX = forwardStartX;
	    fsY = forwardStartY;
	    feX = forwardEndX;
	    feY = forwardEndY;
	    bsX = backwardStartX;
	    bsY = backwardStartY;
	    beX = backwardEndX;
	    beY = backwardEndY;*/
	    
	    
	}

	@Override
	public void tick() 
	{

		
	}

	@Override
	public void render(Graphics2D g) 
	{
		g.setColor(Color.gray);
		if(this.isBad)
		{
			 g.setColor(Color.RED);
		}
		//g.drawLine(x1, y1, x2, y2);
		
		Path2D.Double parallelogram = new Path2D.Double();
		parallelogram.moveTo(startRightX, startRightY);
		parallelogram.lineTo(endRightX, endRightY);
		parallelogram.lineTo(endLeftX, endLeftY);
		parallelogram.lineTo(startLeftX, startLeftY);
		parallelogram.lineTo(startRightX, startRightY);
		parallelogram.closePath();
	    g.fill(parallelogram);
	    
	    // the center line
	    g.setColor(Color.yellow);
	    g.drawLine(x1, y1, x2, y2);
	}
	
	// checks if this road's line segment intersects the other
	// line segment determined by the given points
	// But we check if the intersection is not at the existing
	public boolean hitsRoad(int x3, int y3, int x4, int y4)
	{
		double xOfIntersection;
		int temp;
		
		// Reorder them so x3 <= x4
		if(x4 < x3)
		{
		    temp = x3;
		    x3 = x4;
		    x4 = temp;
		    temp = y3;
		    y3 = y4;
		    y4 = temp;
		}
		if(x3 == x4 && y3 < y4)
		{
			temp = x3;
		    x3 = x4;
		    x4 = temp;
			temp = y3;
			y3 = y4;
			y4 = temp;
		}
		// if both segments are vertical
		if(x3 == x4 && this.slope == 1000.5)
		{
			return false;
		}
		// if just the other segment is vertical
		else if(x3 == x4)
		{
			return this.x1 < x3 && this.x2 > x3 && 
					this.slope*x3 + this.yint < y3 && this.slope*x3 + this.yint > y4;
		}
		
		// if the new segment is not vertical
		double otherSlope = (y4 - y3) / (double)(x4 - x3);
		double otherYint = y3 - otherSlope*x3;
		
		// if just this road is vertical
		if(this.x1 == this.x2)
		{
			return x3 < this.x1 && x4 > this.x1 && 
					otherSlope*this.x1 + otherYint < this.y1 && 
					otherSlope*this.x1 + otherYint > this.y2;
		}
		
		// if the two segments have the same slope, they do not intersect
		if(Math.abs(otherSlope - this.slope) < 0.001)
		{
			return false;
		}
		
		// otherwise, calculate the intersection point normally
		xOfIntersection = (otherYint - this.yint) / (this.slope - otherSlope);
		return xOfIntersection > this.x1 && xOfIntersection < this.x2 && xOfIntersection >= x3
				&& xOfIntersection <= x4;
	}
	
	// Euclidean distance bewteen two points
	public double distanceFormula(int a, int b, int c, int d)
	{
		return Math.sqrt((a-c)*(a-c) + (b-d)*(b-d));
	}
	
	// Returns the directed distance from a given point to the line extension of this segment 
	public double directedDistance(int x, int y)
	{
		double A, B, C;
		
		// if the road is horizontal
		if(this.slope == 0)
		{
			if(x > this.x1 && x < this.x2)
			{
				return Math.abs(y - this.y1);
			}
			else
			{
				return Math.min(this.distanceFormula(this.x1, this.y1, x, y),
						this.distanceFormula(this.x2, this.y2, x, y));
			}
		}
		// if this road is vertical
		else if(this.slope == 1000.5)
		{
			if(y > this.y2 && y < this.y1)
			{
				return Math.abs(x - this.x1);
			}
			else
			{
				return Math.min(this.distanceFormula(this.x1, this.y1, x, y),
						this.distanceFormula(this.x2, this.y2, x, y));
			}
		}
		else
		{
			A = this.slope;
			B = -1;
			C = this.yint;
			double otherSlope = -1/A;
			double otherYint = y - otherSlope*x;
			double xOfIntersection = (otherYint - this.yint) / (this.slope - otherSlope);
			if(xOfIntersection > this.x1 && xOfIntersection < this.x2)
			{
				return Math.abs(A*x + B*y + C) / Math.sqrt(A*A + B*B);
			}
			else
			{
				return Math.min(this.distanceFormula(this.x1, this.y1, x, y),
						this.distanceFormula(this.x2, this.y2, x, y));
			}
		}		
	}
	
	// =======================================
	//
	//        Setting Private Variables
	//
	// =======================================
	
	// when stuff moves, we gotta update the yint
	public void setYint()
	{
		if(x1 == x2) // undefined slope
		{
			this.yint = x1;
		}
		else
		{
			this.yint = y1 - slope*x1;
		}
	}
	public void setAngle()
	{
		if(x1 == x2)
		{
			this.angle = 3*Math.PI/2;
		}
		else
		{
			double refAngle = Math.atan(this.slope);
			if(y1 > y2)
			{
				this.angle = refAngle + 2*Math.PI;
			}
			else
			{
				this.angle = refAngle;
			}
		}
	}
	
	// draw the points the vehicles actually target, for debugging
	public void drawPointTest(Graphics2D g)
	{
		int x = (int)this.fsX;
		int y = (int)this.fsY;
		g.setColor(Color.GREEN);
		g.fillRect(x-3, y-3, 6, 6);
		
		x = (int)this.beX;
		y = (int)this.beY;
		g.setColor(Color.PINK);
		g.fillRect(x-3, y-3, 6, 6);
		
		x = (int)this.bsX;
		y = (int)this.bsY;
		g.setColor(Color.GREEN);
		g.fillRect(x-3, y-3, 6, 6);
		
		x = (int)this.feX;
		y = (int)this.feY;
		g.setColor(Color.PINK);
		g.fillRect(x-3, y-3, 6, 6);
	}
	
	public void setFS(double fsX, double fsY)
	{
		this.fsX = fsX;
		this.fsY = fsY;
	}
	public void setFE(double feX, double feY)
	{
		this.feX = feX;
		this.feY = feY;
	}
	public void setBS(double bsX, double bsY)
	{
		this.bsX = bsX;
		this.bsY = bsY;
	}
	public void setBE(double beX, double beY)
	{
		this.beX = beX;
		this.beY = beY;
	}

	// =========================================
	//
	//                 Getters
	//
	// ========================================
	
	// Return the coordinates of the starting and points of the road
	public int[] getStartCoordinates()
	{
		return new int[]{this.x1, this.y1};
	}
	public int[] getEndCoordinates()
	{
		return new int[]{this.x2, this.y2};
	}
	
	// Return the actual intersections that start and end the road
	public Intersection getStartInt()
	{
		return this.startInt;
	}
	public Intersection getEndInt()
	{
		return this.endInt;
	}
	
	// Return the actual coordinates where the road is drawn
	public double[] getDrawCoordinates()
	{
		return new double[]{startRightX, startRightY, endRightX, 
				endRightY, startLeftX, startLeftY, endLeftX, endLeftY};
	}
	
	public double getAngle()
	{
		return this.angle;
	}
	// Get the angle at that this road makes with the given intersection
	public double getAngleFromIntersection(Intersection intsec)
	{
		if(this.startInt.equals(intsec))
		{
			return this.angle;
		}
		else if(this.endInt.equals(intsec))
		{
			if(this.angle >= Math.PI)
			{
				return this.angle - Math.PI;
			}
			else
			{
				return this.angle + Math.PI;
			}
		}
	    return 1000;
	}
	
	public double[] getForwardStartCoordinates()
	{
		return new double[]{this.forwardStartX, this.forwardStartY};
	}
	public double[] getForwardEndCoordinates()
	{
		return new double[]{this.forwardEndX, this.forwardEndY};
	}
	public double[] getBackwardStartCoordinates()
	{
		return new double[]{this.backwardStartX, this.backwardStartY};
	}
	public double[] getBackwardEndCoordinates()
	{
		return new double[]{this.backwardEndX, this.backwardEndY};
	}
	public double[] getFSCoordinates()
	{
		return new double[]{this.fsX, this.fsY};
	}
	public double[] getFECoordinates()
	{
		return new double[]{this.feX, this.feY};
	}
	public double[] getBSCoordinates()
	{
		return new double[]{this.bsX, this.bsY};
	}
	public double[] getBECoordinates()
	{
		return new double[]{this.beX, this.beY};
	}
	
	// ==========================================
	//
	//         Movement for key presses
	//
	// ==========================================
	public void moveUp(double distance)
	{
		this.y1 -= distance;
		this.y2 -= distance;
		this.setYint();
		this.startRightY -= distance;
		this.endRightY -= distance;
		this.startLeftY -= distance;
		this.endLeftY -= distance;
		this.forwardStartY -= distance;
		this.forwardEndY -= distance;
		this.backwardStartY -= distance;
		this.backwardEndY -= distance;
	}
	public void moveDown(double distance)
	{
		this.y1 += distance;
		this.y2 += distance;
		this.setYint();
		this.startRightY += distance;
		this.endRightY += distance;
		this.startLeftY += distance;
		this.endLeftY += distance;
		this.forwardStartY += distance;
		this.forwardEndY += distance;
		this.backwardStartY += distance;
		this.backwardEndY += distance;
	}
	public void moveLeft(double distance)
	{
		this.x1 -= distance;
		this.x2 -= distance;
		this.setYint();
		this.startRightX -= distance;
		this.endRightX -= distance;
		this.startLeftX -= distance;
		this.endLeftX -= distance;
		this.forwardStartX -= distance;
		this.forwardEndX -= distance;
		this.backwardStartX -= distance;
		this.backwardEndX -= distance;
	}
	public void moveRight(double distance)
	{
		this.x1 += distance;
		this.x2 += distance;
		this.setYint();
		this.startRightX += distance;
		this.endRightX += distance;
		this.startLeftX += distance;
		this.endLeftX += distance;
		this.forwardStartX += distance;
		this.forwardEndX += distance;
		this.backwardStartX += distance;
		this.backwardEndX += distance;
	}
	
	public boolean testRoad()
	{
		for(Intersection intsec : this.rm.getIntersections())
		{
			if(!intsec.equals(this.startInt) && !intsec.equals(this.endInt) &&
					intsec.directedDistance(x1, y1, x2, y2) < rm.getMinIntersectionDistance())
			{
				this.isBad = true;
				return true;
			}
		}
		this.isBad = false;
		return false;
	}
}
