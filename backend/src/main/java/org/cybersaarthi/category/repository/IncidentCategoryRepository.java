package org.cybersaarthi.category.repository;

import org.cybersaarthi.category.entity.IncidentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncidentCategoryRepository extends JpaRepository<IncidentCategory, UUID> {

    Optional<IncidentCategory> findByTaxonomyVersionAndCode(String taxonomyVersion, String code);

    List<IncidentCategory> findAllByTaxonomyVersionAndIsActiveTrue(String taxonomyVersion);
}
