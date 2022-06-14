package app.main.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * @author Moritz Halbritter
 */
@Entity
//@NamedEntityGraph(name = "Book.authors",
//		attributeNodes = @NamedAttributeNode("authors")
//)
public class Book {
	@Id
	@GeneratedValue
	private Long id;

	@Column
	private String title;

	@OneToMany(cascade = CascadeType.ALL)
	private Set<Author> authors = new HashSet<>();

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<Author> getAuthors() {
		return authors;
	}

	@Override
	public String toString() {
		return "Book{" +
				"id=" + id +
				", title='" + title + '\'' +
				", authors=" + authors +
				'}';
	}
}
