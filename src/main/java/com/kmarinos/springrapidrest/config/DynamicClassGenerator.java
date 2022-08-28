package com.kmarinos.springrapidrest.config;

import static net.bytebuddy.matcher.ElementMatchers.named;

import com.kmarinos.springrapidrest.domain.EntityHistoryFactory;
import com.kmarinos.springrapidrest.domain.HistoryChangeListener;
import com.kmarinos.springrapidrest.domain.model.TrackedEntity;
import com.kmarinos.springrapidrest.domain.model.TrackedEntityHistory;
import com.kmarinos.springrapidrest.domain.repository.ExpandedTrackedEntityHistoryRepository;
import com.kmarinos.springrapidrest.domain.repository.TrackedEntityHistoryRepository;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Loaded;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.hibernate.annotations.Target;
import org.reflections.Reflections;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Slf4j
class DynamicClassGenerator {

    private List<Class<?>> entityClasses = new ArrayList<>();
    public DynamicClassGenerator(){
        Reflections reflections = new Reflections("com.kmarinos.springrapidrest");
        reflections.getTypesAnnotatedWith(Entity.class).forEach(entityClasses::add);
    }
    /***
     * Creates the below class dynamically and loads it into the ClassLoader as
     * well as saves the .class file on the disk:
     *
     *
     * @param entityClassName
     * @param entityListener
     */

    public Optional<Class<?>> createJpaEntity(String entityClassName, Class<?> entityType, Class<?> entityListener) {
        if (classFileExists(entityClassName)) {
            log.info("The Entity class " + entityClassName + " already exists, not creating a new one");
            return Optional.empty();
        }
        log.info("Creating new Entity class: {}...", entityClassName);
        var tableName = camelToSnake(entityClassName.substring(entityClassName.lastIndexOf(".")+1).trim());
        Unloaded<?> generatedClass = null;
        var builder = new ByteBuddy().with(TypeValidation.DISABLED)
                .subclass(Generic.Builder.parameterizedType(TrackedEntityHistory.class, entityType).build())
                .annotateType(AnnotationDescription.Builder.ofType(Entity.class).build()
                )
                .defineProperty("entity", TrackedEntity.class).annotateField(AnnotationDescription.Builder.ofType(ManyToOne.class).build())
                .annotateField(AnnotationDescription.Builder.ofType(Target.class).define("value",entityType).build())
                .annotateType(AnnotationDescription.Builder.ofType(EntityListeners.class).defineTypeArray("value",entityListener, AuditingEntityListener.class).build())

                .name(entityClassName);
        for(Field field : entityType.getDeclaredFields()){
            log.info("Trying for field {}",field.getName());
            //ignore transient fields
            if(!field.isAnnotationPresent(Transient.class)){
                log.info("Field is not transient");
                if(field.isAnnotationPresent(OneToMany.class)||field.isAnnotationPresent(ManyToMany.class)){
                    log.info("Field is collection. Skipping...");
                    continue;
                }
                //only save the id of other entities
                if(entityClasses.contains(field.getType())){

                 builder =builder.defineProperty(field.getName()+"Id",TypeDescription.STRING)
                         .annotateField(AnnotationDescription.Builder.ofType(Column.class).define("name",camelToSnake(field.getName()+"Id")).build());
                    log.info("Set only id because field is entity");
                }else{
                    builder =builder.defineProperty(field.getName(),field.getType()).annotateField(field.getAnnotations());
                    log.info("Set field");
                }
            }else{
                log.info("Field is transient. Skipping...");
            }
        }
        generatedClass=builder.make();

        return Optional.of(saveGeneratedClassAsFile(generatedClass));
    }

