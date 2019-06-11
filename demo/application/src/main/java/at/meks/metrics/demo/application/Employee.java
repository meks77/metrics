package at.meks.metrics.demo.application;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "EMPLOYEE")
@NamedQuery(name = Employee.QUERY_FIND_ALL, query = "SELECT e FROM Employee e")
public class Employee implements Serializable {

    static final String QUERY_FIND_ALL = "Employee.findAll";

    @Id
    private int id;

    @Column(length = 50)
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
