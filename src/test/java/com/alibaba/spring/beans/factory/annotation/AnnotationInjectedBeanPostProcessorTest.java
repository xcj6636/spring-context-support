/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.spring.beans.factory.annotation;

import com.alibaba.spring.util.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.annotation.*;

/**
 * {@link AnnotationInjectedBeanPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        AnnotationInjectedBeanPostProcessorTest.TestConfiguration.class,
})
public class AnnotationInjectedBeanPostProcessorTest {

    @Autowired
    @Qualifier("parent")
    private TestConfiguration.Parent parent;

    @Autowired
    @Qualifier("child")
    private TestConfiguration.Child child;

    @Autowired
    private TestConfiguration.UserHolder userHolder;

    @Autowired
    private AnnotationInjectedBeanPostProcessor processor;

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Test
    public void testAnnotationInjectedBeanPostProcessor() {

        Assert.assertEquals(environment, processor.getEnvironment());
        Assert.assertEquals(beanFactory.getBeanClassLoader(), processor.getClassLoader());
        Assert.assertEquals(beanFactory, processor.getBeanFactory());
        Assert.assertEquals(Referenced.class, processor.getAnnotationType());
        Assert.assertEquals(Ordered.LOWEST_PRECEDENCE, processor.getOrder());
        Assert.assertEquals(1, processor.getBeans().size());
        Assert.assertTrue(processor.getBeans().contains(parent.parentUser));

    }

    @Test

    public void testReferencedUser() {
        Assert.assertEquals("mercyblitz", parent.user.getName());
        Assert.assertEquals(32, parent.user.getAge());
        Assert.assertEquals(parent.user, parent.parentUser);
        Assert.assertEquals(parent.user, child.childUser);
        Assert.assertEquals(parent.user, userHolder.user);
    }

    static class TestConfiguration {

        static class Parent {

            @Referenced
            private User parentUser;

            private User user;

            @Referenced
            public void setUser(User user) {
                this.user = user;
            }
        }

        static class Child extends Parent {

            @Referenced
            private User childUser;

        }

        static class UserHolder {

            private User user;
        }


        @Bean
        public Parent parent() {
            return new Parent();
        }

        @Bean
        public Child child() {
            return new Child();
        }


        @Bean
        public User user() {
            User user = new User();
            user.setName("mercyblitz");
            user.setAge(32);
            return user;
        }

        @Bean
        public UserHolder userHolder(User user) {
            UserHolder userHolder = new UserHolder();
            userHolder.user = user;
            return userHolder;
        }

        @Bean
        public ReferencedPostProcessor processor() {
            return new ReferencedPostProcessor();
        }

    }


    @Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Referenced {
    }

    private static class ReferencedPostProcessor extends AnnotationInjectedBeanPostProcessor<Referenced, User> {


        @Override
        protected User resolveInjectedBean(Referenced annotation, Class<User> beanClass,
                                           ConfigurableListableBeanFactory beanFactory, Environment environment,
                                           ClassLoader classLoader) throws Exception {
            return beanFactory.getBean(beanClass);
        }

        @Override
        protected String generateInjectedBeanCacheKey(Referenced annotation, Class<User> beanClass,
                                                      ConfigurableListableBeanFactory beanFactory,
                                                      Environment environment, ClassLoader classLoader) {
            return beanClass.getName();
        }
    }

}