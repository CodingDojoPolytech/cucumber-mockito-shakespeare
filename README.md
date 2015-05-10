# Kata Cucumber / Mockito

  - Date: Mai 2015.
  - Références:
    - Cukes.info
    - Mockito

Le thème "Roméo & Juliette" du kata est librement inspiré d'un atelier sur les test d'acceptations fait par Viviane Morelle et Nicolas Verdot dans le cadre du SUG sophipolitain.

Pour lancer l'exemple avec maven:

	azrael:cucumber-mockito-shakespeare mosser$ mvn clean package

Avertissement: Le but de ce kata est de mettre l'accent sur la notion de test d'acceptation (Cucumber) et d'utilisations de bouchons dans une arhcitecture (mockito). Java n'y est vu que comme un support.

## Contexte: User story


As a (user), I want to (action) so that (benefit).

  1. transformer la story en élément spécifiable, l’implémenter sous la forme d’un test pour la vérifier (tests unitaire / intégration)
  2. mieux: écrire la spec comme un test unitaire, avant le code. C’est du test-driven development, et c’est so 2002.
  3. mieux au carré: utiliser directement la story comme un test. 

Chiche? C’est le principe du BDD (et en vrai ça existe depuis 2003). Il existe pleins de framework pour travailler en BDD. 

Majoritairement j’utilise specs2 en scala, mais déjà qu’on va voir deux nouveaux trucs, pas la peine de vous faire peur avec Scala. Donc on va rester dans l’écosystème Java, et utiliser Cucumber puis ensuite Mockito.


## Thème: draguer dans un bar


Epic traitée dans le kata :
 
  //As Romeo, I want to offer a drink to Juliette so that we can discuss together (and maybe more).//



## Objectif du kata


  - Utiliser Cucumber dans un projet Java
  - Déclarer des specs avec Cucumber
  - Rendre les étapes de scénarios paramétrables, et les scénarios aussi
  - Utiliser des mocks pour simuler aux interfaces.

## Phase 1 : Utilisation de Cucumber

### Setup du projet.


On va utiliser Cucumber. A la base c’est fait pour bosser en Ruby, mais c’est dispo au dessus de la JVM. Et c’est **the** reference dans l’écosystème Java.

On prend maven pour se simplifier la vie. (oui, vraiment)

	$ touch pom.xml

Dans le POM, on charge Junit, cucumber4java et le lien entre les deux. That’s all folks. J’ai dit simple.

```
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>codingdojo</groupId>
    <artifactId>kata-bdd</artifactId>
    <version>1.0-SNAPSHOT</version>
    <dependencies>
        <dependency>
            <groupId>info.cukes</groupId>
            <artifactId>cucumber-java</artifactId>
            <version>1.2.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>info.cukes</groupId>
            <artifactId>cucumber-junit</artifactId>
            <version>1.2.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

	$ mkdir -p src/main/java/dojo src/test/java/dojo src/test/resources/dojo
	$ mvn clean package

build success, on est bon.

import dans IntelliJ comme un projet Maven, JDK 1.8. Il y a un plugin Cucumber4Java pour IntelliJ


### Setup de Cucumber

Cucumber va faire la glue entre le code de test qu’on va écrire pour opérationaliser la spec et une description de la story. Le langage utilisé pour décrire nos stories est Gherkin, un DSL dédié à la spécifications (//teasing cours IDM-1 en 5A//).

On crée un fichier de test qui va se contenter de déclencher Cucumber. Le reste est automatique, on ne va plus écrire de test unitaire directement (on est en 2015 tout de même …), mais des scénarios métiers au niveau de l’architecture fonctionnelle.

Fichier "`RunCucumberTest.java`", dans `test/java/dojo`:

```
package dojo;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
public class RunCucumberTest { }
```


On crée maintenant un fichier de feature qui décrit notre epic. Fichier `test/resources/dojo/cocktail.feature`

```
Feature: Cocktail Ordering

  As Romeo, I want to offer a drink to Juliette so that we can discuss together (and maybe more).
