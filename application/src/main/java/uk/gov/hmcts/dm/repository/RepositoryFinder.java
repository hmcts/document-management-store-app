package uk.gov.hmcts.dm.repository;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.dm.exception.RepositoryCouldNotBeFoundException;

import java.io.Serializable;
import java.util.Iterator;
import javax.validation.constraints.NotNull;

@Component
public class RepositoryFinder {

    @Autowired
    private ListableBeanFactory listableBeanFactory;

    public CrudRepository<Object, Serializable> find(@NotNull String domainClassName) {
        try {
            return this.find(Class.forName(domainClassName));
        } catch (ClassNotFoundException e) {
            throw new RepositoryCouldNotBeFoundException("Could not find a Repository for Domain class: " + domainClassName);
        }
    }

    @SuppressWarnings(value = "unchecked")
    public CrudRepository<Object, Serializable> find(@NotNull Class<?> domainClass) {
        Repositories repositories = new Repositories(listableBeanFactory);

        Iterator<Class<?>> it = repositories.iterator();
        while (it.hasNext()) {
            Object repository = repositories.getRepositoryFor(domainClass);
            if (repository != null && repository instanceof CrudRepository) {
                return (CrudRepository<Object, Serializable>)repository;
            }
        }

        return null;
    }

}
