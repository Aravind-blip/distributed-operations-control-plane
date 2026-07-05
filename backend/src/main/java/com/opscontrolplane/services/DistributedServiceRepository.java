package com.opscontrolplane.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface DistributedServiceRepository extends JpaRepository<DistributedService, UUID>,
        JpaSpecificationExecutor<DistributedService> {

    List<DistributedService> findByNameIgnoreCase(String name);

    Page<DistributedService> findAll(Pageable pageable);
}