```

On peut lancer le fichier RunCucumberTest, le test n’est pas lancé, normal, il est vide pour le moment.



### Premier morceau de concombre


Première story. 

Small tiny steps. 

Qu’est-ce qui est minimal dans notre epic? 

Pourvoir créer une commande vide. Gherkin utilise une syntax Given/When/Then pour créer des scénarios, mais on retrouve les basics de la story derrière : un triplet (persona, action, bénéfice).


Donc, on spécifie notre système comme suit:

    Scenario: Creating an empty order
      Given Romeo who wants to buy a drink
      When  an order is declared for Juliette
      Then  there is no cocktail in the order

On lance le test. Junit rale, normal, il ne sait pas quoi faire de notre spec.

On va décrire les étapes du scénario dans une classe java (`CocktailStepsDefinitions.java`):

```
package dojo;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static org.junit.Assert.*;

import java.util.List;

public class CocktailStepDefinitions {

	private Order order;

	@Given("^Romeo who wants to buy a drink$")
	public void romeo_who_wants_to_buy_a_drink() {
		order = new Order();
		order.declareOwner("Romeo");
	}

	@When("^an order is declared for Juliette$")
	public void an_order_is_declared_for_juliette() {
		order.declareTarget("Juliette");
	}

	@Then("^there is 0 cocktails in the order$")
	public void there_is_no_cocktail_in_the_order() {
		List<String> cocktails =  order.getCocktails();
		assertEquals(0, cocktails.size());
	}
}
```

Il faut du coup aussi créer la classe Order, et les méthodes qui vont bien (dans main cette fois, pas dans test). 

IntelliJ: On utilise alt / create method dans la classe de specs pour créer les méthodes. 

On parle de Behavioral-driven, dans le sens ou le comportement fait émerger l’API public. 

On expose une api publique uniquement dirigée par le métier, et pas par la structure. C’est très loin d’être évident à faire sur de gros projets, il faut de l’expérience et une certaine "excellence technique" qui vient uniquement avec le temps.

Fichier `Order.java`

```
package dojo;

import java.util.ArrayList;
import java.util.List;

public class Order {

	private String from;
	private String to;
	private List<String> contents = new ArrayList<String>();

	public void declareOwner(String romeo) {
		this.from = romeo ;
	}

	public void declareTarget(String juliette) {
		this.to = juliette ;
	}

	public List<String> getCocktails() {
		return contents;
	}
}
```

On lance le test, il passe. **En vrai il faudrait d’abord faire échouer le test, puis implémenter ce qui le fait passer.** Mais la timebox joue contre nous => on va a l’essentiel.

Avertissement: Oui, en java, on déclare des getters et des setters, on utilise un constructeur pour initialiser, ... soit. mais là n'est pas la question.

### Rendre les étapes de tests paramétrable

Roméo, Juliette, … c’est so 1597 … Et puis pourquoi pas Tom et Jerry?

On va essayer de rendre les étapes de scénarios paramétrable, pour pouvoir s’en servir dans d’autres scénarios.

Qu’est-ce qui serait réutilisable dans nos étapes? Tout en fait ! On peut changer les prénoms, et le nombre de cocktails attendus dans la liste.

en fait, si on regarde les annotations Java utilisée, elles reposent sur des expressions rationnelles (oui, ça sert dans autre chose que pour écrire des compilateurs ou reconnaitre abbabbab).

Donc, on va définir des expressions rationnelles dans nos annotations, et Cucumber va automatiquement les matcher et les passer en paramètre des fonctions d’étapes.

  - Matcher un prénom: (.*)
  - Matcher un entier: (\\d+)

```
	@Given("^(.*) who wants to buy a drink$")
	public void romeo_who_wants_to_buy_a_drink(String romeo) {
		order = new Order();
		order.declareOwner(romeo);
	}

	@When("^an order is declared for (.*)")
	public void an_order_is_declared_for_juliette(String juliette) {
		order.declareTarget(juliette);
	}

	@Then("^there is (\\d+) cocktails in the order$")
	public void there_is_n_cocktails_in_the_order(int n) {
		List<String> cocktails =  order.getCocktails();
		assertEquals(n, cocktails.size());
	}
