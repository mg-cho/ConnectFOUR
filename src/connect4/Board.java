package connect4;
//import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.*;

public class Board {
		
	private CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>> board; 
	private Lock lock = new ReentrantLock(); 
	
	private Integer width; 
	private Integer height; 
	private Integer winnerLength; 
	
	public Board(Integer width, Integer height, Integer winnerLength) {
		
		this.width = width; 
		this.height = height; 
		this.winnerLength = winnerLength; 
		
		board = new CopyOnWriteArrayList<CopyOnWriteArrayList<Integer>>(); 
		for(int i = 0; i < height; i++) {
			board.add(new CopyOnWriteArrayList<Integer>()); 
			for(int j = 0; j < width; j++) {
				board.get(i).add(0); 
			}
		}
	}
	
	/*
	 * Add function adds player token to lowest available spot in their chosen column. 
	 * If that column is full, return 0 to signify they cannot place their token there
	 * Otherwise, iterate down chosen column until it reaches the bottom of the board, or the lowest available space is found
	 * Return 1 to signify operation was successful
	 * */
	public Integer add(Integer player, Integer col) {
		col--;
		lock.lock(); 
		try {
			if(!(board.get(0).get(col).equals(0))) { return 0; }
			
			for(Integer i = 0; i < height; i++) {
				if(!(board.get(i).get(col).equals(0))) {
					board.get(i-1).set(col, player); 
					break; 
				}
				else if(i.equals(height - 1)) {
					board.get(i).set(col, player);
					break; 
				}
			}
			return 1; 
		}
		finally { lock.unlock(); }
		
	}
	
	public boolean isEmpty() {
		for (int i=0; i<height; ++i) {
			for (int j=0; j<width; ++j) {
				if (board.get(i).get(j) != 0) {
					return false;
				}
			}
		}
		return true;
	}
	
	/*
	 * Checks if there is a winner on the board
	 * If there is, return player number
	 * If there is not, return 0
	 * */
	public Integer checkWinner() {
		
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				if(!(board.get(i).get(j).equals((0)))) {
					if(checkWinnerHelper(i,j,board.get(i).get(j))) {
						return board.get(i).get(j); 
					}
				}
			}
		}
		
		return 0; 
	}
	
	/*
	 * Helper function checks if there is winnerLength tokens of the same player in a row 
	 * Checks horizontally, vertically, and diagonally
	 * */
	private Boolean checkWinnerHelper(int i, int j, Integer player) {
		
		//Check right
		Boolean rightCheck = true; 
		if(j + winnerLength >= width) { rightCheck = false; }
		else {
			for(int cnt = 0; cnt < winnerLength; cnt++) {
				if(!board.get(i).get(j + cnt).equals(player)) {
					rightCheck = false; 
					break; 
				}
			}
		}
		if(rightCheck) { return true; } 
		
		//Check left
		Boolean leftCheck = true; 
		if(j - winnerLength < 0) { leftCheck = false; }
		else {
			for(int cnt = 0; cnt < winnerLength; cnt++) {
				if(!board.get(i).get(j - cnt).equals(player)) {
					leftCheck = false; 
					break; 
				}
			}
		}
		if(leftCheck) { return true; } 
		
		//Check up
		Boolean upCheck = true; 
		if(i - winnerLength < 0) { upCheck = false; }
		else {
			for(int cnt = 0; cnt < winnerLength; cnt++) {
				if(!board.get(i - cnt).get(j).equals(player)) {
					upCheck = false; 
					break; 
				}
			}
		}
		if(upCheck) { return true; } 
		
		//Check down
		Boolean downCheck = true; 
		if(i + winnerLength >= height) { downCheck = false; }
		else {
			for(int cnt = 0; cnt < winnerLength; cnt++) {
				if(!board.get(i + cnt).get(j).equals(player)) {
					downCheck = false; 
					break; 
				}
			}
		}
		if(downCheck) { return true; } 
		
		//Check up right diagonal
		Boolean upRightCheck = true; 
		if(i - winnerLength < 0 || j + winnerLength >= width) { upRightCheck = false; }
		else {
			for(int cnt = 0; cnt < winnerLength; cnt++) {
				if(!board.get(i - cnt).get(j + cnt).equals(player)) {
					upRightCheck = false; 
					break; 
				}
			}
		}
		if(upRightCheck) { return true; } 
		
		//Check down right diagonal
		Boolean downRightCheck = true; 
		if(i + winnerLength >= height || j + winnerLength >= width) { downRightCheck = false; }
		else {
			for(int cnt = 0; cnt < winnerLength; cnt++) {
				if(!board.get(i + cnt).get(j + cnt).equals(player)) {
					downRightCheck = false; 
					break; 
				}
			}
		}
		if(downRightCheck) { return true; } 
		
		//Check up left diagonal
		Boolean upLeftCheck = true; 
		if(i - winnerLength < 0 || j - winnerLength < 0) { upLeftCheck = false; }
		else {
			for(int cnt = 0; cnt < winnerLength; cnt++) {
				if(!board.get(i - cnt).get(j - cnt).equals(player)) {
					upLeftCheck = false; 
					break; 
				}
			}
		}
		if(upLeftCheck) { return true; } 
		
		//Check down left diagonal
		Boolean downLeftCheck = true; 
		if(i + winnerLength >= height || j - winnerLength < 0) { downLeftCheck = false; }
		else {
			for(int cnt = 0; cnt < winnerLength; cnt++) {
				if(!board.get(i + cnt).get(j - cnt).equals(player)) {
					upLeftCheck = false; 
					break; 
				}
			}
		}
		if(downLeftCheck) { return true; } 
		
		return false; 
	}
	
	/*
	 * Print function for checking functionality
	 * */
	public void print() {
		for(int i = 0; i < height; i++) {
			System.out.print("[");
			for(int j = 0; j < width; j++) {
				Integer token = board.get(i).get(j);
				if (token == 0) {
					System.out.print(" ");
				}
				else if (token == 1) {
					System.out.print("X");
				}
				else if (token == 2) {
					System.out.print("O");
				}
				System.out.print(" "); 
			}
			System.out.println("]"); 
		}
	}
	
	/*
	 * Main function for checking functionality
	 * */
	/*public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in); 
		Board board = new Board(7,6,4); 
		int i = 0; 
		
		board.print(); 
		while(true) {
			if(!(board.checkWinner().equals(0))) {
				System.out.println(board.checkWinner() + " wins!"); 
				break; 
			}
			
			if(i % 2 == 0) {
				System.out.print("Which column would you like to add your token? (Cols 1-7) ");
				int col = sc.nextInt(); 
				board.add(1, col - 1); 
			}
			else {
				System.out.print("Which column would you like to add your token? (Cols 1-7) ");
				int col = sc.nextInt(); 
				board.add(2, col - 1); 
			}
			
			board.print(); 
			
			i++; 
		}
		
		sc.close(); 
	}*/
}