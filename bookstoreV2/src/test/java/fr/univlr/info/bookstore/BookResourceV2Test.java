 package fr.univlr.info.bookstore;

import fr.univlr.info.bookstore.domain.Book;
import fr.univlr.info.bookstore.domain.Person;
import fr.univlr.info.bookstore.rest.BookResource;
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

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(Arquillian.class)
@RunAsClient
// tests sorted by alphabetic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookResourceV2Test {

    // fake database
    private static Map<String, Book> allBooks = new HashMap<>();
    private static Map<String, Person> allAuthors = new HashMap<>();
    static {
        Person pierre = new Person("Pierre","Durand");
        Person paul = new Person("Paul","Martin");
        allAuthors.put("pierre", pierre);
        allAuthors.put("paul", paul);
        Book book1 = new Book("ZT56", "Essai", Arrays.asList(pierre,paul), 12.4f);
        Book book2 = new Book("ZT57", "Roman", Arrays.asList(pierre), 8f);
        allBooks.put("ZT56", book1);
        allBooks.put("ZT57", book2);
    }
    private static WebTarget wt = ClientBuilder.newClient().target("http://localhost:8080/api/book");

    // needed by Arquillian test framework
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "testBook.war")
                .addPackage(BookResource.class.getPackage())
                .addPackage(Book.class.getPackage());
    }

    @Test
    public void getAllBookTest() {
        Set<Book> actualBooks = wt.request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Set<Book>>() {
                });

        Assert.assertEquals("Get all books failed: ", 2, actualBooks.size());

        // compare books
        Set<Book> expectedBooks = new HashSet<>(allBooks.values());
        Assert.assertEquals(expectedBooks, actualBooks);
    }

    @Test
    public void getSpecificBookTest1() {
        // Given an existing resource
        String bookId = "ZT56";

        Book actualBook = wt.path(bookId).request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Book>() {
                });

        Book expectedBook = allBooks.get(bookId);
        Assert.assertEquals("Get an existing book failed:",expectedBook, actualBook);
    }

    @Test
    public void getSpecificBookTest2() {
        // Given unknown resource
        String bookId = "ZT60";
        try {
            Book actualBook = wt.path(bookId).request(
                    MediaType.APPLICATION_JSON).
                    get(new GenericType<Book>() {
                    });
            Assert.fail("Get an unknown book with isbn: " + bookId);
        } catch (NotFoundException nae) {
            // OK...
        }
    }

    @Test
    public void getSpecificBookTestAuthors() {
        String bookId = "ZT56";
        Collection<Person> authors = wt.path(bookId).path("authors").request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Collection<Person>>() {
                });
        Assert.assertEquals("Get authors for book " + bookId +" failed: ", 2, authors.size());

        // compare authors
        Set<Person> expectedAuthors = new HashSet<>(allBooks.get("ZT56").getAuthors());
        Set<Person> actualAuthors = new HashSet<>(authors);
        Assert.assertEquals(expectedAuthors, actualAuthors);
    }

    @Test
    public void newBookTest1() {
        Book book = new Book("ZT59", "Nouvelle", Arrays.asList(allAuthors.get("paul")), 5f);
        Response resp = wt.request(
                MediaType.APPLICATION_XML).
                post(Entity.entity(book, MediaType.APPLICATION_XML));
        Assert.assertEquals("Create a new book failed: ",Response.Status.CREATED, resp.getStatusInfo().toEnum());
        Assert.assertEquals("HTTP location header incorrect:","/api/book/ZT59", resp.getLocation().getPath());

        // Given the new book id
        String bookId = "ZT59";
        Book actualBook = wt.path(bookId).request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Book>() {
                });
        Assert.assertEquals("Get a new created book failed: ",book, actualBook);
    }

    @Test
    public void newBookTest2() {
        // test error management with an existing book
        Book book = new Book("ZT56", "Essai", Arrays.asList(allAuthors.get("pierre")), 12.4f);
        Response resp = wt.request(
                MediaType.APPLICATION_XML).
                post(Entity.entity(book, MediaType.APPLICATION_XML));
        Assert.assertEquals("Creating an already existing book: ", Response.Status.CONFLICT, resp.getStatusInfo().toEnum());
    }

    @Test
    public void removeBookTest1() {
        String bookId = "ZT59";
        Response resp = wt.path(bookId).request(
                MediaType.APPLICATION_JSON).
                delete();
        Assert.assertEquals("Deleting an existing book failed:", Response.Status.NO_CONTENT, resp.getStatusInfo().toEnum());

        try {
            wt.path(bookId).request(
                    MediaType.APPLICATION_JSON).
                    get(new GenericType<Book>() {
                    });
            Assert.fail("Get a deleted book with isbn: " + bookId);
        } catch (NotFoundException nae) {
            // OK...
        }
    }

    @Test
    public void updateBookTest1() {
        // Given
        Book book = new Book("ZT56", "Roman", Arrays.asList(allAuthors.get("paul")), 12.3f);
        String bookId = book.getIsbn();
        // updating the book ZT56
        Response resp = wt.path(bookId).request(
                MediaType.APPLICATION_XML).
                put(Entity.entity(book, MediaType.APPLICATION_XML));
        Assert.assertEquals("Updating an existing book failed: ",Response.Status.NO_CONTENT, resp.getStatusInfo().toEnum());

        // test the updated book
        Book updatedBook = wt.path(bookId).request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Book>() {
                });
        Assert.assertEquals("Get an updated book failed: ",book, updatedBook);
    }

    @Test
    public void updateBookTest2() {
        // test error management with incoherent data
        // Given
        Book book = new Book("ZT57", "Essai", Arrays.asList(allAuthors.get("pierre")), 12.4f);
        String bookId = "ZT56"; // different isbn
        Response resp = wt.path(bookId).request(
                MediaType.APPLICATION_XML).
                put(Entity.entity(book, MediaType.APPLICATION_XML));
        Assert.assertEquals("Incoherent data during update: ",422, resp.getStatus());
    }

    @Test
    public void updateBookTest3() {
        // test error management with non-existent book
        Book book = new Book("ZT60", "Essai", Arrays.asList(allAuthors.get("pierre")), 12.4f);
        String bookId = "ZT60";
        Response resp = wt.path(bookId).request(
                MediaType.APPLICATION_XML).
                put(Entity.entity(book, MediaType.APPLICATION_XML));
        Assert.assertEquals("Updating a non-existent book: ",Response.Status.NOT_FOUND, resp.getStatusInfo().toEnum());
    }

    // last test
    @Test
    public void zzz_removeBookTest2() {
        // remove all books...
        Response resp = wt.request(
                MediaType.APPLICATION_JSON).
                delete();

        Set<Book> actualBooks = wt.request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Set<Book>>() {
                });
        Assert.assertEquals("Get no books failed: ", 0, actualBooks.size());
    }

}
