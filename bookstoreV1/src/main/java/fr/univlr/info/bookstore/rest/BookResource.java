package fr.univlr.info.bookstore.rest;

import fr.univlr.info.bookstore.domain.Book;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.*;

@Path("/api/")
public class BookResource {
    // pseudo database
    private static Map<String, Book> books = new HashMap<>();

    static {
        books.put("ZT56", new Book("ZT56", "Essai", "pierre", 12.4f));
        books.put("ZT57", new Book("ZT57", "Roman", "pierre", 8f));
    }

    /***
     * Cherche un livre à partir de son identifiant dans la bibliothèque
     * @param isbn
     * @return un livre s'il existe dans la bibliothèque
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
     * Renvoie la liste de tous les livres de la bibliothèque
     * @return set de livre
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
     * Ajoute un livre dans la bibliothèque s'il n'existe pas
     * @param b
     * @param uriInfo
     * @return response
     */
    @POST
    @Path("/book")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addBook(Book b, @Context UriInfo uriInfo) {

        if (books.containsKey(b.getIsbn())) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } else {
            books.put(b.getIsbn(), b);

            UriBuilder ub = uriInfo.getAbsolutePathBuilder();
            URI isbnUri = ub.path(b.getIsbn()).build();
            return Response.created(isbnUri).build();
        }
    }

    /***
     * Supprime un livre de la bibliothèque
     * @param isbn
     * @return response
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
     * @return response
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
     * Met à jour les données d'un livre sauf l'isbn qui ne peux changer
     * @param b
     * @param isbn
     * @param uriInfo
     * @return response
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
            return Response.status(Response.Status.CONFLICT).build();
        }

        books.remove(isbn);
        books.put(b.getIsbn(), b);

        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        return Response.noContent().build();

    }
}