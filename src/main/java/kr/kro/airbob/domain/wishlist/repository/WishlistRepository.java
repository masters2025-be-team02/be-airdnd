package kr.kro.airbob.domain.wishlist.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import kr.kro.airbob.domain.wishlist.Wishlist;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
	@Query("""
	   SELECT w 
	   FROM Wishlist w 
	   WHERE w.member.id = :memberId 
	   AND (:lastCreatedAt IS NULL OR w.createdAt < :lastCreatedAt 
	   OR (w.createdAt = :lastCreatedAt AND w.id < :lastId)) 
	   ORDER BY w.createdAt DESC, w.id DESC
""")
	Slice<Wishlist> findByMemberIdWithCursor(Long memberId, Long lastId, LocalDateTime lastCreatedAt, PageRequest of);
}
