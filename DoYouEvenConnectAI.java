import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.*;

public class DoYouEvenConnectAI extends CKPlayer {
	private long startTime;
	private long estimatedTime;
	
//	private LinkedHashMap<BoardModel,Point> stateAndMoves = new LinkedHashMap<BoardModel, Point>();
	public DoYouEvenConnectAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "DoYouEvenConnectAI";
	}

	public byte oppPlayer(byte b) {
		if (b == 1)
			return (byte) 2;
		return (byte) 1;
	}

	/* 
	 * IDS/Sorting AI must run on openlab.ics.uci.edu in the tournament shell 
	 * and extend Alpha-beta AI to include IDS search up to the time limit 
	 * plus sort moves based on the last IDS iteration to put the most favorable moves first for A/B pruning
	 * 
	 * HINTS
	 * http://stackoverflow.com/questions/10985000/how-should-i-design-a-good-evaluation-function-for-connect-4
	 * you have the base cases ironed out: my win = 100 pts, my loss = -100, tie = 0. 
	 * The "unsure" case you can kill, it does not reflect the "goodness" of the board. 
	 * So now you need to fill in the gaps. Cases you want to consider and assign values to:
	 * I have X in a row (If i have 3 in a row, that's better than only two in a row - 
	 * your function should favor adding to longer rows over shorter ones)
	 * My opponent has X in a row (Likewise, the more he/she has in a row, the worse off we are)
	 * Count how many rows you are filling in (Placing a piece and forming 2 rows of 3 is better than placing a piece 
	 * and only forming one row of 3)
	 * Count how many rows you are blocking (similarly, if you can drop a piece and block two opponents rows of 3, 
	 * that's better than blocking a single row of 2)
	 */

	@Override
	public Point getMove(BoardModel state) {
		startTime = System.currentTimeMillis();
		if (state.getLastMove() == null) {
			return new Point(state.getWidth()/2, state.getHeight()/2);
		}
		else {
			Point move;
			if(state.gravityEnabled()){
				move = mmsearch(state, 4);
			}
			else{
				move = mmsearch(state, 4);
			}
			//			System.out.println(move);
			return move; // temporary
		}
	}

	public int h(BoardModel state) {
		//System.out.println("player 1 (R)= " + countWins(state, player));
		//System.out.println("player 2 (B)= " + countWins(state, oppPlayer(player)));
		return countWins(state, player) - countWins(state, oppPlayer(player)); // old heuristic
//		return countWins(state,player);
	}

	public int countWins(BoardModel state, byte p) {
		int count = 0;
		int width = state.getWidth();
		int height = state.getHeight();
		for(int w = 0; w< width; w++){
			for(int h = 0; h< height; h++){
				if(state.getSpace(w, h) == p){
//					System.out.println(w + ", " + h);
					count += checkAllDir(w, h, state, p);
//					System.out.println(count);
				}
			}
		}
		
		
//		List<Point> toAttack = attackOrDefend(state, oppPlayer(player));
//		List<Point> toDefend = attackOrDefend(state, player);
		
//		System.out.println(count);
		return count; 
	}

	public boolean blocked(int x, int y, BoardModel state, byte p, int inARow, Point dir){
		int originalX = x;
		int originalY = y;
		int height = state.getHeight();
		int width = state.getWidth();
		int k = state.kLength;
		int need = k - inARow;
		for(int i = 0; i < need; i++){	//checks points before
			x-= dir.x;
			y-= dir.y;
			if (x < 0 || y >= height || x >= width || y < 0){
				break;
			}
			if(state.getSpace(x,y) == oppPlayer(p)){
				return true;
			}
		}
		x = originalX;
		y = originalY;
		for(int i = 0; i < k; i++){
			x+= dir.x;
			y+= dir.y;
			if (x < 0 || y >= height || x >= width || y < 0){
				break;
			}
			if(state.getSpace(x,y) == oppPlayer(p)){
				return true;
			}
		}	
		return false;
	}

	public int checkAllDir(int x, int y, BoardModel state, byte p){
		int count = 0;
		Point[] dir = {new Point(0,1), new Point(1,0), new Point(1,1), new Point(-1,1),
				new Point(-1,0), new Point(0,-1), new Point(-1,-1),  new Point(1,-1)};
		int winLines = 0;

		// loop through all the directions
		for (int i = 0; i < 8; i++){
			int inARow =  consecutiveInARow(x, y, state, dir[i], p);
			if(winLine(x, y, p, dir[i], state)){
				count += 1;
				winLines += 1;
			}
			if(p == player){
				if(blocked(x, y, state, p, inARow, dir[i])){
					count += inARow;
				}
			}
		}
		return count;
	}
	
	public boolean winLine(int x, int y, byte p, Point dir, BoardModel state) {
		int width = state.getWidth();
		int height = state.getHeight();
		int k = state.kLength;
		int row = 1;
		int currPosX = x;
		int currPosY = y;
		for (int i = 0; i < k; i++) {
			currPosX += dir.x;
			currPosY += dir.y;
			if (currPosX < 0 || currPosY >= height || currPosX >= width || currPosY < 0) {
				break;
			}
			if (state.getSpace(currPosX, currPosY) == 0 || state.getSpace(currPosX, currPosY) == p) {
				row += 1;
			}
			if (state.getSpace(currPosX, currPosY) == oppPlayer(p)) {
				break;
			}
		}
		if (row != k) {
			currPosX = x;
			currPosY = y;
			for (int i = 0; i < (k - row); i++) {
				// flip the direction to be opposite
				currPosX -= dir.x;
				currPosY -= dir.y;
				if (currPosX < 0 || currPosY >= height || currPosX >= width || currPosY < 0) {
					break;
				}
				if (state.getSpace(currPosX, currPosY) == 0 || state.getSpace(currPosX, currPosY) == p) {
					row += 1;
				}
				if (state.getSpace(currPosX, currPosY) == oppPlayer(p)) {
					break;
				}
			}
			if (row != k)
				return false;
		}
		return true;
	}
	

	public int inARow(int x, int y, BoardModel state, Point dir, byte p){
		int height = state.getHeight();
		int width = state.getWidth();
		int inARow = 1;
		int kLength = state.kLength;
		for(int i = 0; i< kLength-1; i++){
			x+= dir.x;
			y+= dir.y;
			if (x < 0 || y >= height || x >= width || y < 0){
				break;
			}
			if(state.getSpace(x, y) == p){
				inARow += 1;

			}
			else if(state.getSpace(x,y) == oppPlayer(p)){
				break;
			}
		}
		return inARow;
		
	}
	
	public int consecutiveInARow(int x, int y, BoardModel state, Point dir, byte p){
		int height = state.getHeight();
		int width = state.getWidth();
		int inARow = 1;
		int kLength = state.kLength;
		for(int i = 0; i< kLength-1; i++){
			x+= dir.x;
			y+= dir.y;
			if (x < 0 || y >= height || x >= width || y < 0){
				break;
			}
			if(state.getSpace(x, y) == p){
				inARow += 1;

			}
			else{
				break;
			}
		}
		return inARow;
		
	}
	
	
	
	
	public List<Point> attackOrDefend(BoardModel state, byte p){
		int k = state.kLength;
		List<Point> possibleAttacks = new ArrayList<Point>();
		List<Point> pieces = getPlayerPieces(state, p);
		Point[] dir = {new Point(0,1), new Point(1,0), new Point(1,1), new Point(-1,1),
				new Point(-1,0), new Point(0,-1), new Point(-1,-1),  new Point(1,-1)};
		for(int i = 0; i < pieces.size(); i++){
			for(int j = 0; j < 4; j++){
				int x = pieces.get(i).x;
				int y = pieces.get(i).y;
				int connected = inARow(x, y, state, dir[j], p);
				//System.out.println("checking connected at "+ pieces.get(i) + " : " + connected);
				if(connected >= (k-2)){
					int need = k - connected;
					//System.out.println("connected: " + connected + " need: " + need);
					List<Point> temp = needPoints(x, y ,state, need, dir[j], connected, p);
					if(temp.size() >= need){
						possibleAttacks.addAll(temp);
					}
				}
			}
		}
		
		return possibleAttacks;
		
	}
	
	public void printList(List<Point> p){
		for(int i = 0; i < p.size(); i++){
			System.out.println(p.get(i));
		}
	}
	
	public List<Point> needPoints(int x, int y, BoardModel state, int need, Point dir, int connected, byte p){
		int originalX = x;
		int originalY = y;
		int height = state.getHeight();
		int width = state.getWidth();
		List<Point> possible = new ArrayList<Point>();
		for(int i = 0; i < need; i++){	//checks points before
			x-= dir.x;
			y-= dir.y;
			if (x < 0 || y >= height || x >= width || y < 0){
				break;
			}
			if(state.getSpace(x,y) == 0){
//				System.out.println("Direction: " + dir);
//				System.out.println("For this space " + originalX + ", " + originalY + ": " + x + "," +y);
				possible.add(new Point(x,y));
			}
			if(state.getSpace(x,y) == p){
				break;
			}
		}
		x = originalX;
		y = originalY;
		for(int i = 0; i < (need+connected); i++){
			x+= dir.x;
			y+= dir.y;
			if (x < 0 || y >= height || x >= width || y < 0){
				break;
			}
			if(state.getSpace(x,y) == 0){
//				System.out.println("Direction: " + dir);

//				System.out.println("For this space " + originalX + ", " + originalY + ": " + x + "," +y);
				possible.add(new Point(x,y));
			}
			if(state.getSpace(x,y) == p){
				break;
			}
			
		}	
		return possible;
	}
	


	public Point lastInARow(Point move, BoardModel state, Point direction){
		int x = move.x;
		int y = move.y;
		int kLength = state.kLength;
		int width = state.getWidth();
		int height = state.getHeight();
		for(int k = 0; k < kLength-2; k++){
			x += direction.x;
			y += direction.y;
			if (x < 0 || y >= height || x >= width || y < 0){
				break;
			}
			if(state.getSpace(x, y) != player){
				return move;
			}
		}
		return new Point(x,y);
	}
	public int success(int x, int y, BoardModel state, Point p) {
		// checks a particular direction
		int width = state.getWidth();
		int height = state.getHeight();
		int k = state.kLength;
		int currPosX = x;
		int currPosY = y;
		int value = 0;
		for(int j = 0; j < Integer.max(width, height); j++) {
			currPosX += p.x;
			currPosY += p.y;
			//System.out.println("currPosX, currPosY " + currPosX + " " + currPosY);
			//System.out.println("player_space" + state.getSpace(currPosX, currPosY));
			// if it goes off the 4 edges of the board then return p
			if (currPosX < 0 || currPosY >= height || currPosX >= width || currPosY < 0)
				return value;
			if (state.getSpace(currPosX, currPosY) == player)
				value += 1;
			if (state.getSpace(currPosX, currPosY) == oppPlayer(player))
				value -= 1;
			if (state.getSpace(currPosX, currPosY) == 0)
				break;
		}
		return value;
	}




	// Returns a list of a player's pieces on the board
	public List<Point> getPlayerPieces(BoardModel state, byte player) {
		List<Point> p = new ArrayList<Point> (state.getWidth() * state.getHeight());
		if (state.gravityEnabled()) {
			for (int i = 0; i < state.getWidth(); i++) {
				for (int j = 0; j < state.getHeight(); j++) {
					// if you hit an empty space with gravity enabled there's no more pieces higher (j++) than this one.
					if (state.getSpace(i,j) == 0)
						break;
					// find if space is occupied with player's piece
					if (state.getSpace(i,j) == player) {
						p.add(new Point(i,j));
					}
				}
			}
		}
		else {
			for (int i = 0; i < state.getWidth(); i++) {
				for (int j = 0; j < state.getHeight(); j++) {
					// find if space is occupied with player's piece
					if (state.getSpace(i,j) == player) {
						p.add(new Point(i,j));
					}
				}
			}
		}
		return p;
	}


	public List<Point> possiblePoints(BoardModel state) {
		// new possiblePoints function!
		Point[] dir = {new Point(0,1), new Point(1,0), new Point(1,1), new Point(-1,1),
				new Point(-1,0), new Point(0,-1), new Point(-1,-1),  new Point(1,-1)};
		List<Point> moves_made = getPlayerPieces(state, player);
		moves_made.addAll(getPlayerPieces(state, oppPlayer(player)));
		Set<Point> p = new HashSet<Point>();
		int width = state.getWidth();
		int height = state.getHeight();
		for (int j = 0; j < moves_made.size(); j++) {
			for(int i = 0; i < 8; i++){
				int currX = moves_made.get(j).x + dir[i].x;
				int currY = moves_made.get(j).y + dir[i].y;
				if(!(currX < 0 || currY >= height || currX >= width || currY < 0)){
					if(state.getSpace(currX, currY) == 0) {
						p.add(new Point(currX, currY));
					}
				}
			}
		}
		return new ArrayList<Point>(p);
	}


	public List<Point> findMaxReturnList(BoardModel originalState, List<Point> p){
		//System.out.println(p);
		Point firstPoint = p.get(0); 
		int greatestHeuristic = Integer.MIN_VALUE;
		for(int i = 0; i < p.size(); i++){
			BoardModel currentState = originalState.placePiece(p.get(i), player);
			if(h(currentState) > greatestHeuristic){
				firstPoint = p.get(i);
			}
		}
//		List<Point> onlyMax = new Vector<Point>();
//		onlyMax.add(firstPoint);
		p.remove(firstPoint);
		p.add(0,firstPoint);
		return p;
	}
	private BoardModel getLast(LinkedHashMap<BoardModel, Point> m){
		BoardModel last = null;
		for(BoardModel key : m.keySet()){
			last = key;
		}
		return last;
	}
	

	
	public Point mmsearch(BoardModel state, int depth) {
		System.out.println();
		System.out.println();
//		System.out.println("Current State");
//		System.out.println(state);
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		List<Point> p;
		List<Point> possibleA = attackOrDefend(state, oppPlayer(player));
		List<Point> possibleD = attackOrDefend(state, player);
		if(possibleA.size() > 0){
			p = possibleA;
			System.out.println("In mmsearch, list of possible attacks: ");
			printList(p);
			List<Point> p2 = possibleD;
			System.out.println("In mmsearch, list of possible defends: ");
			printList(p2);
		}
		else{
			p = possiblePoints(state);
		}
//		p = possiblePoints(state);
		int maxNumber = Integer.MIN_VALUE;
		Point maxP = p.get(0);
		BoardModel originalState = state.clone();
		//p = findMaxReturnList(originalState, p);
//		System.out.println(p);
//		System.out.println();
//		if(!stateAndMoves.isEmpty()){
//			Point move = stateAndMoves.get(state);
//			if(move != null){
//				state = getLast(stateAndMoves);
//				minValue(state, depth, alpha, beta);
//				return move;
//			}
//			else{
//				//if opponent makes a move not expected
//				stateAndMoves.clear();
//			}
//		}
		for(int i = 0; i < p.size(); i++){
			state = originalState.placePiece(p.get(i), player);
			int num = minValue(state, depth, alpha, beta);
			if(num > maxNumber){
				maxNumber = num;
				maxP = p.get(i);
			}
		}
//		System.out.println(maxP);
		return maxP;

	}

	

	public int minValue(BoardModel state, int depth, int alpha, int beta){
		if(((System.currentTimeMillis()-startTime)/1000) > 2){
			//System.out.println("Stopped because of time");
			return h(state);
		}
		if(depth == 0){
			int h = h(state);
			if (h > alpha) {
				alpha = h;
			}
			return h;
		}
		int v = Integer.MAX_VALUE;
		List<Point> p = possiblePoints(state);
		if(p.size() == 0){
			return h(state);
		}
		BoardModel originalState = state.clone();
//		p = findMaxReturnList(originalState, p);
		for(int i = 0; i < p.size(); i++){
			if (v < beta) {
				beta = v;
			}
			if (alpha >= beta) {
				break;
			}
			state = originalState.placePiece(p.get(i), oppPlayer(player));
//			stateAndMoves.put(originalState, p.get(i));
			v = Math.min(v, maxValue(state, depth-1, alpha, beta));
		}
		return v;
	}

	public int maxValue(BoardModel state, int depth, int alpha, int beta){
		if(((System.currentTimeMillis()-startTime)/1000) > 2){
			//System.out.println("Stopped because of time");
			return h(state);
		}
		if(depth == 0){
			int h = h(state);
			if (h > alpha) {
				alpha = h;
			}
			return h;
		}
		int v = Integer.MIN_VALUE;
		
		List<Point> p = possiblePoints(state);
		if(p.size() == 0){
			return h(state);
		}
		BoardModel originalState = state.clone();

//		p = findMaxReturnList(originalState, p);

		for(int i = 0; i < p.size(); i++){
			if (v > alpha) {
				alpha = v;
			}
			if (alpha >= beta) {
				break;
			}
			state = originalState.placePiece(p.get(i), player);
//			stateAndMoves.put(state, p.get(i));
			v = Math.max(v, minValue(state, depth-1, alpha, beta));
		}
		return v;
	}




	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}
}

























