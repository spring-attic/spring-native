package app.main.model;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class Foo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;
	private String value;
	@OneToOne(cascade = CascadeType.ALL)
	private Flurb flurb;
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

	public Flurb getFlurb() {
		return flurb;
	}

	public void setFlurb(Flurb flurb) {
		this.flurb = flurb;
	}
}
