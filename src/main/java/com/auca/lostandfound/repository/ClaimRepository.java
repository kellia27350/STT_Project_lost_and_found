package com.auca.lostandfound.repository;

import com.auca.lostandfound.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    /** All claims filed against a particular item. */
    List<Claim> findByItemId(Long itemId);

    /** All claims submitted by a particular student. */
    List<Claim> findByClaimantUserId(Long claimantUserId);

    /** Active claims by a student on a specific item — used for duplicate detection. */
    List<Claim> findByItemIdAndClaimantUserIdAndClaimStatus(
            Long itemId, Long claimantUserId, String claimStatus);
}
