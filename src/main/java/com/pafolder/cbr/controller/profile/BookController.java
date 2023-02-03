package com.pafolder.cbr.controller.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.pafolder.cbr.model.Book;
import com.pafolder.cbr.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@Tag(name = "profile-book-controller")
@RequestMapping(value = BookController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class BookController {
    public static final String REST_URL = "/api/profile/books";
    private static final String NO_BOOKS_FOUND = "No books found";
    public static final String NO_BOOK_FOUND = "No book found";
    private final Logger log = LoggerFactory.getLogger(getClass());
    protected BookRepository bookRepository;

    @GetMapping("/search")
    @Operation(summary = "Searching for books by Author or substring in title", security = {@SecurityRequirement(name = "basicScheme")})
    @Parameter(name = "author", description = "Author name")
    @Parameter(name = "text", description = "Text substring in Book's title (ignoring case)")
    public MappingJacksonValue search(@RequestParam @Nullable String author, @RequestParam @Nullable String text) {
        log.info("search()");
        List<Book> books = new ArrayList<>();
        if (Optional.ofNullable(author).isPresent()) {
            books.addAll(bookRepository.findAllByAuthor(author));
        }
        if (Optional.ofNullable(text).isPresent()) {
            books.addAll(bookRepository.findAllBySubstringInTitle(text));
        }
        if (books.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, NO_BOOKS_FOUND);
        }
        return getFilteredBooksJson(books);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by Id", security = {@SecurityRequirement(name = "basicScheme")})
    public MappingJacksonValue getById(@PathVariable int id) {
        log.info("getById()");
        return getFilteredBooksJson(bookRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, NO_BOOK_FOUND)));
    }

    private <T> MappingJacksonValue getFilteredBooksJson(T object) {
        SimpleFilterProvider filterProvider = new SimpleFilterProvider()
                .addFilter("bookJsonFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept(
                                "id", "author", "title", "location", "amount"));
        new ObjectMapper().setFilterProvider(filterProvider);
        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(object);
        mappingJacksonValue.setFilters(filterProvider);
        return mappingJacksonValue;
    }
}