package fr.univlr.info.bookstore;

import fr.univlr.info.bookstore.domain.Book;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

@RunWith(Arquillian.class)
@RunAsClient
// tests sorted by alphabetic order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookResourceTest {
    // fake database
    private static Map<String, Book> allBooks = new HashMap<>();
    static {
        allBooks.put("ZT56", new Book("ZT56", "Essai", "pierre", 12.4f));
        allBooks.put("ZT57", new Book("ZT57", "Roman", "pierre", 8f));
    }

    // REST api URL
    private static WebTarget wt = ClientBuilder.newClient().target("http://localhost:8080/api/book");

    // needed by Arquillian test framework
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage(BookResource.class.getPackage())
                .addPackage(Book.class.getPackage());
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
    public void newBookTest1() {
        Book book = new Book("ZT59", "Nouvelle", "paul", 5f);
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
        Book book = new Book("ZT56", "Essai", "pierre", 12.4f);
        Response resp = wt.request(
                MediaType.APPLICATION_XML).
                post(Entity.entity(book, MediaType.APPLICATION_XML));
        Assert.assertEquals("Creating an already existing book: ", Response.Status.NOT_ACCEPTABLE, resp.getStatusInfo().toEnum());
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
        Book book = new Book("ZT56", "Roman", "paul", 12.3f);
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
        Book book = new Book("ZT57", "Essai", "pierre", 12.4f);
        String bookId = "ZT56"; // different isbn
        Response resp = wt.path(bookId).request(
                MediaType.APPLICATION_XML).
                put(Entity.entity(book, MediaType.APPLICATION_XML));
        Assert.assertEquals("Incoherent data during update: ",Response.Status.CONFLICT, resp.getStatusInfo().toEnum());
    }

    @Test
    public void updateBookTest3() {
        // test error management with non-existent book
        // Given
        Book book = new Book("ZT60", "Essai", "pierre", 12.4f);
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
        Response resp = wt.request(MediaType.APPLICATION_JSON).delete();

        System.err.print("-------------------------------------------------"+wt.getUri()+"\n");
        Set<Book> actualBooks = wt.request(
                MediaType.APPLICATION_JSON).
                get(new GenericType<Set<Book>>() {
                });
        Assert.assertEquals("Get no books failed: ", 0, actualBooks.size());
    }

}