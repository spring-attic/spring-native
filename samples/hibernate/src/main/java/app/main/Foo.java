package app.main;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Foo {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;

	private String name;

	public Foo() {
	}

	public Foo(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format(
				"Foo[id=%d, name='%s']",
				id, name);
	}
}
