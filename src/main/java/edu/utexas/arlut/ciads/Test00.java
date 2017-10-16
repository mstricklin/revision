package edu.utexas.arlut.ciads;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;

@Slf4j
public class Test00 {
    public static void main(String[] args) {
        log.info("== Foo ==");
        @SuppressWarnings("unchecked")
        Entry<String, String>[] entries = (Entry[]) new Entry[4];

        Arrays.stream(entries).forEach(e -> log.info("{}", e));
        entries[0] = new Entry<>("zzz", "Z");

        entries[1] = new Entry<>("aaa", "A");
        log.info("== Bar ==");
        Arrays.stream(entries).forEach(e -> log.info("{}", e));
        log.info("== sort ==");
        Arrays.sort(entries, 0, 2);
        Arrays.stream(entries).forEach(e -> log.info("{}", e));
    }

    @ToString
    private static class Entry<T extends Comparable<T>, U> implements Comparable<Entry<T, U>> {
        private T key = null;
        private U value = null;
        private T[] keys = null;

        @SuppressWarnings("unchecked")
        Entry(T t, U u) {
            key = t;
            value = u;
            keys = (T[]) new Comparable[4];
        }

        @Override
        public int compareTo(Entry<T, U> o) {
            return key.compareTo(o.key);
        }
    }
}