```

Le test passe toujours. On est iso-fonctionnel, mais les étapes sont maintenant paramétrables.

### Tests par l’exemple : Mots doux


Au niveau du métier, on peut donner des bases d’exemples que le système doit comprendre et interpréter.

On va utiliser la commande pour envoyer un message a la cible. 

Le scénario "de base" serait:

```
Scenario: Sending a message with an order
      Given Romeo who wants to buy a drink
      When  an order is declared for Juliette
        And  a message saying "Wanna chat?" is added
      Then the ticket must say "From Romeo to Juliette: Wanna chat?"
```

On écrit les étapes. Matcher tout sauf " : ([^\"]*) 
```

	@When("^a message saying \"([^\"]*)\" is added$")
	public void a_message_saying_something_is_added(String something){
	  	order.withMessage(something);
	}

	@Then("^the ticket must say \"([^\"]*)\"$")
	public void the_ticket_must_say_something_else(String somethingElse){
		String expected = String.format("From %s to %s: %s", 
			order.getOwner(), order.getTarget(), order.getMessage());
		assertEquals(expected, order.getTicketMessage());
	}
```

On ajoute ce qui manque dans l’API publique de `Order`:

```
	public String getOwner() { return from; }

	public String getTarget() { return to; }

	public String getMessage() { return message; }

	public String getTicketMessage() {
		return "From " + from + " to " + to + ": " + message;
	}
```

On lance, ca passe. Perfekt.

Mais comment tester ça sur plusieurs messages? Plusieurs destinataires? On va utiliser une "base d’exemples", et rendre paramétrable le scénario lui-même et plus seulement les étapes.

    Scenario Outline: Sending a message with an order

      Given <from> who wants to buy a drink
      When  an order is declared for <to>
        And  a message saying "<message>" is added
      Then the ticket must say "<expected>"

      Examples:
        | from  | to       | message     | expected                            |
        | Romeo | Juliette | Wanna chat? | From Romeo to Juliette: Wanna chat? |
        | Tom   | Jerry    | Hei!        | From Tom to Jerry: Hei!             |
        # ...

On lance, ça passe. Regarder le nombre d’étapes exécutées, bien plus que ce qu’on a écrit. 

Dérouler le résultat ses Junit dans IntelliJ. On voit toutes les instances du scénarios développé.

Minimiser son effort. Be Lazy.

### Factorisation des specs

Roméo, c’est un peu un lourdeau, c’est toujours lui qui offre des verres. Et en plus il en offre toujours à Juliette. Du coup on va le dire une fois pour toute (minimiser notre effort). On utilise un "background" pour ça.

```
Feature: Cocktail Ordering

  As Romeo, I want to offer a drink to Juliette so that we can discuss together (and maybe more).

    Background:
      Given Romeo who wants to buy a drink
      When  an order is declared for Juliette
      
    Scenario: Creating an empty order
      Then  there is 0 cocktails in the order

    Scenario Outline: Sending a message with an order

      When  an order is declared for <to>
        And  a message saying "<message>" is added
      Then the ticket must say "<expected>"

      Examples:
        | to       | message     | expected                            |
        | Juliette | Wanna chat? | From Romeo to Juliette: Wanna chat? |
        | Jerry    | Hei!        | From Romeo to Jerry: Hei!           |
        # ...
