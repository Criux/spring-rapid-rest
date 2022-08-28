package com.kmarinos.springrapidrest.config;

import com.kmarinos.springrapidrest.domain.EntityHistoryFactory;
import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;


@Configuration
@Slf4j
public class DynamicJpaBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private static final String ENTITY_CLASS_SUFFIX = "History";
    private static final String LISTENER_CLASS_SUFFIX = "Listener";
    private static final String REPO_CLASS_SUFFIX = "Repository";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        DynamicClassGenerator dynamicClassGenerator = new DynamicClassGenerator();
        String packageName = "com.kmarinos.springrapidrest.domain.dynamic";
        Reflections reflections = new Reflections("com.kmarinos.springrapidrest");
        Set<Class<? extends TrackedEntity>> classes = reflections.getSubTypesOf(TrackedEntity.class);
        classes.forEach(referenceEntity -> {
            String classNameToCreate = referenceEntity.getSimpleName() + ENTITY_CLASS_SUFFIX;
            String listenerNameToCreate = referenceEntity.getSimpleName() + ENTITY_CLASS_SUFFIX + LISTENER_CLASS_SUFFIX;
            String repoNameToCreate = referenceEntity.getSimpleName() + ENTITY_CLASS_SUFFIX + REPO_CLASS_SUFFIX;

            EntityHistoryFactory.classMap.put(referenceEntity,packageName + ".model." + classNameToCreate);
            var entityListener = dynamicClassGenerator.createEntityListener(packageName + ".entitylistener." + listenerNameToCreate, referenceEntity);

            if (entityListener.isEmpty()) {
                return;
            } else {
                log.info("Created listener {}", entityListener.get().getName());
            }

            Optional<Class<?>> entityClass = null;
            entityClass = dynamicClassGenerator.createJpaEntity(packageName + ".model." +
                    classNameToCreate, referenceEntity, entityListener.get());
            if (entityClass.isEmpty()) {
                return;
            }

            Optional<Class<?>> repoClass =
                    dynamicClassGenerator.createJpaRepository(entityClass.get(), packageName + ".repository." + repoNameToCreate);

            if (repoClass.isEmpty()) {
                return;
            }

            log.info("Created the Entity class {} and Repository class {} successfully", classNameToCreate,
                    repoNameToCreate);

            registerJpaRepositoryFactoryBean(repoClass.get(), (DefaultListableBeanFactory) beanFactory);
        });
    }

    /**
     * Registers a {@link JpaRepositoryFactoryBean} similar to below:
     *
     * <pre>
     * &#64;Bean
     * public JpaRepositoryFactoryBean<BookDao, Book, Integer> bookRepository() {
     *     return new JpaRepositoryFactoryBean<>(BookDao.class);
     * }
     * </pre>
     * <p>
     * Since the generic arguments are not necessary, these are ignored.
     *
     * @param jpaRepositoryClass
     * @param defaultListableBeanFactory
     */
    private void registerJpaRepositoryFactoryBean(Class<?> jpaRepositoryClass,
                                                  DefaultListableBeanFactory defaultListableBeanFactory) {
        String beanName = jpaRepositoryClass.getSimpleName();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
                .rootBeanDefinition(JpaRepositoryFactoryBean.class).addConstructorArgValue(jpaRepositoryClass);
        defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());

        log.info("Registered the {} bean for {} successfully", JpaRepositoryFactoryBean.class.getSimpleName(),
                beanName);
    }

}
