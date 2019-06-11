package at.meks.metrics.demo.application;

import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Named
@Dependent
public class EmployeeRepository {

    @PersistenceContext
    private EntityManager em;

    List<Employee> all() {
        return em.createNamedQuery(Employee.QUERY_FIND_ALL, Employee.class).getResultList();
    }
}
