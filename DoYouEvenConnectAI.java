import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class DoYouEvenConnectAI extends CKPlayer {

	public DoYouEvenConnectAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "DoYouEvenConnectAI";
	}
	
	public byte oppPlayer(byte b) {
		if (b == 1)
			return (byte) 2;
		return (byte) 1;
	}
	
	@Override
	public Point getMove(BoardModel state) {		
		if (state.getLastMove() == null) {
			return new Point(state.getWidth()/2, state.getHeight()/2);
		}
		else {
			Point move;
			if(state.gravityEnabled()){
				move = mmsearch(state, 4);
			}
			else{
				move = mmsearch(state, 2);
			}
//			System.out.println(move);
			return move; // temporary
		}
	}
	
	public int h(BoardModel state) {
		//System.out.println("player 1 (R)= " + countWins(state, player));
		//System.out.println("player 2 (B)= " + countWins(state, oppPlayer(player)));
		return countWins(state, player) - countWins(state, oppPlayer(player));
	}
	
	public int countWins(BoardModel state, byte player) {
		// loop through various positions on the board and count the number of wins
		// currently goes through all pieces so works when gravity is on and off. 
		int count = 0;
		int width = state.getWidth();
		int height = state.getHeight();
		for (int w = 0; w < width; w++)  {
			for (int h = 0; h < height; h++) {
//				System.out.println("At position (" + w + ", " + h + ")");
				count += checkAllDir(w, h, state, player);
			}
		}
		return count; 
	}
	
	public int checkAllDir(int x, int y, BoardModel state, byte player){
		// given current position, check all the directions and return the # of possible wins (how many passed)
		int count = 0;
		Point[] dir = {new Point(0,1), new Point(1,0), new Point(1,1), new Point(-1,-1),
				new Point(-1,0), new Point(0,-1), new Point(-1,1),  new Point(1,-1)};
		// loop through all the directions
		for (int i = 0; i < 4; i++){
			if (checkDir(x, y, state, player, dir[i]))
				count += 1;				
//			Point[] dir = {new Point(j,0), new Point(-j,0), new Point(j,j), new Point(-j,j), 
//						new Point(-j,-j), new Point(j,-j), new Point(0,j), new Point(0,-j)};
//			int[][] dir = {{j,0}, {-j,0}, {j, j}, {-j,j}, {-j,-j}, {j,-j}, {0,j}, {0,-j}};
		}
		return count;
	}
	
	public boolean checkDir(int x, int y, BoardModel state, byte player, Point p) {
		// checks a particular direction
		int width = state.getWidth();
		int height = state.getHeight();
		int k = state.kLength;
		// think there's a problem here when dealing with negative directions (cuz adding and sub result differently?)
		int currPosX = x - p.x;
		int currPosY = y - p.y;
		for(int j = 0; j < k; j++) {
			currPosX += p.x;
			currPosY += p.y;
//			System.out.println("\t\tCheckedPos: " + Integer.toString(currPosX) + ","+Integer.toString(currPosY));
			// check that it doesn't go off the 4 edges of the board
			if (currPosX < 0 || currPosY >= height || currPosX >= width || currPosY < 0) {
//				System.out.println("\t-----------------------------------");
				return false;
			}
			// if space is not blank or is opponent's then stop checking
			if (state.getSpace(currPosX, currPosY) != 0 && state.getSpace(currPosX , currPosY) != player){
//				System.out.println("\t-----------------------------------");
				return false;
			}
//			System.out.println("Move being checked: " + Integer.toString(currPosX) + ","+Integer.toString(currPosY));
		}
//		System.out.println("\tDirection Passed: " + p.x + ", " + p.y); // should print if this direction passed!
		return true;
	}
	
	public List<Point> possiblePoints(BoardModel state){
    	List<Point> p = new ArrayList<Point> (state.getWidth() * state.getHeight());
    	if (state.gravityEnabled()) {
	        for (int i = 0; i < state.getWidth(); i++) {
	        	for (int j = 0; j < state.getHeight(); j++) {
	        		if (state.getSpace(i,j) == 0 ) {
	        			p.add(new Point(i,j));
	        			break;
		        	}
	        	}
	        }
        } else {
        	for (int i = 0; i < state.getWidth(); i++) {
	        	for (int j = 0; j < state.getHeight(); j++) {
	        		if (state.getSpace(i,j) == 0 ) {
	        			p.add(new Point(i,j));
		        	}
	        	}
	        }
        }
    	return p;
    }
	
	public Point mmsearch(BoardModel state, int depth) {
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		List<Point> p = possiblePoints(state);
		int maxNumber = Integer.MIN_VALUE;
		Point maxP = p.get(0);
		BoardModel originalState = state.clone();
		for(int i = 0; i < p.size(); i++){
			state = originalState.placePiece(p.get(i), player);
			//state = state.placePiece(p.get(i), player);
			int num = minValue(state, depth, alpha, beta);
			if(num > maxNumber){
				maxNumber = num;
				maxP = p.get(i);
//				System.out.println("This is maxP: ");
//				System.out.println(maxNumber);
			}
		}
		return maxP;
		
	}
	
	public int maxValue(BoardModel state, int depth, int alpha, int beta){
		if(depth == 0){
			int h = h(state);
			if (h > alpha) {
				alpha = h;
			}
			return h;
		}
		int v = Integer.MIN_VALUE;
		List<Point> p = possiblePoints(state);
		BoardModel originalState = state.clone();
		for(int i = 0; i < p.size(); i++){
			if (v > alpha) {
				alpha = v;
			}
			if (alpha >= beta) {
				break;
			}
			state = originalState.placePiece(p.get(i), player);
			v = Math.max(v, minValue(state, depth-1, alpha, beta));
		}
		return v;
	}
	
	public int minValue(BoardModel state, int depth, int alpha, int beta){
		if(depth == 0){
			int h = h(state);
			if (h > alpha) {
				alpha = h;
			}
			return h;
		}
		int v = Integer.MAX_VALUE;
		List<Point> p = possiblePoints(state);
		BoardModel originalState = state.clone();
		for(int i = 0; i < p.size(); i++){
			if (v < beta) {
				beta = v;
			}
			if (alpha >= beta) {
				break;
			}
			state = originalState.placePiece(p.get(i), oppPlayer(player));
			v = Math.min(v, maxValue(state, depth-1, alpha, beta));
		}
		return v;
	}
	
	
	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}
}
