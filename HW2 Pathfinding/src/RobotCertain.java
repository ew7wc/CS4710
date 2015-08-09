import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import world.Robot;
import world.World;

public class RobotCertain extends Robot {

	/**
	 * @param args
	 */
	private static World myWorld;
	private static RobotCertain WallE;
	private static double w_g = 1;
	private static double w_h = 1;
	private static int x, y = 0;
	private static char[][] trueWorld;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			myWorld = new World("8.txt", false);
			WallE = new RobotCertain();
			WallE.addToWorld(myWorld);
		
			y = myWorld.numCols();
			x = myWorld.numRows();
			trueWorld = new char [x][y];

			//Ping every spot and build map
			
			for (int r = 0; r < x; r++) {
				for (int c = 0; c < y; c++) {
					
					trueWorld[r][c] = WallE.pingMap(new Point(r,c)).charAt(0);
				}	
			}
			
			WallE.travelToDestination();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void travelToDestination() {
		ArrayList<Node> path = getPathToDestination();
		for (int i = path.size() - 1; i >= 0; i--) {
			Point p = WallE.move(new Point(path.get(i).xCor, path.get(i).yCor));
			
		}
	}

	// A* algorithm
	public ArrayList<Node> getPathToDestination() {
		y = myWorld.numCols();
		x = myWorld.numRows();

		Point startPt = myWorld.getStartPos();
		Point endPt = myWorld.getEndPos();
		Node start = new Node(startPt.x, startPt.y, 0, 0);
		Node end = new Node(endPt.x, endPt.y, 0, 0);

		start.parent = start;

		// distance from start node to finish node
		double startToFinish = dist(start.xCor, start.yCor, end.xCor, end.yCor);
		double fxStart = fx(w_g, 0, w_h, startToFinish);
		start.fx = fxStart;

		ArrayList<Node> open = new ArrayList<Node>();
		ArrayList<Node> closed = new ArrayList<Node>();

		// 1) add the starting square to the open list.
		// 2) a) Look for lowest F cost square on the open list.
		// (Current square)
		// b) Switch it to the closed list.

		// find adjacent nodes to start
		closed.add(start);

		// Get adjacent unvisited nodes to start
		// Add these nodes to the open unexplored list
		ArrayList<Node> adj = new ArrayList<Node>();
		adj = retAdjNodes(start, end, 0);
		for (int i = 0; i<adj.size(); i++) {
			adj.get(i).parent = start;
		}
		open.addAll(adj);
		NodeComparator order = new NodeComparator();
		Collections.sort(open, order);

		while (!open.isEmpty()) {

			// remove the smallest node w/ smallest fx
			// take it off the open list and put it in the closed
			// list

			// resort to find lowest fx
			Collections.sort(open, order);
			Node lowestFx = open.get(0);

			// if that node you just put on closed is F, then we are
			// done.
			char spot = trueWorld[lowestFx.xCor][lowestFx.yCor];
			if (spot == 'F') {
				end.parent = lowestFx;
				end.gx = lowestFx.gx;
				// then we're done
				break;
			}
			open.remove(open.get(0));
			closed.add(lowestFx);

			// System.out.println("Route: "
			// + lowestFx.xCor + "," + lowestFx.yCor + " " +
			// lowestFx.fx);
			//
			// find more adjacent nodes
			ArrayList<Node> adjNodes = new ArrayList<Node>();
			adjNodes = retAdjNodes(lowestFx, end, (int) lowestFx.gx);

			// c) For each 8 sq adjacent to this square
			// i) if it's not walkable or if it is on closed list,
			// ignore it
			// ii) if it isn't on open list, add it to open. Make
			// current square parent of this square. Record F, G, H
			// costs of the square.
			// iii) if it is on the open list already, check to see
			// if path to that square is better, using G cost as
			// measure. Recalc G and F of that square
			// d) STOP when you add target square to CLOSED list,

			for (int count = 0; count < adjNodes.size(); count++) {

				if (closed.contains(adjNodes.get(count))) {
					continue;
				}
				// if open doesn't contain one of the adjacent
				// nodes, add it to open.
				else if (!open.contains(adjNodes.get(count))) {
					adjNodes.get(count).parent = lowestFx;
					open.add(adjNodes.get(count));
				}

				// if open contains temp, iterate through open and
				// find the the equivalent node
				else if (open.contains(adjNodes.get(count))) {
					for (int a = 0; a < open.size(); a++) {

						// find the equivalent node and compare its
						// G values
						if (adjNodes.get(count).equals(open.get(a))) {
							// if the Gx value of the node the
							// adjacent nodes list is less than
							// the one in open, update its value and
							// recalculate fx.
							if (adjNodes.get(count).gx < open.get(a).gx + 1) {

								open.get(a).parent = lowestFx;
								// reset Gx to the node in open list
								// recalculate Fx for the node in
								// open.
								open.get(a).gx = adjNodes.get(count).gx;

								open.get(a).fx = fx(
										w_g,
										open.get(a).gx,
										w_h,
										dist(open.get(a).xCor,
												open.get(a).yCor, end.xCor,
												end.yCor));
							}
						}
					}
				}
			}
		}
		// System.out.println((int) end.gx);

		ArrayList<Node> path = new ArrayList<Node>();

		path.add(end);
		Node p = end.parent;
		while ((p.xCor != start.xCor) || (p.yCor != start.yCor)) {
			path.add(p);
			p = p.parent;

		}
		// System.out.println(path.toString());

		return path;
	}

