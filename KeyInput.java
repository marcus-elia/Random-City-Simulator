package prequel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter
{
	private Handler handler;
	
	public KeyInput(Handler handler)
	{
		this.handler = handler;
	}
	
    public void keyPressed(KeyEvent e)
    {
    	int key = e.getKeyCode();
    	
    	for(int i = 0; i < handler.objects.size(); i++)
    	{
    		GameObject temp = handler.objects.get(i);
    		
    		double scrollSpeed = 8.0;
    		if(temp.getID() == ID.Map)
    		{
    			if(key == KeyEvent.VK_UP)
    			{
    				temp.moveDown(scrollSpeed);
    			}
    			if(key == KeyEvent.VK_DOWN)
    			{
    				temp.moveUp(scrollSpeed);
    			}
    			if(key == KeyEvent.VK_LEFT)
    			{
    				temp.moveRight(scrollSpeed);
    			}
    			if(key == KeyEvent.VK_RIGHT)
    			{
    				temp.moveLeft(scrollSpeed);
    			}
    		}
    	}
    }
    
    public void keyReleased(KeyEvent e)
    {
    	int key = e.getKeyCode();
    }
	
}
