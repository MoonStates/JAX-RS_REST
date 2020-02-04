package fr.univlr.info.bookstore;

import fr.univlr.info.bookstore.domain.Book;
import fr.univlr.info.bookstore.domain.Person;
import fr.univlr.info.bookstore.rest.PersonResource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RunWith(Arquillian.class)
@RunAsClient
// tests sorted by alphabetic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersonResourceTest {
    private static WebTarget wt = ClientBuilder.newClient().target("http://localhost:8080/api/author");

    // needed by Arquillian test framework
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "testAuthor.war")
                .addPackage(PersonResource.class.getPackage())
                .addPackage(Person.class.getPackage());
    }

    @Test
    public void getAllAuthorsTest() {
        Set<Person> actualAuthors = wt.request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Set<Person>>() {
                });

        Assert.assertEquals("Get all authors failed: ", 2, actualAuthors.size());

        // compare authors
        Person pierre = new Person("Pierre","Durand");
        Person paul = new Person("Paul","Martin");
        Set<Person> expectedAuthors = new HashSet<>(Arrays.asList(pierre,paul));
        Assert.assertEquals(expectedAuthors, actualAuthors);
    }

    @Test
    public void getSpecificAuthorsTest() {
        Set<Person> authors = wt.queryParam("firstname", "Pierre").
                queryParam("lastname", "Durand").request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Set<Person>>() {
                });

        Assert.assertEquals("Get author Pierre Durand failed: ", 1, authors.size());

        // compare authors
        Person pierre = new Person("Pierre","Durand");
        Set<Person> expectedAuthors = new HashSet<>(Arrays.asList(pierre));
        Assert.assertEquals(expectedAuthors, authors);
    }

    @Test
    public void getBooksFromSpecificAuthorTest() {
        Set<Book> books = wt.path("/book").queryParam("firstname", "Pierre").
                queryParam("lastname", "Durand").request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Set<Book>>() {
                });

        Assert.assertEquals("Get books from Pierre Durand failed: ", 2, books.size());
    }
}
