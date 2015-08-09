//Elaine Wu (ew7wc) and Ashley Schoen (ams5da)
//Date: 3/4/15

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import world.Robot;
import world.World;

public class RobotUncertain extends Robot {

	/**
	 * @param args
	 */
	private static World myWorld;
	private static RobotUncertain WallE;
	private static double w_g = 1;
	private static double w_h = 1;
	private static int r, c = 0;
	private static MapPt[][] pingedMap;
	private static int radius = 0;
	private static int numR = 0;
	private static int numC = 0;
	private static int knownSpaces = 0;
	private static Queue<Node> last3 = new ArrayDeque<Node>();

	public static class MapPt {
		char val = ' ';
		double prob = -1;

		public MapPt(char val, double prob) {
			this.val = val;
			this.prob = prob;
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			myWorld = new World("9.txt", true);
			WallE = new RobotUncertain();
			WallE.addToWorld(myWorld);
			numR = myWorld.numRows();
			numC = myWorld.numCols();
			radius = radiusGenerator(numR, numC);
			//System.out.println(radius);
			pingedMap = new MapPt[numR][numC];
			for (int r = 0; r < myWorld.numRows(); r++) {
				for (int c = 0; c < myWorld.numCols(); c++) {
					pingedMap[r][c] = new MapPt('O', 0.0);
				}
			}
			pingedMap[myWorld.getStartPos().x][myWorld.getStartPos().y] = new MapPt(
					'S', 1);
			pingedMap[myWorld.getEndPos().x][myWorld.getEndPos().y] = new MapPt(
					'F', 1);

			
			while (true) 
				WallE.travelToDestination();
			 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void travelToDestination() {

		/*
		 * System.out.println(dist(WallE.getX(), WallE.getY(),
		 * myWorld.getEndPos().x, myWorld.getEndPos().y));
		 */
		if (dist(WallE.getX(), WallE.getY(), myWorld.getEndPos().x,
				myWorld.getEndPos().y) < radius) {
			ArrayList<Node> path = getPathToDestination(WallE.getPosition(),
					myWorld.getEndPos());
			Point p = null;

			for (int i = path.size() - 1; i >= 0; i--) {
				Point loc = WallE.getPosition();
				p = WallE.move(new Point(path.get(i).xCor, path.get(i).yCor));

				// fill in our map
				// WallE did not move
				if (p.equals(loc)) {
					
					 /*System.out.println("WallE did not move " +
					 WallE.getPosition());
					 */
					pingedMap[path.get(i).xCor][path.get(i).yCor] = new MapPt(
							'X', 1.0);
					/*
					 * System.out.println("WallE's current pos: " +
					 * WallE.getPosition()); System.out.println();
					 */
					if (pingedMap[p.x][p.y].prob != 1) {
						pingedMap[p.x][p.y] = new MapPt('O', 1.0);
					}
					break;

				} else {
					//System.out.println("WallE moved" + WallE.getPosition());
					if (pingedMap[p.x][p.y].prob != 1) {
						pingedMap[p.x][p.y] = new MapPt('O', 1.0);
					}
				}
				/*
				 * for (int r = 0; r < myWorld.numRows(); r++) { for (int c = 0;
				 * c < myWorld.numCols(); c++) {
				 * System.out.print(pingedMap[r][c].val); }
				 * System.out.println(); }
				 */
			}
		} else {
			PriorityQueue<Node> pots = getMiniFinishes();
			NodeComparator n = new NodeComparator();
			PriorityQueue<Node> mf = new PriorityQueue<Node>(8, n);


			// run A* on all mini finishes to calculate g(x)
			for (int i = 0; i < pots.size(); i++) {
				Node tmp = pots.remove();
				tmp.fx += getGx(WallE.getPosition(), new Point(tmp.xCor,
						tmp.yCor));
				mf.add(tmp);

			}
			// System.out.println("MF: " + mf.toString());
			Node temp = mf.remove(); // remove the one with the lowest fx.

			mf.clear();
			Point p = null;
			ArrayList<Node> path = getPathToDestination(WallE.getPosition(),
					new Point(temp.xCor, temp.yCor));
			/*
			 * System.out.println("WallE's potential path: walle is @" +
			 * WallE.getPosition());
			 */

			/*
			 * for (int i = path.size() - 1; i >= 0; i--) {
			 * System.out.println(path.get(i).toString()); }
			 */

			for (int i = path.size() - 1; i >= 0; i--) {
				Point loc = WallE.getPosition();
				p = WallE.move(new Point(path.get(i).xCor, path.get(i).yCor));

				// fill in our map
				// WallE did not move
				if (p.equals(loc)) {
					
					 /*System.out.println("WallE did not move " +
					 WallE.getPosition());*/
					
					pingedMap[path.get(i).xCor][path.get(i).yCor] = new MapPt(
							'X', 1.0);
					/*
					 * System.out.println("WallE's current pos: " +
					 * WallE.getPosition()); System.out.println();
					 */
					if (pingedMap[p.x][p.y].prob != 1) {
						pingedMap[p.x][p.y] = new MapPt('O', 1.0);
					}
					break;

				} else {
					//System.out.println("WallE moved" + WallE.getPosition());
					if (pingedMap[p.x][p.y].prob != 1) {
						pingedMap[p.x][p.y] = new MapPt('O', 1.0);
					}
				}
				/*
				 * for (int r = 0; r < myWorld.numRows(); r++) { for (int c = 0;
				 * c < myWorld.numCols(); c++) {
				 * System.out.print(pingedMap[r][c].val); }
				 * System.out.println(); }
				 */
			}
		}
		// System.out.println();
		// System.out.println(WallE.getPosition().toString());
		/*
		 * for (int r = 0; r < myWorld.numRows(); r++) { for (int c = 0; c <
		 * myWorld.numCols(); c++) { System.out.print(pingedMap[r][c].val); }
		 * System.out.println(); }
		 */
	}

	// A* algorithm
	public ArrayList<Node> getPathToDestination(Point s, Point f) {
		/*
		 * System.out.println("Start: " + s.toString());
		 * System.out.println("Finish: " + f.toString()); System.out.println();
		 */
		c = myWorld.numCols();
		r = myWorld.numRows();

		// Point startPt = myWorld.getStartPos();
		// Point endPt = myWorld.getEndPos();
		Node start = new Node(s.x, s.y, 0, 0);
		Node end = new Node(f.x, f.y, 0, 0);

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
		for (int i = 0; i < adj.size(); i++) {
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
			if ((f.x == lowestFx.xCor) && (f.y == lowestFx.yCor)) {
				end.parent = lowestFx;
				end.gx = lowestFx.gx;
				end.parent = lowestFx.parent;
				// then we're done
				break;
			}
			open.remove(open.get(0));
			closed.add(lowestFx);

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
				/*
				 * else if (adjNodes.get(count).xCor == end.xCor &&
				 * adjNodes.get(count).yCor == end.yCor) { end.fx =
				 * adjNodes.get(count).fx; end.gx = adjNodes.get(count).gx;
				 * break; }
				 */
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
		// end.parent =
		Node p = end;

		while ((p.xCor != start.xCor) || (p.yCor != start.yCor)) {

			path.add(p);

			p = p.parent;

		}

		return path;
	}

	public static double getGx(Point s, Point f) {
		c = myWorld.numCols();
		r = myWorld.numRows();

		// Point startPt = myWorld.getStartPos();
		// Point endPt = myWorld.getEndPos();
		Node start = new Node(s.x, s.y, 0, 0);
		Node end = new Node(f.x, f.y, 0, 0);

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
		for (int i = 0; i < adj.size(); i++) {
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
			if ((f.x == lowestFx.xCor) && (f.y == lowestFx.yCor)) {
				end.parent = lowestFx;
				end.gx = lowestFx.gx;
				end.parent = lowestFx.parent;
				// then we're done
				break;
			}
			open.remove(open.get(0));
			closed.add(lowestFx);

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

		return end.gx;
	}

	public static double dist(double x1, double y1, double x2, double y2) {
		if (x1 < 0 || y1 < 0)
			return -1;
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

		// we will only change a node on our pingedmap to X if we know with 100%
		// certainty that it is an X
		// check north
		if ((s.xCor - 1) >= 0 && (pingedMap[s.xCor - 1][s.yCor].val != 'X')) {
			double currToFinish = dist(s.xCor - 1, s.yCor, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor - 1, s.yCor, fxCurr, path + 1));
		} else if (s.xCor - 1 >= 0
				&& (f.xCor == s.xCor - 1 && f.yCor == s.yCor)) {
			retArray.add(f);
			return retArray;
		}

		// check northeast
		if (((s.xCor - 1) >= 0) && ((s.yCor + 1) < c)
				&& (pingedMap[s.xCor - 1][s.yCor + 1].val != 'X')) {
			double currToFinish = dist(s.xCor + 1, s.yCor + 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor - 1, s.yCor + 1, fxCurr, path + 1));
		} else if (s.xCor - 1 >= 0 && (s.yCor + 1 < c)
				&& (f.xCor == s.xCor - 1 && f.yCor == s.yCor + 1)) {
			retArray.add(f);
			return retArray;
		}

		// check east
		if (((s.yCor + 1) < c) && (pingedMap[s.xCor][s.yCor + 1].val != 'X')) {
			double currToFinish = dist(s.xCor, s.yCor + 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor, s.yCor + 1, fxCurr, path + 1));
		}

		// check southeast
		if (((s.xCor + 1) < r) && ((s.yCor + 1) < c)
				&& (pingedMap[s.xCor + 1][s.yCor + 1].val != 'X')) {
			double currToFinish = dist(s.xCor + 1, s.yCor + 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor + 1, s.yCor + 1, fxCurr, path + 1));
		}

