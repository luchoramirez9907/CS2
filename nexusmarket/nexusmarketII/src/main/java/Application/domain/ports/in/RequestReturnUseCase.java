package Application.domain.ports.in;

import Application.domain.models.Return;

/**
 * Input port (use case): requests the return of a delivered order.
 */
public interface RequestReturnUseCase {

    /**
     * @param buyerId  identifier of the Buyer requesting the return
     * @param orderId  identifier of the delivered Order
     * @param reason   reason provided by the buyer
     * @return the requested Return (status REQUESTED)
     */
    Return requestReturn(String buyerId, String orderId, String reason);
}
