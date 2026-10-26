package com.example.demo.book;

import com.example.demo.annotation.ValidPublishedYear;

@ValidPublishedYear
public record BookDto(Long id, String title, String author, Integer publishedYear) {
}
