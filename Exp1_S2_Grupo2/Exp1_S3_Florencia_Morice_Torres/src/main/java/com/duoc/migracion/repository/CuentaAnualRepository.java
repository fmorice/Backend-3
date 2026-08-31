package com.duoc.migracion.repository;

import com.duoc.migracion.model.CuentaAnual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CuentaAnualRepository extends JpaRepository<CuentaAnual, Long> {
}