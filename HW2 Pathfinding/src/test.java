import java.awt.Point;

import world.World;


public class test {
	
	private static World myWorld;
	private static RobotUncertain WallE;
	private static int numR = 0;
	private static int numC = 0;
	private static char pingedMap[][];
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			myWorld = new World("1.txt", false);
			WallE = new RobotUncertain();
			WallE.addToWorld(myWorld); 
			numR = myWorld.numRows();
			numC = myWorld.numCols();
			pingedMap = new char[numR][numC];
			for (int r = 0; r<myWorld.numRows(); r++) {
				for (int c = 0; c <myWorld.numCols(); c++) {
					pingedMap[r][c] = WallE.pingMap(new Point(r,c)).charAt(0);
				}
			}
			//pingedMap[myWorld.getStartPos().x][myWorld.getStartPos().y] = 'S';
			//pingedMap[myWorld.getEndPos().x][myWorld.getEndPos().y] = 'F';
			
			//WallE.travelToDestination();
			for (int r = 0; r<myWorld.numRows(); r++) {
				for (int c = 0; c <myWorld.numCols(); c++) {

					System.out.print(pingedMap[r][c]);
				}
				System.out.println();
			}
			System.out.println(WallE.move(new Point(0,1)));
			System.out.println(WallE.getPosition());
			System.out.println(WallE.getX());
			Node n = new Node(WallE.getPosition().x, WallE.getPosition().y,0,0);
			System.out.println(n.xCor);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void travelToDestination() {
	
	}

}
