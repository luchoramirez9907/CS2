package Application.domain.ports.in;

import Application.domain.models.Return;

/**
 * Input port (use case): approves or rejects a return request.
 */
public interface ApproveReturnUseCase {

    /**
     * @param administratorId identifier of the Administrator approving the return
     * @param returnId        identifier of the Return
     * @return the approved Return
     */
    Return approveReturn(String administratorId, String returnId);
}
