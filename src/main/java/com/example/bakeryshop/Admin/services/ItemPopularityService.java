package com.example.bakeryshop.Admin.services;

import com.example.bakeryshop.Admin.model.item;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

@Service
public class ItemPopularityService {
    private final ConcurrentMap<String, LongAdder> addToCartCounts = new ConcurrentHashMap<>();

    public void recordAddToCart(String itemId) {
        addToCartCounts.computeIfAbsent(itemId, ignored -> new LongAdder()).increment();
    }

    public List<item> getPopularItems(List<item> items, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return items.stream()
                .filter(candidate -> candidate != null && candidate.getId() != null)
                .filter(candidate -> "Available".equals(candidate.getAvailability()))
                .filter(candidate -> addToCartCounts.containsKey(candidate.getId()))
                .sorted(Comparator
                        .<item>comparingLong(candidate -> addToCartCounts.get(candidate.getId()).sum())
                        .reversed()
                        .thenComparing(item::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(limit)
                .toList();
    }
}