	public static double dist(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2));
	}

	// g(x) = integer number of squares traversed
	// h(x) = distance formula
	public static double fx(double w_g, double g_x, double w_h, double h_x) {
		return w_g * g_x + w_h * h_x;
	}

	// separate method to return an array of adjacent nodes
	public static ArrayList<Node> retAdjNodes(Node s, Node f, int path) {

		ArrayList<Node> retArray = new ArrayList<Node>();

		// check north
		if ((s.xCor - 1) >= 0
				&& (trueWorld[s.xCor - 1][ s.yCor] != 'X')) {
			double currToFinish = dist(s.xCor - 1, s.yCor, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor - 1, s.yCor, fxCurr, path + 1));
		}

		// check northeast
		if (((s.xCor - 1) >= 0)
				&& ((s.yCor + 1) < y)
				&& (trueWorld[s.xCor - 1][s.yCor + 1] != 'X')) {
			double currToFinish = dist(s.xCor + 1, s.yCor + 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor - 1, s.yCor + 1, fxCurr, path + 1));
		}

		// check east
		if (((s.yCor + 1) < y)
				&& (trueWorld[s.xCor][ s.yCor + 1] != 'X')) {
			double currToFinish = dist(s.xCor, s.yCor + 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor, s.yCor + 1, fxCurr, path + 1));
		}

		// check southeast
		if (((s.xCor + 1) < x)
				&& ((s.yCor + 1) < y)
				&& (trueWorld[s.xCor + 1][s.yCor + 1] != 'X')) {
			double currToFinish = dist(s.xCor + 1, s.yCor + 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor + 1, s.yCor + 1, fxCurr, path + 1));
		}

		// check south
		if (((s.xCor + 1) < x)
				&& (trueWorld[s.xCor + 1][s.yCor] != 'X')) {
			double currToFinish = dist(s.xCor + 1, s.yCor, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor + 1, s.yCor, fxCurr, path + 1));
		}

		// check southwest
		if (((s.xCor + 1) < x)
				&& ((s.yCor - 1) >= 0)
				&& (trueWorld[s.xCor + 1][s.yCor - 1] != 'X')) {
			double currToFinish = dist(s.xCor + 1, s.yCor - 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor + 1, s.yCor - 1, fxCurr, path + 1));
		}

		// check west
		if (((s.yCor - 1) >= 0)
				&& (trueWorld[s.xCor][ s.yCor - 1] != 'X')) {
			double currToFinish = dist(s.xCor, s.yCor - 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor, s.yCor - 1, fxCurr, path + 1));
		}

		// check northwest
		if (((s.xCor - 1) >= 0)
				&& ((s.yCor - 1) >= 0)
				&& (trueWorld[s.xCor - 1][ s.yCor - 1] != 'X')) {
			double currToFinish = dist(s.xCor - 1, s.yCor - 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor - 1, s.yCor - 1, fxCurr, path + 1));
		}

		return retArray;
	}

}
