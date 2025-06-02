package datasahi.siyadb.common.util;

import java.util.Arrays;
import java.util.List;

public class CircularRecordBuffer<T> {
    private final T[] buffer;
    private int currentPosition = 0;
    private boolean isFull = false;
    private final Object lock = new Object(); // Dedicated lock object

    @SuppressWarnings("unchecked")
    public CircularRecordBuffer(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        buffer = (T[]) new Object[capacity];
    }

    public void add(T record) {
        synchronized (lock) {
            buffer[currentPosition] = record;
            currentPosition = (currentPosition + 1) % buffer.length;

            if (currentPosition == 0) {
                isFull = true;
            }
        }
    }

    public List<T> getRecords() {
        synchronized (lock) {
            @SuppressWarnings("unchecked")
            T[] result = (T[]) new Object[size()];

            if (!isFull) {
                System.arraycopy(buffer, 0, result, 0, currentPosition);
            } else {
                // Buffer is full, need to arrange elements in correct order
                // First copy the older elements (from currentPosition to end)
                // Then copy the newer elements (from 0 to currentPosition)
                System.arraycopy(buffer, currentPosition, result, 0, buffer.length - currentPosition);
                System.arraycopy(buffer, 0, result, buffer.length - currentPosition, currentPosition);
            }

            return Arrays.asList(result);
        }
    }

    public int size() {
        synchronized (lock) {
            return isFull ? buffer.length : currentPosition;
        }
    }

    public int capacity() {
        return buffer.length;
    }

    public void clear() {
        synchronized (lock) {
            Arrays.fill(buffer, null);
            currentPosition = 0;
            isFull = false;
        }
    }
}