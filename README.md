# Service REST (JAX-RS)
## Environnement de travail
Comme le travail s’effectuera à partir d’un projet Maven, la seule nécessité est que cet outil soit installé (comme par exemple sur l’image Ubuntu virtualisée) et éventuellement l’outil curl pour générer différents types de requêtes HTTP.
## Développement d’un service REST élémentaire à partir d’une classe unique
Le but ici est de développer une API REST simple en utilisant la bibliothèque JAX-RS (RS = RESTful Services) qui, à l’aide d’annotations (de manière similaire celles utilisées pour JPA), permet de développer assez facilement ce type d’API.
### Spécifications
Nous allons reprendre la classe Book (vue dans l’UE « Bases de données avancées » lors de la persistance avec JPA) et créer un service permettant d’accéder à des ressources de type Book avec les fonctionnalités CRUD implémentées avec les méthodes HTTP correspondantes
(cf. cours). 
Le format des URL de l’API REST, pour être compatibles avec les tests fournis, est imposé et ils sont présentés comme ci-dessous :
```
GET : /api/book/{isbn}? Le paramètre isbn est optionnel, si absent l’ensemble des livres sera retourné.
POST : /api/book Création d’un livre avec l’ISBN, le titre, le nom de l’auteur et le prix.
PUT : /api/book/{isbn} Modification des informations d’un livre, excepté son ISBN.
DELETE : /api/book/{isbn}? Le paramètre isbn est optionnel, si absent l’ensemble des livres sera supprimé.
```
### Mise en œuvre
Compléter la classe BookResource pour implémenter l’ensemble des fonctionnalités en vous basant sur les requêtes de la classe de test BookResourceTest. La classe de test est automatiquement exécutée lorsqu’on effectue la commande « mvn test » (le projet est
d’ailleurs fourni avec 1 test fonctionnel).
En effectuant la commande « mvn install », un fichier Bookstore-thorntail.jar est généré dans le répertoire target. Ce fichier contient à la fois votre application et un serveur J2EE (WildFly). On peut ainsi déployer l’application avec la commande suivante :
```
java -jar Bookstore-thorntail.jar
```
Il est possible de tester ponctuellement un service en utilisant cURL (voir ici quelques informations sur son utilisation).
JAX-RS permet de prendre en compte automatiquement la sérialisation JSON et XML ce qui permet de passer (ou de retourner) directement des objets « métier » (Book en l’occurrence). Vous veillerez à ce que votre implémentation de l’API puisse retourner soit un format JSON, soit un format XML.
## Enrichissement du service REST à l’aide d’une seconde classe
### Spécifications
La première version de la classe Book présente l’inconvénient majeur :
* de ne pouvoir spécifier qu’un seul auteur ;
* que les informations sur l’auteur se résument uniquement à son nom.
Pour remédier à cela, une classe Person sera créée avec la structure suivante :
```
public class Person {
  private String firstname;
  private String lastname;
  private Collection<Book> books;
...
}
```
La classe Book sera également modifiée pour prendre en compte la notion d’auteur :
```
public class Book {
  private String isbn;
  private String title;
  private Collection<Person> authors;
  private float price;
...
}
```
L’ajout de la classe Person ajoutera à l’API REST de nouvelles fonctionnalités :
```
GET : /api/author?{firstname}=value&{lastname}=value Les paramètres seront optionnels, si absents l’ensemble des auteurs sera retourné.
GET : /api/author/book?{firstname}=value&{lastname}=value Les paramètres seront optionnels, si absents l’ensemble des livres sera etourné.
GET : /api/book/{isbn}/authors Retourne l’ensemble des auteurs d’un livre.
```
Dans le cas d’un GET sur l’URL /api/book, le format JSON devra respecter la forme suivante :
```
[
{
"isbn": "ZT57",
"title": "Roman",
"price": 8.0,
"authors": [
{
"firstname": "Pierre",
"lastname": "Durand"
}
]
},
{
"isbn": "ZT56",
"title": "Essai",
"price": 12.4,
"authors": [
{
"firstname": "Paul",
"lastname": "Martin"
},
{
"firstname": "Pierre",
"lastname": "Durand"
}
]
}
]
```
Et le format XML la structure présentée ci-dessous (cf. l’annotation @XmlElementWrapper)
en respectant l’ordre de déclaration des attributs dans la classe Book :
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<collection>
<book>
<isbn>ZT57</isbn>
<title>Roman</title>
<authors>
<author>
<firstname>Pierre</firstname>
<lastname>Durand</lastname>
</author>
</authors>
<price>8.0</price>
</book>
<book>
<isbn>ZT56</isbn>
<title>Essai</title>
<authors>
<author>
<firstname>Paul</firstname>
<lastname>Martin</lastname>
</author>
<author>
<firstname>Pierre</firstname>
<lastname>Durand</lastname>
</author>
3
</authors>
<price>12.4</price>
</book>
</collection>
```
### Mise en œuvre
Pour la mise en œuvre, il est conseillé de :
* créer une classe Person et modifier la classe Book pour qu’elle la référence ;
* récupérer la nouvelle classe de test BookResourceV2Test ;
* modifier la classe BookResource pour que ses fonctionnalités satisfassent les tests de BookResourceV2Test ;
* récupérer la classe de test PersonResourceTest ;
* créer une classe PersonResource pour que ses fonctionnalités satisfassent les tests de PersonResourceTest ;
## Remise du code développé
Le code remis devra :
* être commenté et être accompagné d’un fichier texte décrivant l’avancement du travail
en précisant ce qui fonctionne et ce qui ne fonctionne pas ;
* conserver obligatoirement la structure du projet Maven mais ne pas contenir le répertoire target car cela augmente inutilement la taille de l’archive ZIP que vous déposerez (il sera généré lors de la compilation du projet).