```


### Retrospective apport BDD


On vient de voir comment Cucumber pouvait nous aider à :

  - Écrire des specs presque comme des stories
  - Mapper ces specs sur des tests U
  - Rendre le tout paramétrable a coup d’expressions rationnelles.

Software engineering rocks ^^.

Attention, ça ne veut pas dire qu’on écrit plus de tests unitaires ni de tests d’intégration. Loin de là. On va forcément avoir des tests à écrire sur des fonctionalités précises. 

L’avantage du BDD est de travailler avec des scénarios de spécifications exprimés dans les termes du métiers, qui rendent les choses compréhensible pour un client. 

D’un point de vue agile, c’est une manière d’exposer les tests d’acceptations des stories. Il y a certes un effort pour écrire ces scénarios, mais au final tout le monde y gagne (lisibilité, maintenance).

## Phase 2 : Mockito

### Vif du sujet : offrir un cocktail a Juliette
 

Il faut pouvoir offrir le fameux verre à juliette. Mais on ne peut pas offrir ce qu’on veut, il faut offrir un cocktail proposé par la carte du bar.

Sauf que la carte … on l’à pas. Il faudrait une base de données pour stocker le contenu de la carte. Il faudra faire intervenir dans la définition du système des "experts" bases de données. Et justement, c’est tout là l’intérêt: connaitre les limite de sa propre incompétence. On va s'occuper de ce qu'on sait faire, et mettre un "bouchon" là où on ne sait plus.

D’un point de vue architectural, on se schtroupmfe du comportement interne de la Carte. On veut juste utiliser son interface, les "experts" DBMS nous fourniront une implèm de cette interface. Et au moins on définira d’un point de vue fonctionnel comment on veut se servir de la carte, et on évitera les interfaces CRUD classiques qui ne fournissent pas un super support aux developeurs.

Donc, comment on veut utiliser notre carte ? On va donner un "code" qui correspond au numéro du cocktail choisi, et la BDD nous dira son prix et son nom "joli".

Fichier `Menu.java`

```
package dojo;

public interface Menu {

	public String getPrettyName(int code);

	public int getPrice(int code);

}
```

Ok. So what? A l’ancienne, on pourrait se faire une classe d’implem de test pour nous, et l’utiliser dans nos étapes de scénarios. Mais bon, on n’a pas forcément besoin de tout implémenter pour chacun de nos tests … minimiser notre effort … 

Donc? Et bien on va mettre un "bouchon". Un mock. En bouchonnant l’interface, on utilise la bonne abstraction dans l’architecture, et on se cogne des détails techniques derrière. Sémantiquement, dans nos specs, on écrit juste le minimum dont on a besoin. La base de données de la Carte devra quand à elle être testée unitairement de son coté. Mais c’est le taff des experts DBMS, pas le notre.


On ajoute une dépendance a Mockito dans le projet. Mockito stable est en 1.10.19 (dec 2014). La 2.0 est on it’s way.

```
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>1.10.19</version>
            <scope>test</scope>
        </dependency>
