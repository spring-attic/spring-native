package app.main.hierarchy;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// needs aot proxy
@Transactional
@Component
public class TransactionalComponent {

    public void foo() {

    }

}
