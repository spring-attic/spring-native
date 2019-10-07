#!/bin/bash

if ! [ "$1" == "" ]; then 
    cd $1; shift
fi

n=100
if ! [ "$1" == "" ]; then 
    n=$1; shift
fi

base=src/main/java
pkg=app/main/model
if ! [ "$1" == "" ]; then 
    pkg=$1; shift
fi

mkdir -p $base/$pkg

foo=$base/$pkg/Foo.java
imprt=`echo $pkg | sed -e s,/,.,g`
if ! [ -e $foo ]; then
    cat <<EOF > $foo
package ${imprt};
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Foo {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
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
EOF
fi

for f in $(seq 1 $n); do
    target=${foo/Foo/Foo$f}
    sed -e "s/Foo/Foo$f/g" $foo > $target
done
