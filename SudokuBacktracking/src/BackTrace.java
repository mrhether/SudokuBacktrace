import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class BackTrace {

	public static ArrayList<ArrayList<SudokuVariable>> sudokuArray = new ArrayList<ArrayList<SudokuVariable>>();
	public static int nodeCount = 0;

	public static void main(String[] args) throws Exception {
		double total = 0;
		double nodeTotal = 0;
		ArrayList<Long> times = new ArrayList<Long>();
		ArrayList<Long> nodes = new ArrayList<Long>();
		
	    for (int j =0; j <1; j++) {
			sudokuArray = new ArrayList<ArrayList<SudokuVariable>>();
			nodeCount = 0;
			loadSudoku("sudokuEvil");
			ArrayList<ArrayList<SudokuVariable>> ls = sudokuArray;
			long startTime = System.currentTimeMillis();
			recalculateConstraints(ls);
			bracktrack(ls);
			long estimatedTime = System.currentTimeMillis() - startTime;
			total += estimatedTime;
			nodeTotal += nodeCount;
			times.add(estimatedTime);
			nodes.add((long) nodeCount);
			System.out.println("Time: " + estimatedTime);
			System.out.println("NC: " + nodeCount);
			printSudoku(ls);
		}
	    double avgTime = total/times.size();
	    double avgNode = nodeTotal/nodes.size();
		System.out.println("avgTime: " + avgTime);
		System.out.println("avgNode: " + avgNode);
		System.out.println("sdTime: " + StandardDeviation(avgTime, times));
		System.out.println("sdNode: " + StandardDeviation(avgNode, nodes));
	    
	    
	}

	// Algorithms ///
	
	private static boolean bracktrack(final ArrayList<ArrayList<SudokuVariable>> ls) {
		nodeCount++;
		
		// Check if we have one.
		boolean win = checkWin(ls);
		if (win){
			return true;
		}
		
		// Forward check
		if (forwardCheck(ls)) {
			return false;
		}
		
		// Create List of empty squares
		final ArrayList<SudokuVariable> values = new ArrayList<SudokuVariable>();
		for(int y = 0; y < 9; y++) {
			for(int x = 0; x < 9; x++) {
				SudokuVariable value = ls.get(y).get(x);
				if (value.CurrentValue == 0) {
					values.add(value);
				}
			}
		}
		
		// Perform Heuristics of Randomization
		// Collections.shuffle(values, new Random(System.nanoTime()));
		mostConstrainedAndMostConstraining(ls, values);
		
		for (final SudokuVariable value : values) {
			ArrayList<Integer> copyOfAvailableValues = new ArrayList<Integer>(value.AvailableValues);
			
			// Perform Heuristic or Randomization
			//Collections.shuffle(copyOfValues, new Random(System.nanoTime()));
			leastConstrainingValue(ls, values, value, copyOfAvailableValues);
			
			for(Integer i: copyOfAvailableValues) {
				value.CurrentValue = i;
				if (allDif(ls.get(value.Y)) 
					&& allDifColumn(ls, value.X)
					&& allDifSquare(ls, value.X/3,value.Y/3)) {

					recalculateConstraints(ls);
					boolean tree = bracktrack(ls);
					if (tree == true) return true;
				}
				value.CurrentValue = 0;
				resetConstraints(ls);
				recalculateConstraints(ls);

			}
			return false;
		}
		return false;
	}

	private static void resetConstraints(
			final ArrayList<ArrayList<SudokuVariable>> ls) {
		for(int j=0; j < 9; j++) {
			resetVariables(ls.get(j));
			resetVariablesColumn(ls,j);
			resetVariablesSquare(ls,j%3,j/3);
		}
	}

	private static void recalculateConstraints(
			final ArrayList<ArrayList<SudokuVariable>> ls) {
		for(int j=0; j < 9; j++) {
			removeUnavailable(ls.get(j));
			removeUnavailableColumn(ls,j);
			removeUnavailableSquare(ls,j%3,j/3);
		}
	}

	private static void mostConstrainedAndMostConstraining(
			final ArrayList<ArrayList<SudokuVariable>> ls,
			final ArrayList<SudokuVariable> values) {
		Collections.sort(values, new Comparator<SudokuVariable>() {
			@Override
			public int compare(SudokuVariable o1, SudokuVariable o2) {
				int mCV = 0;
				if (o1.AvailableValues.size() == o2.AvailableValues.size()) {
					int a1 = mostConstrainingCount(ls, o1.X, o1.Y);
					int a2 = mostConstrainingCount(ls, o2.X, o2.Y);
					mCV = a1 > a2 ? 1 : a1 == a2 ? 0 : -1;
				}
				return o1.AvailableValues.size() < o2.AvailableValues.size() ? -1 : 
					   o1.AvailableValues.size() == o2.AvailableValues.size() ? mCV : 1;
			}
		});
	}

	private static void leastConstrainingValue(
			final ArrayList<ArrayList<SudokuVariable>> ls,
			final ArrayList<SudokuVariable> values, final SudokuVariable value,
			ArrayList<Integer> copyOfValues) {
		
		// If there's only one variable there is no need to sort
		if (copyOfValues.size() > 1) {
			// Populate a hash with the total number 
			// of free spaces left for each number choice.
			final HashMap<Integer, Integer> remainingVariables = new HashMap<Integer, Integer>();
			for (Integer possibleValue : copyOfValues) {
				value.CurrentValue = possibleValue;
				resetConstraints(ls);
				recalculateConstraints(ls);
				int spotsLeft = 0;
				for (SudokuVariable val : values) {
					spotsLeft += val.AvailableValues.size();
				}
				remainingVariables.put(possibleValue, spotsLeft);
			}
			
			// reset the value.
			value.CurrentValue = 0;
			resetConstraints(ls);
			recalculateConstraints(ls);
			
			// sort using the above hash.
			Collections.sort(copyOfValues, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return remainingVariables.get(o1) > remainingVariables.get(o2) ? -1 : 
						remainingVariables.get(o1) == remainingVariables.get(o2) 
						   ? 0 : 1;
				}
			});
		}
	}

	private static boolean forwardCheck(ArrayList<ArrayList<SudokuVariable>> ls) {
		for (ArrayList<SudokuVariable> row : ls) {
			for (SudokuVariable number : row) {
				if (number.CurrentValue == 0
						&& number.AvailableValues.size() == 0)
					return true;
			}
		}
		return false;
	}
	
	// Private functions ///
	
	private static boolean checkWin(ArrayList<ArrayList<SudokuVariable>> ls) {
		boolean win = true;
		for (ArrayList<SudokuVariable> row : ls) {
			for (SudokuVariable number : row) {
				if (number.CurrentValue == 0)
					win = false;
			}
		}
		return win;
	}

	private static boolean allDifSquare(
			ArrayList<ArrayList<SudokuVariable>> ls, int startX, int startY) {
		ArrayList<SudokuVariable> column = getSquare(ls, startX, startY);
		return allDif(column);
	}

	private static boolean allDifColumn(
			ArrayList<ArrayList<SudokuVariable>> ls, int columnNum) {
		ArrayList<SudokuVariable> column = getColumn(ls, columnNum);
		return allDif(column);
	}

	public static boolean allDif(ArrayList<SudokuVariable> list) {
		for (int i = 0; i < list.size(); i++) {
			SudokuVariable variable = list.get(i);
			if (variable.CurrentValue != 0) {
				boolean fail = list.subList(i + 1, list.size()).contains(variable);
				if (fail) return false;
				fail = variable.CurrentValue > 10 || variable.CurrentValue < 0;
				if (fail) return false;
			}
		}
		return true;
	}
	
	public static int mostConstrainingCount(
			ArrayList<ArrayList<SudokuVariable>> ls, int x, int y) {
		int count = 0;
		ArrayList<SudokuVariable> column = getColumn(ls, x);
		ArrayList<SudokuVariable> row = ls.get(y);
		ArrayList<SudokuVariable> square = new ArrayList<SudokuVariable>();
		for (SudokuVariable value : getSquare(ls, x%3,y/3)) {
			if (value.X != x || value.Y != y) {
				square.add(value);
			}
		}
		count += addValues(column);
		count += addValues(square);
		count += addValues(row);
		return count;
		
	}
	
	public static int addValues(ArrayList<SudokuVariable> list) {
		int count = 0;
		for (SudokuVariable variable : list) {
			if (variable.CurrentValue == 0) count++;
		}
		return count;
	}

	private static void removeUnavailableSquare(
			ArrayList<ArrayList<SudokuVariable>> ls, int startX, int startY) {
		ArrayList<SudokuVariable> column = getSquare(ls, startX, startY);
		removeUnavailable(column);
	}

	private static void removeUnavailableColumn(
			ArrayList<ArrayList<SudokuVariable>> ls, int columnNum) {
		ArrayList<SudokuVariable> column = getColumn(ls, columnNum);
		removeUnavailable(column);
	}

	public static void removeUnavailable(ArrayList<SudokuVariable> list) {
		for (int i = 0; i < list.size(); i++) {
			SudokuVariable variable = list.get(i);
			if (variable.CurrentValue != 0) {
				for (SudokuVariable var : list) {
					if (!var.equals(variable))
						var.AvailableValues.remove(variable.CurrentValue);
				}
			}
		}
	}

	private static void resetVariablesSquare(
			ArrayList<ArrayList<SudokuVariable>> ls, int startX, int startY) {
		ArrayList<SudokuVariable> column = getSquare(ls, startX, startY);
		resetVariables(column);
	}

	private static void resetVariablesColumn(
			ArrayList<ArrayList<SudokuVariable>> ls, int columnNum) {
		ArrayList<SudokuVariable> column = getColumn(ls, columnNum);
		resetVariables(column);
	}

	public static void resetVariables(ArrayList<SudokuVariable> list) {
		for (int i = 0; i < list.size(); i++) {
			SudokuVariable variable = list.get(i);
			variable.resetAvail();
		}
	}
	
	private static ArrayList<SudokuVariable> getSquare(
			ArrayList<ArrayList<SudokuVariable>> ls, int startX, int startY) {
		ArrayList<SudokuVariable> column = new ArrayList<SudokuVariable>();
		for (int i = 0; i < 9; i++) {
			int x = startX * 3 + i % 3;
			int y = startY * 3 + i / 3;
			column.add(ls.get(y).get(x));
		}
		return column;
	}

	private static ArrayList<SudokuVariable> getColumn(
			ArrayList<ArrayList<SudokuVariable>> ls, int columnNum) {
		ArrayList<SudokuVariable> column = new ArrayList<SudokuVariable>();
		for (ArrayList<SudokuVariable> lst : ls) {
			column.add(lst.get(columnNum));
		}
		return column;
	}
	
	// Unrelated to algorithm ////
	
	private static double StandardDeviation(double avg, ArrayList<Long> times) {
		double sd = 0;
		for (Long d:times) {
			sd += Math.pow(d - avg, 2);
		}
		sd /= times.size();
		sd = Math.sqrt(sd);
		return sd;
	}
	
	private static void loadSudoku(String file) throws FileNotFoundException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;

		int x = 0;
		int y = 0;

		while ((line = reader.readLine()) != null) {
			String[] parts = line.split("\\s");
			ArrayList<SudokuVariable> row = new ArrayList<SudokuVariable>();
			for (String part : parts) {
				row.add(new SudokuVariable(Integer.valueOf(part), x, y));
				x++;
			}
			sudokuArray.add(row);
			y++;
			x = 0;
		}

		reader.close();
	}

	private static void printSudoku(ArrayList<ArrayList<SudokuVariable>> ls) {
		for (ArrayList<SudokuVariable> row : ls) {
			for (SudokuVariable number : row) {
				System.out.print(number.CurrentValue + " ");
			}
			System.out.println(" ");
		}
		System.out.println(" ");
	}
}
