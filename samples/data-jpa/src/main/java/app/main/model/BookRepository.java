package app.main.model;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author Moritz Halbritter
 */
public interface BookRepository extends JpaRepository<Book, Long> {
//	@EntityGraph(value = "Book.authors")
//	@Query("SELECT b FROM Book b WHERE b.title = :title")
//	Book findByTitleWithNamedGraph(String title);

	@EntityGraph(attributePaths = "authors")
	@Query("SELECT b FROM Book b WHERE b.title = :title")
	Book findByTitleWithAdHocGraph(String title);
}
