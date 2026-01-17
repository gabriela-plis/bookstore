package app.backend.book;

import app.backend.user.UserEntity;
import app.backend.user.UserRepository;
import app.backend.utils.SecurityContextAccessor;
import app.backend.utils.exceptions.ProductAlreadyBorrowedException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final Logger logger = LogManager.getLogger(BookService.class);

    private final SecurityContextAccessor securityContextAccessor;

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final UserRepository userRepository;

    private final BookTypeRepository bookTypeRepository;
    private final BookTypeMapper bookTypeMapper;

    public PagedBooksDTO getAllBooks(@PageableDefault(size = 5) Pageable pageable) {
        logger.info("Fetching all books");
        Page<BookEntity> pagedBooks = bookRepository.findAll(pageable);

        return getPagedBooksDTO(pagedBooks);
    }

    public PagedBooksDTO getAllBooksToBorrow(Pageable pageable) {
        logger.info("Fetching books available to borrow");
        Page<BookEntity> pagedBooks = bookRepository.getByCanBeBorrowIsTrueAndAvailableAmountIsGreaterThan(0, pageable);

        return getPagedBooksDTO(pagedBooks);
    }

    public PagedBooksDTO getByOwnerUser(int id, Pageable pageable) {
        logger.info("Fetching books owned by user with id: {}", id);
        Page<BookEntity> pagedBooks = bookRepository.getByOwnerUsers_Id(id, pageable);

        return getPagedBooksDTO(pagedBooks);
    }

    public BookDTO getById(int id) {
        logger.info("Fetching book with id: {}", id);
        BookEntity book = bookRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        return bookMapper.toDTO(book);
    }

    public PagedBooksDTO getBySortingCriteria(BookSortingCriteriaDTO criteria, Pageable pageable) {
        logger.info("Fetching books by sorting criteria: minYear={}, maxYear={}", criteria.getMinPublishYear(), criteria.getMaxPublishYear());
        Page<BookEntity> pagedBooks;

        if (criteria.getTypes().size() != 0) {
            pagedBooks = bookRepository.findByPublishYearBetweenAndType_NameInAndCanBeBorrowIsTrueAndAvailableAmountGreaterThan(criteria.getMinPublishYear(), criteria.getMaxPublishYear(), criteria.getTypes(), 0, pageable);
        } else {
            pagedBooks = bookRepository.findByPublishYearBetweenAndCanBeBorrowIsTrueAndAvailableAmountGreaterThan(criteria.getMinPublishYear(), criteria.getMaxPublishYear(),0, pageable);
        }

        return getPagedBooksDTO(pagedBooks);
    }

    public PagedBooksDTO getAllBooksToRemove(Pageable pageable) {
        logger.info("Fetching books to remove");
        Page<BookEntity> pagedBooks = bookRepository.findAllWithNoOwnerUser(pageable);

        return getPagedBooksDTO(pagedBooks);
    }

    public List<BookTypeDTO> getAllBookTypes() {
        logger.info("Fetching all book types");
        List<BookTypeEntity> bookTypes = bookTypeRepository.findAll();

        return bookTypeMapper.toDTOs(bookTypes);
    }

    public BookDTO save(BookDTO bookToSave) {
        logger.info("Saving new book: title={}, author={}", bookToSave.title(), bookToSave.author());
        BookEntity savedBook = bookRepository.save(bookMapper.toEntity(bookToSave));

        return bookMapper.toDTO(savedBook);
    }

    @Transactional
    public void borrowBook(int bookId) {
        logger.info("Attempting to borrow book with id: {}", bookId);
        BookEntity book = bookRepository.findById(bookId)
            .orElseThrow(EntityNotFoundException::new);

        String username = securityContextAccessor.getAuthentication().getName();

        boolean isAlreadyBorrowed = book.getOwnerUsers().stream().anyMatch(user -> user.getEmail().equals(username));
        if(isAlreadyBorrowed) {
            logger.warn("User {} attempted to borrow book {} which is already borrowed by them", username, bookId);
            throw new ProductAlreadyBorrowedException();
        }

        book.getOwnerUsers().add(userRepository.findByEmail(username).orElseThrow(IllegalStateException::new));
        book.setAvailableAmount(book.getAvailableAmount()-1);
        logger.info("Book {} successfully borrowed by user {}", bookId, username);
    }

    @Transactional
    public void returnBook(int bookId) {
        logger.info("Attempting to return book with id: {}", bookId);
        BookEntity book = bookRepository.findById(bookId)
            .orElseThrow(EntityNotFoundException::new);

        String username = securityContextAccessor.getAuthentication().getName();

        UserEntity user = book.getOwnerUsers().stream()
            .filter(u -> u.getEmail().equals(username))
            .findAny()
            .orElseThrow(EntityNotFoundException::new);

        book.getOwnerUsers().remove(user);
        book.setAvailableAmount(book.getAvailableAmount()+1);
        logger.info("Book {} successfully returned by user {}", bookId, username);
    }

    @Transactional
    public BookDTO update(BookDTO updatedBook) {
        logger.info("Updating book with id={}: title={}, author={}", updatedBook.id(), updatedBook.title(), updatedBook.author());
        BookEntity bookEntity = bookRepository.findById(updatedBook.id())
            .orElseThrow(EntityNotFoundException::new);

        bookMapper.updateEntity(bookEntity, updatedBook);

        return bookMapper.toDTO(bookEntity);
    }

    public void delete(int id) {
        logger.warn("Deleting book with id: {}", id);
        BookEntity bookToDelete = bookRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        bookRepository.delete(bookToDelete);
    }

    private PagedBooksDTO getPagedBooksDTO(Page<BookEntity> pagedBooks) {
        List<BookDTO> books = bookMapper.toDTOs(pagedBooks.getContent());

        return new PagedBooksDTO(pagedBooks.getTotalPages(), books);
    }

}
