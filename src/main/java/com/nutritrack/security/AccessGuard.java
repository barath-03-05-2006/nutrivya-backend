package com.nutritrack.security;

import com.nutritrack.entity.ClientProfile;
import com.nutritrack.entity.User;
import com.nutritrack.repository.ClientProfileRepository;
import com.nutritrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Central place for "is this person allowed to touch this client's data?" checks.
 *
 * Every endpoint that takes a {clientId} in the URL MUST call one of these methods
 * before reading or writing anything. Without this, any logged-in user (client or
 * dietitian) could change the id in the URL and access someone else's profile,
 * meal plans, weight history, or progress notes — this class exists specifically
 * to close that hole.
 *
 * Rule of access: a request for clientId X is only allowed if the caller is either
 *   (a) that exact client (X is looking at their own data), or
 *   (b) the dietitian who is actually assigned to client X.
 * Everyone else gets a 403, no matter how valid their JWT is.
 */
@Component
public class AccessGuard {

    @Autowired private UserRepository userRepo;
    @Autowired private ClientProfileRepository profileRepo;

    /** Thrown when access is denied; caught centrally and turned into a 403 response. */
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) { super(message); }
    }

    /** Resolves the logged-in user from the JWT/Authentication context. */
    public User currentUser(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("Not authenticated");
        }
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }

    /**
     * Verifies the caller is allowed to access clientId's data: either it's their
     * own data, or they are the dietitian assigned to that client.
     * Throws AccessDeniedException (→ 403) if not authorized.
     */
    public User requireClientAccess(Authentication auth, Long clientId) {
        User requester = currentUser(auth);

        // Case 1: the client is looking at their own data
        if (requester.getId().equals(clientId)) {
            return requester;
        }

        // Case 2: the requester is the dietitian assigned to this specific client
        if (requester.getRole() == User.Role.DIETITIAN) {
            ClientProfile profile = profileRepo.findByUserId(clientId).orElse(null);
            if (profile != null && profile.getDietitian() != null
                    && profile.getDietitian().getId().equals(requester.getId())) {
                return requester;
            }
        }

        throw new AccessDeniedException("You don't have permission to access this client's data");
    }

    /**
     * Stricter variant for dietitian-only write actions (e.g. setting targets,
     * adding progress notes) — the client themselves should NOT be able to call these,
     * only their own assigned dietitian.
     */
    public User requireDietitianOwnership(Authentication auth, Long clientId) {
        User requester = currentUser(auth);

        if (requester.getRole() != User.Role.DIETITIAN) {
            throw new AccessDeniedException("Only the assigned dietitian can perform this action");
        }

        ClientProfile profile = profileRepo.findByUserId(clientId).orElse(null);
        if (profile == null || profile.getDietitian() == null
                || !profile.getDietitian().getId().equals(requester.getId())) {
            throw new AccessDeniedException("You are not the assigned dietitian for this client");
        }

        return requester;
    }
}
