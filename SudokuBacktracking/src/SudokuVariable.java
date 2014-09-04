

import java.lang.Integer;
import java.util.ArrayList;

public class SudokuVariable {
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((AvailableValues == null) ? 0 : AvailableValues.hashCode());
		result = prime * result
				+ ((CurrentValue == null) ? 0 : CurrentValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SudokuVariable other = (SudokuVariable) obj;
		if (CurrentValue == null) {
			if (other.CurrentValue != null)
				return false;
		} else if (!CurrentValue.equals(other.CurrentValue))
			return false;
		return true;
	}

	public Integer CurrentValue;
	public ArrayList<Integer> AvailableValues;
	public Integer X;
	public Integer Y;
	
	public SudokuVariable(int value, int x, int y) {
		CurrentValue = value;
		X=x;
		Y=y;
		resetAvail();
	}
	
	public void resetAvail() {
		AvailableValues = new ArrayList<Integer>();
		for(int i=1; i < 10;i++) {
			AvailableValues.add(i);
		}
	}
	
	
}
