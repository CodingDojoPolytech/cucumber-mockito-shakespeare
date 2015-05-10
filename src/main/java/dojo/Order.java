package dojo;

import java.util.ArrayList;
import java.util.List;

public class Order {

	private String from;
	private String to;
	private List<String> contents = new ArrayList<String>();
	private String message;
	private Menu menu;
	private int amount = 0;

	public void declareOwner(String romeo) { this.from = romeo ; }

	public void declareTarget(String juliette) { this.to = juliette ; }

	public List<String> getCocktails() { return contents; }

	public void withMessage(String something) { this.message = something; }

	public String getOwner() { return from; }

	public String getTarget() { return to; }

	public String getMessage() { return message; }

	public String getTicketMessage() {
		return "From " + from + " to " + to + ": " + message;
	}

	public void useMenu(Menu menu) { this.menu = menu; }

	public void addCocktail(int c) {
		this.contents.add(menu.getPrettyName(c));
		this.amount += menu.getPrice(c);
	}

	public int getBillAmount() { return amount; }

}