		// check south
		if (((s.xCor + 1) < r) && (pingedMap[s.xCor + 1][s.yCor].val != 'X')) {
			double currToFinish = dist(s.xCor + 1, s.yCor, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor + 1, s.yCor, fxCurr, path + 1));
		}

		// check southwest
		if (((s.xCor + 1) < r) && ((s.yCor - 1) >= 0)
				&& (pingedMap[s.xCor + 1][s.yCor - 1].val != 'X')) {
			double currToFinish = dist(s.xCor + 1, s.yCor - 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor + 1, s.yCor - 1, fxCurr, path + 1));
		}

		// check west
		if (((s.yCor - 1) >= 0) && (pingedMap[s.xCor][s.yCor - 1].val != 'X')) {
			double currToFinish = dist(s.xCor, s.yCor - 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor, s.yCor - 1, fxCurr, path + 1));
		}

		// check northwest
		if (((s.xCor - 1) >= 0) && ((s.yCor - 1) >= 0)
				&& (pingedMap[s.xCor - 1][s.yCor - 1].val != 'X')) {
			double currToFinish = dist(s.xCor - 1, s.yCor - 1, f.xCor, f.yCor);
			double fxCurr = fx(w_g, path + 1, w_h, currToFinish);
			retArray.add(new Node(s.xCor - 1, s.yCor - 1, fxCurr, path + 1));
		}

		return retArray;
	}

	public static PriorityQueue<Node> getMiniFinishes() {
		int tmp = radius;
		radius = radiusRandomizer();
		NodeComparator nc = new NodeComparator();
		PriorityQueue<Node> miniFinishes = new PriorityQueue<Node>(8, nc);

		ArrayList<Double> dists = new ArrayList<Double>();
		dists.add(dist(WallE.getX() - radius, WallE.getY(),
				myWorld.getEndPos().x, myWorld.getEndPos().y));
		dists.add(dist(WallE.getX() - radius, WallE.getY() + radius,
				myWorld.getEndPos().x, myWorld.getEndPos().y));
		dists.add(dist(WallE.getX(), WallE.getY() + radius,
				myWorld.getEndPos().x, myWorld.getEndPos().y));
		dists.add(dist(WallE.getX() + radius, WallE.getY() + radius,
				myWorld.getEndPos().x, myWorld.getEndPos().y));
		dists.add(dist(WallE.getX() + radius, WallE.getY(),
				myWorld.getEndPos().x, myWorld.getEndPos().y));
		dists.add(dist(WallE.getX() + radius, WallE.getY() - radius,
				myWorld.getEndPos().x, myWorld.getEndPos().y));
		dists.add(dist(WallE.getX(), WallE.getY() - radius,
				myWorld.getEndPos().x, myWorld.getEndPos().y));
		dists.add(dist(WallE.getX() - radius, WallE.getY() - radius,
				myWorld.getEndPos().x, myWorld.getEndPos().y));

		// Create your mini finishes and put them in a min heap
		// only create nodes for the points that are on the map

		// north
		if ((WallE.getX() - radius) >= 0) {
			int x = WallE.getX() - radius;
			int y = WallE.getY();
			Node north = new Node(x, y, dists.get(0), 0);
			if (pingedMap[x][y].val != 'X') {
				miniFinishes.add(north);
			}

		} else {
			if (WallE.getPosition().x != 0) {
				Node north = new Node(0, WallE.getY(), dist(0, WallE.getY(),
						myWorld.getEndPos().x, myWorld.getEndPos().y), 0);
				if (pingedMap[0][WallE.getY()].val != 'X') {
					miniFinishes.add(north);
				}
			}
		}

		// north east
		if ((WallE.getX() - radius >= 0) && (WallE.getY() + radius < numC)) {
			Node north_east = new Node((WallE.getX() - radius), WallE.getY()
					+ radius, dists.get(1), 0);
			if (pingedMap[WallE.getX() - radius][WallE.getY() + radius].val != 'X') {
				miniFinishes.add(north_east);
			}

		} else {
			if (WallE.getPosition().x != 0 && WallE.getPosition().y != numC - 1) {
				Node north_east = new Node(0, WallE.getY(), dist(0,
						WallE.getY(), myWorld.getEndPos().x,
						myWorld.getEndPos().y), 0);
				if (pingedMap[0][WallE.getY()].val != 'X') {
					miniFinishes.add(north_east);
				}
			}
		}

		// east
		if (WallE.getY() + radius < numC) {
			Node east = new Node((WallE.getX()), WallE.getY() + radius,
					dists.get(2), 0);
			if (pingedMap[WallE.getX()][WallE.getY() + radius].val != 'X') {
				miniFinishes.add(east);
			}
		} else {
			if (WallE.getPosition().y != numC - 1) {
				Node east = new Node(WallE.getX(), numC - 1,
						dist(WallE.getX(), numC - 1, myWorld.getEndPos().x,
								myWorld.getEndPos().y), 0);
				if (pingedMap[WallE.getX()][numC - 1].val != 'X') {
					miniFinishes.add(east);
				}
			}
		}

		// south east
		if ((WallE.getX() + radius < numR) && (WallE.getY() + radius < numC)) {
			Node south_east = new Node((WallE.getX() + radius), WallE.getY()
					+ radius, dists.get(3), 0);
			if (pingedMap[WallE.getX() + radius][WallE.getY() + radius].val != 'X') {
				miniFinishes.add(south_east);
			}
		} else {
			if (WallE.getPosition().x != numR - 1
					&& WallE.getPosition().y != numC - 1) {
				Node south_east = new Node(WallE.getX(), numC - 1, dist(
						WallE.getX(), numC - 1, myWorld.getEndPos().x,
						myWorld.getEndPos().y), 0);
				if (pingedMap[WallE.getX()][numC - 1].val != 'X') {
					miniFinishes.add(south_east);
				}
			}
		}

		// south
		if (WallE.getX() + radius < numR) {
			Node south = new Node((WallE.getX() + radius), WallE.getY(),
					dists.get(4), 0);
			if (pingedMap[WallE.getX() + radius][WallE.getY()].val != 'X') {
				miniFinishes.add(south);
			}
		} else {
			if (WallE.getPosition().x != numR - 1) {
				Node south = new Node(numR - 1, WallE.getY(), dist(numR - 1,
						WallE.getY(), myWorld.getEndPos().x,
						myWorld.getEndPos().y), 0);
				if (pingedMap[numR - 1][WallE.getY()].val != 'X') {
					miniFinishes.add(south);
				}
			}
		}

		// south west
		if ((WallE.getX() + radius < numR) && (WallE.getY() - radius >= 0)) {
			Node south_west = new Node((WallE.getX() + radius), WallE.getY()
					- radius, dists.get(5), 0);
			if (pingedMap[WallE.getX() + radius][WallE.getY() - radius].val != 'X') {
				miniFinishes.add(south_west);
			}
		} else {
			if (WallE.getPosition().x != numR - 1 && WallE.getPosition().y != 0) {
				Node south_west = new Node(numR - 1, WallE.getY(), dist(
						numR - 1, WallE.getY(), myWorld.getEndPos().x,
						myWorld.getEndPos().y), 0);
				if (pingedMap[numR - 1][WallE.getY()].val != 'X') {
					miniFinishes.add(south_west);
				}
			}
		}

		// west
		if (WallE.getY() - radius >= 0) {
			Node west = new Node((WallE.getX()), WallE.getY() - radius,
					dists.get(6), 0);
			if (pingedMap[WallE.getX()][WallE.getY() - radius].val != 'X') {
				miniFinishes.add(west);
			}
		} else {
			if (WallE.getPosition().y != 0) {
				Node west = new Node(WallE.getX(), 0, dist(WallE.getX(), 0,
						myWorld.getEndPos().x, myWorld.getEndPos().y), 0);
				if (pingedMap[WallE.getX()][0].val != 'X') {
					miniFinishes.add(west);
				}
			}
		}

		// north west
		if ((WallE.getX() - radius >= 0) && (WallE.getY() - radius >= 0)) {
			Node north_west = new Node((WallE.getX() - radius), WallE.getY()
					- radius, dists.get(7), 0);
			if (pingedMap[WallE.getX() - radius][WallE.getY() - radius].val != 'X') {
				miniFinishes.add(north_west);
			}
		} else {
			if (WallE.getPosition().x != 0 && WallE.getPosition().y != 0) {
				Node north_west = new Node(WallE.getX(), 0, dist(WallE.getX(),
						0, myWorld.getEndPos().x, myWorld.getEndPos().y), 0);
				if (pingedMap[WallE.getX()][0].val != 'X') {
					miniFinishes.add(north_west);
				}
			}
		}
		radius = tmp;
		return miniFinishes;
	}

	public static int radiusRandomizer() {
		int min = radius - 1;
		int max = radius + 2;
		Random rand = new Random();
		int randNumber = rand.nextInt((max - min) + 1) + min;
		return randNumber;

	}

	public static int radiusGenerator(double r, double c) {

		double p = 0;
		double rad = 0;
		double m = Math.abs((c - r) / ((r + c) / 2));

		//System.out.println(m);

		// map is a square or drastic rectangle
		if (m <= 0.2) {
			p = 0.8;
		} else if (m > 0.2 && m <= 0.5) {
			p = 0.4;
		} else if (m > 0.5 && m <= 0.9) {
			p = 0.6;

		} else if (m > 0.9 && m < 3.0) {
			p = 0.8;
		} else {
			p = 0.5;
		}

		rad = Math.max(r, c) * p;
		return (int) rad;

	}
}
