


import java.util.ArrayList;

class HeapSortUtils {
	
	
	void heapSortHelp(ArrayList<Edge> edges, int count) {
		int base = count / 2 - 1;
		while(base >= 0) {
			downHeap(edges, base, count - 1);
			base -= 1; 
		}
		
	}
	
	void downHeap(ArrayList<Edge> edges, int start, int stop) {
		int base = start;
		while(base * 2 + 1 <= stop) {
			int childNode = base * 2 + 1;
			int switchI = base;
			if(edges.get(switchI).weight < edges.get(childNode).weight) {
				switchI = childNode;
			}
			if (childNode + 1 <= stop && edges.get(switchI).weight < edges.get(childNode + 1).weight) {
		        switchI = childNode + 1;
		      }
		      if (switchI != base) {
		        new ArrayUtils().swap(edges, base, switchI);
		        base = switchI;
		      }
		      else {
		        return;
		      }
		    }
		  }
		
		
	
	
	
	ArrayList<Edge> heapSort(ArrayList<Edge> edges) {
		int count = edges.size();
		heapSortHelp(edges, count);
		int stop = count - 1;
		while(stop > 0) {
			new ArrayUtils().swap(edges, stop, 0);
			stop = stop - 1;
			this.downHeap(edges, 0, stop);
		}
		
		return edges;
	}
		
	}
	
	
class ArrayUtils<T> {
	void swap(ArrayList<Edge> arr, int i1, int i2) {
		Edge old1 = arr.get(i1);
		Edge old2 = arr.get(i2);
		
		arr.set(i2, old1);
		arr.set(i1, old2);
		
	}
	
}