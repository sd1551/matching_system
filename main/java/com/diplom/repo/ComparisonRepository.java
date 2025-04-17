package com.diplom.repo;

import com.diplom.entity.ComparisonResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ComparisonRepository extends JpaRepository<ComparisonResult, UUID> {
}
