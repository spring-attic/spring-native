package app.main.model;

public class Foo {
    private long id;
	private String value;
	public Foo() {
	}
	public Foo(String value) {
		this.value = value;
	}
	public String getValue() {
		return this.value;
	}
	public void setValue(String value) {
		this.value = value;
	}
    @Override
    public String toString() {
        return String.format(
                "Foo[id=%d, value='%s']",
                id, value);
    }
}
