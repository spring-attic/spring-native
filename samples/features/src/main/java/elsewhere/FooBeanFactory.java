package elsewhere;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.commandlinerunner.FooBean;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

@Configuration
public class FooBeanFactory {
    @Bean
    public FooBean fooBeanFactory() {
        return new FooBean(); 
    }

    @Lazy
    @Bean
    public LazyBean lazyBean() {
        return new LazyBean();
    }

    @Lazy
    @Bean
    public AnotherLazyBean anotherLazyBean() {
        return new AnotherLazyBean();
    }

    @Lazy
    @Bean
    public AnotherLazyBeanHolder anotherLazyBeanHolder(ObjectProvider<AnotherLazyBean> provider) {
        AnotherLazyBean.messages.add("before-provider");
        AnotherLazyBean bean = provider.getIfAvailable();
        Assert.notNull(bean, "lazyProviderBean must not be null");
        AnotherLazyBean.messages.add("after-provider");
        return new AnotherLazyBeanHolder(bean);
    }

    public static class LazyBean {

        public static final List<String> messages = Collections.synchronizedList(new ArrayList<>());

        public LazyBean() {
            messages.add("created");
        }

    }

    public static class AnotherLazyBean {

        public static final List<String> messages = Collections.synchronizedList(new ArrayList<>());

        public AnotherLazyBean() {
            messages.add("created");
        }

    }

    public static class AnotherLazyBeanHolder {

        private final AnotherLazyBean bean;

        public AnotherLazyBeanHolder(AnotherLazyBean bean) {
            AnotherLazyBean.messages.add("holder-created");
            this.bean = bean;
        }

    }
}
