package org.reldb.toolbox.types;

import java.util.Objects;

/**
 * A pair of instances of specified types.
 *
 * @param <T1> First item type.
 * @param <T2> Second item type.
 */
public class Pair<T1, T2> {
	/** Left instance. */
	public final T1 left;

	/** Right instance. */
	public final T2 right;

	/**
	 * Constructor.
	 *
	 * @param left Left instance.
	 * @param right Right instance.
	 */
	public Pair(T1 left, T2 right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + (left != null ? left.hashCode() : 0);
		hash = 31 * hash + (right != null ? right.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof Pair) {
			Pair<?, ?> pair = (Pair<?, ?>) o;
			if (!Objects.equals(left, pair.left))
				return false;
			return Objects.equals(right, pair.right);
		}
		return false;
	}

	/**
	 * Obtain the left instance.
	 *
	 * @return Left instance.
	 */
	public T1 getKey() {
		return left;
	}

	/**
	 * Obtain the right instance.
	 *
	 * @return Right instance.
	 */
	public T2 getValue() {
		return right;
	}

	@Override
	public String toString() {
		return "Pair(" 
				+ (left == null ? "null" : left.toString())
				+ ", "
				+ (right == null ? "null" : right.toString())
				+ ")";
	}
	
}
