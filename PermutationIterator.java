
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class PermutationIterator implements Iterator {
	private char[] buf;
	private final int n;
	private boolean nextReady;

	public PermutationIterator(String s) {
		buf = s.toCharArray();
		Arrays.sort(buf);
		n = buf.length;
		nextReady = n > 0;
	}

	@Override
	public boolean hasNext() {
		return nextReady;
	}

	@Override
	public String next() {
		if (!nextReady) {
			throw new NoSuchElementException();
		}

		String ret = new String(buf);

		int i;
		int j;

		for (i = n - 2; i >= 0; i--) {
			if (buf[i] < buf[i + 1]) break;
		}

		if (i < 0) {
			nextReady = false;
			return ret;
		}

		for (j = n - 1; j >= 0; j--) {
			if (buf[i] < buf[j]) break;
		}

		swapElem(i, j);

		reverseSegment(i + 1, n);

		return ret;
	}

	private void reverseSegment(int a, int b) {
		assert a < b;
		for (b--; a < b; a++, b--) {
			swapElem(a, b);
		}
	}

	private void swapElem(int i, int j) {
		char temp = buf[i];
		buf[i] = buf[j];
		buf[j] = temp;
	}

}


