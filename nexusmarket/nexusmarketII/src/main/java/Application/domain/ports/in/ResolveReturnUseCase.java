package Application.domain.ports.in;

import Application.domain.models.Return;

/**
 * Input port (use case): approves or rejects a return request, and
 * retrieves a return request according to the access permissions of the
 * requesting user.
 */
public interface ResolveReturnUseCase extends ApproveReturnUseCase {

    Return rejectReturn(String administratorId, String returnId);

    Return consultReturn(String requesterId, String returnId);
}
