package app.backend.book;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository repository;
    private final BookMapper mapper;

    public List<BookDTO> getAllBooks() {
        List<BookEntity> books = repository.findAll();

        return mapper.toDTOs(books);
    }

    public List<BookDTO> findByUserId(int id) {
        List<BookEntity> books = repository.getByOwnerUsers_Id(id);

        return mapper.toDTOs(books);
    }

    public BookDTO findById(int id) {
        BookEntity book = repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        return mapper.toDTO(book);
    }

    public List<BookDTO> findBySortingCriteria(BookSortingCriteria criteria) {
        List<BookEntity> books = repository.findByPublishYearBetweenAndBookType_NameIgnoreCase(criteria.getMinPublishYear(), criteria.getMaxPublishYear(), criteria.getTypeName());

        return mapper.toDTOs(books);
    }

    public BookDTO save(BookDTO bookToSave) {
        BookEntity savedBook = repository.save(mapper.toEntity(bookToSave));

        return mapper.toDTO(savedBook);
    }

    public void delete(int id) {
        BookEntity bookToDelete = repository.findById(id)
                .orElseThrow(EntityNotFoundException::new);

        repository.delete(bookToDelete);
    }


}
