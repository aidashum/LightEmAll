import java.awt.Color;

import javalib.funworld.WorldScene;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.WorldImage;
import tester.Tester;

 class ColorCC {

	WorldScene yellowScene = new WorldScene(10, 10);
	public void colorC(World) {
		 WorldImage connector = new RectangleImage(2, 2,"solid", Color.YELLOW);
		yellowScene.placeImageXY(connector, 2, 2);
	}
	
	

}

class testColor{
	void testColor(Tester t) {
		ColorCC yel = new ColorCC();
		yel.colorC();
		
	}
}