package app.main.model;

public class Foo {

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
		return String.format("Foo[name='%s']", name);
	}

}