```

On crée un scénario ou Romeo va offrir un Mojito à Juliette. L’idée d’un mock est de créer une "fausse" instance de l’objet manipulée, et de spécifier son comportement dans le test.

On va donc avoir besoin d’étapes de scénarios du genre:

      When a mocked menu is used
        And the mock binds #42 to mojito
  
L’implèm des étapes est triviale: ( `import static org.mockito.Mockito.*;` )

	@When("^a mocked menu is used$")
	public void a_mocked_menu_is_used(){
		menu = mock(Menu.class);
		order.useMenu(menu);
	}

	@When("^the mock binds #(\\d+) to (.*)$")
	public void the_mock_binds_Id_to_Cocktail(int id, String cocktail) {
		when(menu.getPrettyName(id)).thenReturn(cocktail);
	}

On crée useMenu au passage:

	public void useMenu(Menu menu) { this.menu = menu; }

On peut du coup avoir le bon scénario:

    Scenario: Offering a mojito to Juliette
      When a mocked menu is used
        And the mock binds #42 to mojito
        And a cocktail #42 is added to the order
      Then there is 1 cocktails in the order
        And  the order contains a mojito

Implèm des étapes:

	@When("^a cocktail #(\\d+) is added to the order$")
	public void a_cocktail_C_is_added_to_the_order(int C) {
	 	order.addCocktail(C);
	}

	@Then("^the order contains a (.*)")
	public void the_order_contains_a_given_cocktail(String givenCocktail) {
	  	assertTrue(order.getCocktails().contains(givenCocktail));
	}

Et implèm des méthodes dans Order:

	public void addCocktail(int c) { this.contents.add(menu.getPrettyName(c)); }


### Dernière étape: payer sa conso.

On veut payer la conso. Mais on ne va quand même pas payer réellement a chaque fois qu’on lance le jeu de test … ça serait ruineux, et en plus des boites comme Paypal prennent une com sur chaque transaction.

On va donc considérer qu’on veut payer sa commande. Mais pas la payer effectivement. Juste vérifier que le paiement est bien parti chez le prestataire tiers. (pensez intégration).

On prend un interface de paiement la plus simple possible : `Payment.java`

```
package dojo;

public interface Payment {
	public void performPayment(int amount);
}
```

On implémente la caisse du barm avec une statique pour aller plus vite dans `Cashier.java` :

```
package dojo;

public class Cashier {

	public static void processOrder(Payment payment, Order order) {
		payment.performPayment(order.getBillAmount());
	}

}
```

Modifs dans Order pour maintenir le solde:

	public void addCocktail(int c) {
		this.contents.add(menu.getPrettyName(c));
		this.amount += menu.getPrice(c);
	}

	public int getBillAmount() { return amount; }


Et maintenant le scénario qui montre ça:

    Scenario: Paying the mojito offered to Juliette
      When a mocked menu is used
        And the mock binds #42 to $10
        And a cocktail #42 is added to the order
        And Romeo pays his order
      Then the payment component must be invoked 1 time for $10

On se fait un bouchon du service de paiement, avec les étapes associées:

	@When("^Romeo pays his order$")
	public void romeo_pays_his_order() {
		paypal = mock(Payment.class);
		Cashier.processOrder(paypal, order);
	}

	@Then("^the payment component must be invoked (\\d+) time for \\$(\\d+)")
	public void the_payment_component_must_be_invoked_N_times(int n, int value){
		 verify(paypal, times(n)).performPayment(value);
	}



On lance, ça passe. Pourquoi? **En effet, ça ne devrait pas**, le premier bouchon ne définit pas le prix du cocktail.  En fait c’est pas grave, Mockito renvoit des valeurs par défaut quand on ne spécifie pas. Ici, c’est un int qui est attendu => il renvoie 0 par défaut.


On peut aussi décider au niveau du métier que le service de paiement ne doit pas être appelé pour une facture à $0.

    Scenario: Not paying the empty bill
      When Romeo pays his order
      Then the payment component must be invoked 0 time for $0

Là, le test crash : `org.mockito.exceptions.verification.NeverWantedButInvoked`

On ajoute la contrainte dans le système de caisse.

	public static void processOrder(Payment payment, Order order) {
		if (order.getBillAmount() != 0)
			payment.performPayment(order.getBillAmount());
	}

That’s all folks.



### Retrospective Mock


Les mocks servent à intégrer, a simuler aux interfaces des services tiers sur lesquels on a pas la main. Ça permet aussi de contractualiser des éléments d’interfaces entre des sous-partie d’un même projet, et d’avancer en isolation une fois l’interface commune définie.

Attention, BDD + mock ne résoud pas tout les problèmes de la terre. On n’a pas vérifié par exemple que le calcul du montant de l’addition était correct. 

  - Est-ce réellement une spec fonctionnelle? 
  - Ou se situe le niveau d’abstraction dans les feature spécifiées? 

Cela revient à définir ce qu’est le métier. Et c’est pas simple.


