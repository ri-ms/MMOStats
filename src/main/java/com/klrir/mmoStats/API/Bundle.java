package com.klrir.mmoStats.API;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class Bundle<T, K> {
    @Getter
    @Setter
    private T first;
    private K second;

    public Bundle(T first, K second) {
        this.first = first;
        this.second = second;
    }

    public K getLast() {
        return second;
    }

    public void setLast(K second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                (first != null ? first.toString() : "null") + "; " +
                (second != null ? second.toString() : "null") + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Bundle<?, ?> other = (Bundle<?, ?>) obj;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}