import connectK.CKPlayer;
import connectK.BoardModel;
import java.awt.Point;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.*;

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

	/* 
	 * IDS/Sorting AI must run on openlab.ics.uci.edu in the tournament shell 
	 * and extend Alpha-beta AI to include IDS search up to the time limit 
	 * plus sort moves based on the last IDS iteration to put the most favorable moves first for A/B pruning
	 * 
	 * HINTS
	 * http://stackoverflow.com/questions/10985000/how-should-i-design-a-good-evaluation-function-for-connect-4
	 * you have the base cases ironed out: my win = 100 pts, my loss = -100, tie = 0. 
	 * The "unsure" case you can kill, it does not reflect the "goodness" of the board. 
	 * So now you need to fill in the gaps. Cases you want to consider and assign values to:
	 * I have X in a row (If i have 3 in a row, that's better than only two in a row - 
	 * your function should favor adding to longer rows over shorter ones)
	 * My opponent has X in a row (Likewise, the more he/she has in a row, the worse off we are)
	 * Count how many rows you are filling in (Placing a piece and forming 2 rows of 3 is better than placing a piece 
	 * and only forming one row of 3)
	 * Count how many rows you are blocking (similarly, if you can drop a piece and block two opponents rows of 3, 
	 * that's better than blocking a single row of 2)
	 */

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
				move = mmsearch(state, 4);
			}
			//			System.out.println(move);
			return move; // temporary
		}
	}

	public int h(BoardModel state) {
		//System.out.println("player 1 (R)= " + countWins(state, player));
		//System.out.println("player 2 (B)= " + countWins(state, oppPlayer(player)));
		//		return countWins(state, player) - countWins(state, oppPlayer(player)); // old heuristic
		return countWins(state);
	}

	public int countWins(BoardModel state) {
		int count = 0;
		List<Point> p = new ArrayList<Point>(possiblePoints(state));
		for (int i = 0; i < p.size(); i++) {
			count += checkAllDir(p.get(i).x, p.get(i).y, state, player);
		}
		return count; 
	}

	public int checkAllDir(int x, int y, BoardModel state, byte player){
		// given current position, check all the directions and return the # of possible wins (how many passed)
		int count = 0;
		Point[] dir = {new Point(0,1), new Point(1,0), new Point(1,1), new Point(-1,-1),
				new Point(-1,0), new Point(0,-1), new Point(-1,1),  new Point(1,-1)};
		// loop through all the directions
		for (int i = 0; i < 8; i++){
			count += success(x, y, state, dir[i]);				
		}
		return count;
	}

	public int success(int x, int y, BoardModel state, Point p) {
		// checks a particular direction
		int width = state.getWidth();
		int height = state.getHeight();
		int k = state.kLength;
		int currPosX = x;
		int currPosY = y;
		int value = 0;
		for(int j = 0; j < Integer.max(width, height); j++) {
			currPosX += p.x;
			currPosY += p.y;
			//System.out.println("currPosX, currPosY " + currPosX + " " + currPosY);
			//System.out.println("player_space" + state.getSpace(currPosX, currPosY));
			// if it goes off the 4 edges of the board then return p
			if (currPosX < 0 || currPosY >= height || currPosX >= width || currPosY < 0)
				return value;
			if (state.getSpace(currPosX, currPosY) == player)
				value += 1;
			if (state.getSpace(currPosX, currPosY) == oppPlayer(player))
				value += 1;
			if (state.getSpace(currPosX, currPosY) == 0)
				break;
		}
		return value;
	}



	//	original functions
	//	public int countWins(BoardModel state, byte player) {
	//		// loop through various positions on the board and count the number of wins
	//		// currently goes through all pieces so works when gravity is on and off. 
	//		int count = 0;
	////		int width = state.getWidth();
	////		int height = state.getHeight();
	////		for (int w = 0; w < width; w++)  {
	////			for (int h = 0; h < height; h++) {
	//////				System.out.println("At position (" + w + ", " + h + ")");
	////				count += checkAllDir(w, h, state, player);
	////			}
	////		}
	//	}
	//	public int checkAllDir(int x, int y, BoardModel state, byte player){
	//		// given current position, check all the directions and return the # of possible wins (how many passed)
	//		int count = 0;
	//		Point[] dir = {new Point(0,1), new Point(1,0), new Point(1,1), new Point(-1,-1),
	//				new Point(-1,0), new Point(0,-1), new Point(-1,1),  new Point(1,-1)};
	//		// loop through all the directions
	//		for (int i = 0; i < 4; i++){
	//			if (checkDir(x, y, state, player, dir[i]))
	//				count += 1;				
	//		}
	//		return count;
	//	}
	//	public boolean checkDir(int x, int y, BoardModel state, byte player, Point p) {
	//		// checks a particular direction
	//		int width = state.getWidth();
	//		int height = state.getHeight();
	//		int k = state.kLength;
	//		// think there's a problem here when dealing with negative directions (cuz adding and sub result differently?)
	//		int currPosX = x - p.x;
	//		int currPosY = y - p.y;
	//		for(int j = 0; j < k; j++) {
	//			currPosX += p.x;
	//			currPosY += p.y;
	////			System.out.println("\t\tCheckedPos: " + Integer.toString(currPosX) + ","+Integer.toString(currPosY));
	//			// check that it doesn't go off the 4 edges of the board
	//			if (currPosX < 0 || currPosY >= height || currPosX >= width || currPosY < 0) {
	////				System.out.println("\t-----------------------------------");
	//				return false;
	//			}
	//			// if space is not blank or is opponent's then stop checking
	//			if (state.getSpace(currPosX, currPosY) != 0 && state.getSpace(currPosX , currPosY) != player){
	////				System.out.println("\t-----------------------------------");
	//				return false;
	//			}
	////			System.out.println("Move being checked: " + Integer.toString(currPosX) + ","+Integer.toString(currPosY));
	//		}
	////		System.out.println("\tDirection Passed: " + p.x + ", " + p.y); // should print if this direction passed!
	//		return true;
	//	}

	// Returns a list of a player's pieces on the board
	public List<Point> getPlayerPieces(BoardModel state, byte player) {
		List<Point> p = new ArrayList<Point> (state.getWidth() * state.getHeight());
		if (state.gravityEnabled()) {
			for (int i = 0; i < state.getWidth(); i++) {
				for (int j = 0; j < state.getHeight(); j++) {
					// if you hit an empty space with gravity enabled there's no more pieces higher (j++) than this one.
					if (state.getSpace(i,j) == 0)
						break;
					// find if space is occupied with player's piece
					if (state.getSpace(i,j) == oppPlayer(player)) {
						p.add(new Point(i,j));
					}
				}
			}
		}
		else {
			for (int i = 0; i < state.getWidth(); i++) {
				for (int j = 0; j < state.getHeight(); j++) {
					// find if space is occupied with player's piece
					if (state.getSpace(i,j) == oppPlayer(player)) {
						p.add(new Point(i,j));
					}
				}
			}
		}
		return p;
	}

	//	public List<List<Point>> findRows(BoardModel state) {
	//		List<Point> o = getOppPieces(state);
	//		List<List<Point>> rows = null;
	//		Point[] dir = {new Point(0,1), new Point(1,0), new Point(1,1), new Point(-1,-1),
	//				new Point(-1,0), new Point(0,-1), new Point(-1,1),  new Point(1,-1)};
	//		for (int i = 0; i < o.size(); i++) {
	//			List<Point> r = null;
	//			r.add(o.get(i));
	//			for (int d = 0; d < 8; d++) {
	//				if (o.contains(new Point(o.get(i).x + dir[d].x, o.get(i).y + dir[d].y))) {
	//					r.add(new Point(o.get(i).x + dir[d].x, o.get(i).y + dir[d].y));
	//				}
	//			}
	//		}
	//		return rows;
	//	}

	// List of moves (the moves directly around the opponent's pieces)
	//	public List<Point> suggestedMoves(BoardModel state) {
	//		List<Point> oppMoves = getPlayerPieces(state, oppPlayer(player)); // ??
	//		List<Point> moves = new ArrayList<Point> (oppMoves.size()); // size of it shouldn't be bigger than oppMoves
	//		for (int i = 0; i < oppMoves.size(); i++) {
	//			oppMoves.get(i);
	//			// now determine which surrounding 8 spaces you can place a piece on
	//		}
	//		return moves;
	//	}


	public List<Point> possiblePoints(BoardModel state) {
		// new possiblePoints function!
		Point[] dir = {new Point(0,1), new Point(1,0), new Point(1,1), new Point(-1,-1),
				new Point(-1,0), new Point(0,-1), new Point(-1,1),  new Point(1,-1)};
		List<Point> moves_made = getPlayerPieces(state, player);
		moves_made.addAll(getPlayerPieces(state, oppPlayer(player)));
		Set<Point> p = new HashSet<Point>();
		int width = state.getWidth();
		int height = state.getHeight();
		for (int j = 0; j < moves_made.size(); j++) {
			for(int i = 0; i < 8; i++){
				int currX = moves_made.get(j).x + dir[i].x;
				int currY = moves_made.get(j).y + dir[i].y;
				if(!(currX < 0 || currY >= height || currX >= width || currY < 0)){
					if(state.getSpace(currX, currY) == 0) {
						p.add(new Point(currX, currY));
					}
				}
			}
		}
		return new ArrayList<Point>(p);
	}

	//	public Point mmsearch(BoardModel state, int depth) {
	//		int alpha = Integer.MIN_VALUE;
	//		int beta = Integer.MAX_VALUE;
	//		List<Point> p = possiblePoints(state);
	//		int maxNumber = Integer.MIN_VALUE;
	//		Point maxP = p.get(0);
	//		BoardModel originalState = state.clone();
	//		for(int i = 0; i < p.size(); i++){
	//			state = originalState.placePiece(p.get(i), player);
	//			//state = state.placePiece(p.get(i), player);
	//			int num = minValue(state, depth, alpha, beta);
	//			if(num > maxNumber){
	//				maxNumber = num;
	//				maxP = p.get(i);
	//			}
	//		}
	//		return maxP;
	//	}

	public List<Point> findMaxReturnList(BoardModel originalState, List<Point> p){
		//System.out.println(p);
		Point firstPoint = p.get(0); 
		int greatestHeuristic = Integer.MIN_VALUE;
		for(int i = 0; i < p.size(); i++){
			BoardModel currentState = originalState.placePiece(p.get(i), player);
			if(h(currentState) > greatestHeuristic){
				firstPoint = p.get(i);
			}
		}
		p.remove(firstPoint);
		p.add(0, firstPoint);
		return p;
	}

	public Point mmsearch(BoardModel state, int depth) {
		System.out.println("Current State");
		System.out.println(state);
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;
		List<Point> p = possiblePoints(state);
		int maxNumber = Integer.MIN_VALUE;
		Point maxP = p.get(0);
		BoardModel originalState = state.clone();
		p = findMaxReturnList(originalState, p);
		System.out.println(p);
		System.out.println();
		for(int i = 0; i < p.size(); i++){
			state = originalState.placePiece(p.get(i), player);
			int num = minValue(state, depth, alpha, beta);
			if(num > maxNumber){
				maxNumber = num;
				maxP = p.get(i);
			}
		}
		System.out.println(maxP);
		return maxP;

	}

	//	public Point mmsearch(BoardModel state, int depth) {
	//		int alpha = Integer.MIN_VALUE;
	//		int beta = Integer.MAX_VALUE;
	//		List<Point> p = possiblePoints(state);
	//		int maxNumber = Integer.MIN_VALUE;
	//		Point maxP = p.get(0);
	//		BoardModel originalState = state.clone();
	//		Point best_move = p.get(0);
	//		int best_h = h(originalState.placePiece(p.get(0), player)); // calculate how good the board is if we make that move
	//		for (int i = 1; i < p.size(); i++) {
	//			BoardModel currentState = originalState.placePiece(p.get(i), player);
	//			if (h(currentState) > best_h) { // should it be greater than or equal to? 
	//																		  // Do we want to store moves that get the same heuristic value?
	//				best_move = p.get(i);
	//			}
	//		}
	//		p.remove(best_move);
	//		p.add(0, best_move);
	//		for(int i = 0; i < p.size(); i++){
	//			state = originalState.placePiece(p.get(i), player);
	//			int num = minValue(state, depth, alpha, beta);
	//			if(num > maxNumber){
	//				maxNumber = num;
	//				maxP = p.get(i);
	//			}
	//		}
	//		return maxP;
	//		
	////		state = state.placePiece(best_move, player);
	////		int num = minValue(state, depth, alpha, beta);
	////		if(num > maxNumber){
	////			maxNumber = num;
	//////			maxP = p.get(i); // not sure what this should change to
	////		}
	//////		return maxP;
	////		return best_move;
	//	}

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
		if(p.size() == 0){
			return h(state);
		}
		BoardModel originalState = state.clone();

		p = findMaxReturnList(originalState, p);

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
		if(p.size() == 0){
			return h(state);
		}
		BoardModel originalState = state.clone();

		p = findMaxReturnList(originalState, p);

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


	//	public int maxValue(BoardModel state, int depth, int alpha, int beta){
	//		if(depth == 0){
	//			int h = h(state);
	//			if (h > alpha) {
	//				alpha = h;
	//			}
	//			return h;
	//		}
	//		int v = Integer.MIN_VALUE;
	//		List<Point> p = possiblePoints(state);
	//		BoardModel originalState = state.clone();
	//		for(int i = 0; i < p.size(); i++){
	//			if (v > alpha) {
	//				alpha = v;
	//			}
	//			if (alpha >= beta) {
	//				break;
	//			}
	//			state = originalState.placePiece(p.get(i), player);
	//			v = Math.max(v, minValue(state, depth-1, alpha, beta));
	//		}
	//		return v;
	//	}
	//	
	//	public int minValue(BoardModel state, int depth, int alpha, int beta){
	//		if(depth == 0){
	//			int h = h(state);
	//			if (h > alpha) {
	//				alpha = h;
	//			}
	//			return h;
	//		}
	//		int v = Integer.MAX_VALUE;
	//		List<Point> p = possiblePoints(state);
	//		BoardModel originalState = state.clone();
	//		for(int i = 0; i < p.size(); i++){
	//			if (v < beta) {
	//				beta = v;
	//			}
	//			if (alpha >= beta) {
	//				break;
	//			}
	//			state = originalState.placePiece(p.get(i), oppPlayer(player));
	//			v = Math.min(v, maxValue(state, depth-1, alpha, beta));
	//		}
	//		return v;
	//	}


	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}
}
