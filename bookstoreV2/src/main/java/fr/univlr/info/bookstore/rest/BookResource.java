package fr.univlr.info.bookstore.rest;

import fr.univlr.info.bookstore.domain.Book;
import fr.univlr.info.bookstore.domain.Person;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;

@Path("/api/")
public class BookResource {
    // fake database
    private static Map<String, Book> books = new HashMap<>();
    private static Map<String, Person> authors = new HashMap<>();
    static {
        Person pierre = new Person("Pierre","Durand");
        Person paul = new Person("Paul","Martin");
        authors.put("pierre", pierre);
        authors.put("paul", paul);
        Book book1 = new Book("ZT56", "Essai", Arrays.asList(pierre,paul), 12.4f);
        Book book2 = new Book("ZT57", "Roman", Arrays.asList(pierre), 8f);
        books.put("ZT56", book1);
        books.put("ZT57", book2);
    }

    /***
     * Renvoie le livre spécifique
     * @param isbn
     * @return
     */
    @GET
    @Path("/book/{isbn}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Book getBook(@PathParam("isbn") String isbn) {
        Book b = books.get(isbn);
        if (b != null) {
            return b;
        } else {
            throw new NotFoundException("Book does not exist");
        }
    }

    /***
     * Renvoie tous les livres
     * @return
     */
    @GET
    @Path("/book")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Set<Book> getAllBook() {
        Set<Book> c = new HashSet<>();
        c.addAll(books.values());

        return c;

    }

    /***
     * Ajoute un livre à la bibliothèque
     * @param b
     * @param uriInfo
     * @return
     */
    @POST
    @Path("/book")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addBook(Book b, @Context UriInfo uriInfo) {

        if (books.containsKey(b.getIsbn())) {
            return Response.status(Response.Status.CONFLICT).build();
        } else {
            books.put(b.getIsbn(), b);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI isbnUri = ub.path(b.getIsbn()).build();
            return Response.created(isbnUri).build();
        }
    }

    /***
     * Supprime le livre de la bibliothèque
     * @param isbn
     * @return
     */
    @DELETE
    @Path("/book/{isbn}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteBook(@PathParam("isbn") String isbn) {
        if (books.containsKey(isbn)) {
            books.remove(isbn);
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            throw new NotFoundException("Book does not exist");
        }
    }

    /***
     * Supprime tous les livres de la bibliothèque
     * @param uriInfo
     * @return
     */
    @DELETE
    @Path("/book")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteAllBook(@Context UriInfo uriInfo) {
        books.clear();
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        return Response.ok().build();
    }


    /***
     * Met à jour un livre dans la bibliothèque
     * @param b
     * @param isbn
     * @param uriInfo
     * @return
     */
    @PUT
    @Path("/book/{isbn}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateBook(Book b, @PathParam("isbn") String isbn, @Context UriInfo uriInfo) {

        if (!books.containsKey(isbn)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (!b.getIsbn().equals(isbn)) {
            return Response.status(422).build();
        }

        books.remove(isbn);
        books.put(b.getIsbn(), b);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        return Response.status(Response.Status.NO_CONTENT).build();

    }

    /***
     * Renvoie tous les auteurs d'un livres spécifié
     * @param isbn
     * @return
     */
    @GET
    @Path("/book/{isbn}/authors")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Set<Person> getAllAuthors(@PathParam("isbn") String isbn) {
        Set<Person> p = new HashSet<>();
        p.addAll(books.get(isbn).getAuthors());

        return p;

    }
}