package prequel;

import java.awt.Graphics2D;

public abstract class GameObject 
{
	protected double x, y;
	protected ID id;
	protected double dx, dy;
	
	public GameObject(double x, double y, ID id)
	{
		this.x = x;
		this.y = y;
		this.id = id;
	}
	
	public abstract void tick();
	public abstract void render(Graphics2D g);
	
	public void setX(double x)
	{
		this.x = x;
	}
	public void setY(double y)
	{
		this.y = y;
	}
	public double getX()
	{
		return this.x;
	}
	public double getY()
	{
		return this.y;
	}
	public void setID(ID id)
	{
		this.id = id;
	}
	public ID getID()
	{
		return this.id;
	}
	public void setDX(double dx)
	{
		this.dx = dx;
	}
	public void setDY(double dy)
	{
		this.dy = dy;
	}
	public double getDX()
	{
		return this.dx;
	}
	public double getDY()
	{
		return this.dy;
	}
	
	// movement on key input
	public void moveUp(double distance)
	{
		
	}
	public void moveDown(double distance)
	{
		
	}
	public void moveLeft(double distance)
	{
		
	}
	public void moveRight(double distance)
	{
		
	}
}
