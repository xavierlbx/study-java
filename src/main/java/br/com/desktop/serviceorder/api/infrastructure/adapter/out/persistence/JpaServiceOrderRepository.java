package br.com.desktop.serviceorder.api.infrastructure.adapter.out.persistence;

import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderStatus;
import br.com.desktop.serviceorder.api.domain.serviceorder.ServiceOrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaServiceOrderRepository extends JpaRepository<ServiceOrderEntity, Long> {

    Optional<ServiceOrderEntity> findByIdAndDeletedAtIsNull(Long id);

    // Sem filtro de deletedAt: a constraint UNIQUE da tabela vale tambem para registros removidos logicamente
    boolean existsByProtocol(String protocol);

    long countByProtocolStartingWith(String prefix);

    @Query("""
            SELECT s FROM ServiceOrderEntity s
            WHERE s.deletedAt IS NULL
              AND (:protocol IS NULL OR s.protocol = :protocol)
              AND (:status IS NULL OR s.status = :status)
              AND (:type IS NULL OR s.type = :type)
            """)
    Page<ServiceOrderEntity> search(@Param("protocol") String protocol,
                                    @Param("status") ServiceOrderStatus status,
                                    @Param("type") ServiceOrderType type,
                                    Pageable pageable);
}
