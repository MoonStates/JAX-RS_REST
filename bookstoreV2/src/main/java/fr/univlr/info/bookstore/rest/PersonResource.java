package fr.univlr.info.bookstore.rest;

import fr.univlr.info.bookstore.domain.Book;
import fr.univlr.info.bookstore.domain.Person;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path("/api/")
public class PersonResource {
    //Declare pseudo data for database
    static Person pierre = new Person("Pierre", "Durand");
    static Person paul = new Person("Paul", "Martin");
    static Book livreA = new Book("A34", "LeTitre", Arrays.asList(pierre), 13.45f);
    static Book livreB = new Book("A35", "LeTitre", Arrays.asList(pierre), 13.45f);
    static Book livreC = new Book("A36", "LeTitre", Arrays.asList(paul), 13.45f);

    // pseudo database
    private static Set<Person> people = new HashSet<>(Arrays.asList(pierre, paul));
    private static Set<Book> books = new HashSet<>(Arrays.asList(livreA, livreB, livreC));

    /***
     * Renvoie la totalité des livres si pas d'auteurs spécifié sinon sa liste de livre
     * @param firstname
     * @param lastname
     * @return
     */
    @GET
    @Path("/author/book")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Set<Book> getBookFromPerson(@QueryParam("firstname") String firstname, @QueryParam("lastname") String lastname ) {
        if (firstname ==  null && lastname == null) {
            Set<Book> c = new HashSet<>();
            c.addAll(books);
            return c;
        }
        if (firstname != null && lastname != null) {
            Set<Book> c = new HashSet<>();
            for (Iterator<Book> it = books.iterator(); it.hasNext(); ) {
                Book b = it.next();
                System.out.println(b.toString());
                if (b.getAuthors().contains(new Person(firstname, lastname)))
                    c.add(b);
            }

            return c;
        }

        return new HashSet<>();
    }

    /***
     * Renvoie la totalité des personnes si non spécifié sinon uniquement l'ateur spécifié
     * @param firstname
     * @param lastname
     * @return
     */
    @GET
    @Path("/author")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Set<Person> getPerson(@QueryParam("firstname") String firstname, @QueryParam("lastname") String lastname ) {
        if (firstname ==  null && lastname == null) {
            Set<Person> c = new HashSet<>();
            c.addAll(people);
            return c;
        }
        if (firstname != null && lastname != null) {
            Set<Person> c = new HashSet<>();
            for (Iterator<Person> it = people.iterator(); it.hasNext(); ) {
                Person p = it.next();
                if (p.equals(new Person(firstname, lastname)))
                    c.add(p);
            }

            return c;
        }

        return new HashSet<>();
    }

}
