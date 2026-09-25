package br.lucas.com.service_orders.infrastructure.adapter.out;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaServiceOrderRepository extends JpaRepository<ServiceOrderEntity, Long> {

    Optional<ServiceOrderEntity> findByIdAndDeletedAtIsNull(Long id);
}
