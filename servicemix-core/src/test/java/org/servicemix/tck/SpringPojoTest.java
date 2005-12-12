package org.servicemix.tck;

import org.servicemix.examples.SpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class SpringPojoTest extends SpringTestSupport {

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/servicemix/examples/spring-pojo.xml");

    }
}