    /***
     * Creates the below class dynamically and loads it into the ClassLoader as
     * well as saves the .class file on the disk:
     *
     * <pre>
     * public interface BookDao extends BookDaoTemplate, CrudRepository&lt;Book, Integer&gt; {
     *
     *     &#64;Override
     *     &#64;Transactional
     *     &#64;Modifying
     *     &#64;Query("update Book set author.id = :authorId where id = :bookId")
     *     int updateAuthor(int bookId, int authorId);
     *
     * }
     * </pre>
     *
     * @param repositoryClassName
     * @param entityClass
     */
    public Optional<Class<?>> createJpaRepository(Class<?> entityClass, String repositoryClassName) {
        if (classFileExists(repositoryClassName)) {
            log.info("The Repository class " + repositoryClassName + " already exists, not creating a new one");
            return Optional.empty();
        }

        log.info("Creating new Repo class: {}...", repositoryClassName);

        Generic crudRepo = Generic.Builder.parameterizedType(CrudRepository.class, entityClass, String.class).build();

        Unloaded<?> generatedClass = new ByteBuddy().makeInterface(crudRepo).implement(
                TrackedEntityHistoryRepository.class).implement(
                ExpandedTrackedEntityHistoryRepository.class)
                                .method(ElementMatchers.named("findAllByEntity")).withoutCode()
                .annotateMethod(AnnotationDescription.Builder.ofType(Query.class)
                        .define("value",
                                "select h from " + entityClass.getSimpleName()
                                        + " h where h.entity = :entity")
                        .build())
                .name(repositoryClassName).make();

        return Optional.of(saveGeneratedClassAsFile(generatedClass));
    }
    public Optional<Class<?>> createEntityListener(String listenerToCreate,Class<?> historyClass){
        if (classFileExists(listenerToCreate)) {
            log.info("The Repository class " + listenerToCreate + " already exists, not creating a new one");
            return Optional.empty();
        }
        log.info("Creating listener {}...",listenerToCreate);
        Unloaded<?> generatedClass = new ByteBuddy()
                .with(TypeValidation.DISABLED)
                .subclass(TypeDescription.Generic.Builder.parameterizedType(HistoryChangeListener.class, historyClass).build(), ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
                .annotateType(AnnotationDescription.Builder.ofType(Component.class).build())
                .method(named("updateEntityCallback"))
                .intercept(SuperMethodCall.INSTANCE)
                .annotateMethod(AnnotationDescription.Builder.ofType(PrePersist.class).build())
                .name(listenerToCreate).make();
        return Optional.of(saveGeneratedClassAsFile(generatedClass));
    }
    public Optional<Class<?>> createEntityFactoryImpl(String className){
        if (classFileExists(className)) {
            log.info("The Repository class " + className + " already exists, not creating a new one");
            return Optional.empty();
        }
        Unloaded<?> generatedClass = new ByteBuddy()
                .subclass(EntityHistoryFactory.class)
                .annotateType(AnnotationDescription.Builder.ofType(Component.class).build())
                .name(className)
                .make();
        return Optional.of(saveGeneratedClassAsFile(generatedClass));
    }

    private boolean classFileExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private Class<?> saveGeneratedClassAsFile(Unloaded<?> unloadedClass) {

        Loaded<?> loadedClass = unloadedClass.load(getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION);

        try {
            loadedClass.saveIn(new File("target/classes"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return loadedClass.getLoaded();

    }

    private static String camelToSnake(String str) {

        // Empty String
        String result = "";

        // Append first character(in lower case)
        // to result string
        char c = str.charAt(0);
        result = result + Character.toLowerCase(c);

        // Traverse the string from
        // ist index to last index
        for (int i = 1; i < str.length(); i++) {

            char ch = str.charAt(i);

            // Check if the character is upper case
            // then append '_' and such character
            // (in lower case) to result string
            if (Character.isUpperCase(ch)) {
                result = result + '_';
                result
                        = result
                        + Character.toLowerCase(ch);
            }

            // If the character is lower case then
            // add such character into result string
            else {
                result = result + ch;
            }
        }

        // return the result
        return result;
    }

}
