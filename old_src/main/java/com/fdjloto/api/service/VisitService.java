package com.fdjloto.api.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class VisitService {

    private static final Path FILE = Paths.get("data/visits.txt");
    private final AtomicLong counter;

    public VisitService() {
        this.counter = new AtomicLong(loadFromFile());
    }

    // Incrémente + sauvegarde + renvoie la valeur
    public synchronized long incrementAndGet() {
        long value = counter.incrementAndGet();
        saveToFile(value);
        return value;
    }

    // Renvoie la valeur sans incrément
    public long get() {
        return counter.get();
    }

    private long loadFromFile() {
        try {
            if (!Files.exists(FILE)) {
                Files.createDirectories(FILE.getParent());
                Files.writeString(FILE, "0", StandardOpenOption.CREATE);
                return 0;
            }
            String s = Files.readString(FILE).trim();
            return s.isEmpty() ? 0 : Long.parseLong(s);
        } catch (Exception e) {
            return 0; // fallback si fichier illisible/corrompu
        }
    }

    private void saveToFile(long value) {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(
                    FILE,
                    Long.toString(value),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException ignored) {}
    }
}
