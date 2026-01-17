package app.backend.book;

import app.backend.utils.annotations.ValidIdAbsence;
import app.backend.utils.annotations.ValidIdPresence;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
@Validated
public class BookController {

    private final BookService service;
    private static final Logger logger = LogManager.getLogger(BookController.class);

    @GetMapping
    public PagedBooksDTO getAllBooks(@PageableDefault(size = 5) Pageable pageable) {
        logger.info("GET /books - Fetching all books with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return service.getAllBooks(pageable);
    }

    @GetMapping("/to-borrow")
    public PagedBooksDTO getAllBooksToBorrow(@PageableDefault(size = 5) Pageable pageable) {
        logger.info("GET /books/to-borrow - Fetching books available to borrow");
        return service.getAllBooksToBorrow(pageable);
    }

    @GetMapping("/to-remove")
    public PagedBooksDTO getAllBooksToRemove(@PageableDefault(size = 5) Pageable pageable) {
        logger.info("GET /books/to-remove - Fetching books to remove");
        return service.getAllBooksToRemove(pageable);
    }

    @GetMapping("/{id}")
    public BookDTO getBook(@PathVariable int id) {
        logger.info("GET /books/{} - Fetching book by id", id);
        return service.getById(id);
    }

    @GetMapping("/user/{id}")
    public PagedBooksDTO getUserBooks(@PathVariable int id, @PageableDefault(size = 5) Pageable pageable) {
        logger.info("GET /books/user/{} - Fetching user's books", id);
        return service.getByOwnerUser(id, pageable);
    }

    @GetMapping("/criteria")
    public PagedBooksDTO getSortedBooks(@Valid BookSortingCriteriaDTO criteria, @PageableDefault(size = 5) Pageable pageable) {
        logger.info("GET /books/criteria - Filtering books by criteria: minYear={}, maxYear={}, types={}",
            criteria.getMinPublishYear(), criteria.getMaxPublishYear(), criteria.getTypes());
        return service.getBySortingCriteria(criteria, pageable);
    }

    @GetMapping("/types")
    public List<BookTypeDTO> getBookTypes() {
        logger.info("GET /books/types - Fetching all book types");
        return service.getAllBookTypes();
    }

    @PostMapping
    public BookDTO add(@RequestBody @Valid @ValidIdAbsence BookDTO book) {
        logger.info("POST /books - Adding new book: title={}, author={}", book.title(), book.author());
        return service.save(book);
    }

    @PostMapping("/{bookId}/borrow")
    public void borrowBook(@PathVariable int bookId) {
        logger.info("POST /books/{}/borrow - User borrowing book", bookId);
        service.borrowBook(bookId);
    }

    @PostMapping("/{bookId}/return")
    public void returnBook(@PathVariable int bookId) {
        logger.info("POST /books/{}/return - User returning book", bookId);
        service.returnBook(bookId);
    }

    @PutMapping
    public BookDTO update(@RequestBody @Valid @ValidIdPresence BookDTO book) {
        logger.info("PUT /books - Updating book with id={}: title={}, author={}", book.id(), book.title(), book.author());
        return service.update(book);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(@PathVariable int id) {
        logger.warn("DELETE /books/{} - Deleting book", id);
        service.delete(id);
    }

}
