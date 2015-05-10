package dojo;

public class Cashier {

	public static void processOrder(Payment payment, Order order) {
		if (order.getBillAmount() != 0)
			payment.performPayment(order.getBillAmount());
	}

}
