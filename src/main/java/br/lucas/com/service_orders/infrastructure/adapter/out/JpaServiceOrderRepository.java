package br.lucas.com.service_orders.infrastructure.adapter.out;

import br.lucas.com.service_orders.domain.model.ServiceOrderStatus;
import br.lucas.com.service_orders.domain.model.ServiceOrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaServiceOrderRepository extends JpaRepository<ServiceOrderEntity, Long> {

    Optional<ServiceOrderEntity> findByIdAndDeletedAtIsNull(Long id);

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
